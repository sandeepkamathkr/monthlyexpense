package com.expense.monthly.scheduler;

import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Scheduler service that scans a year/month folder structure for CSV files every 5 minutes
 * and automatically imports them into the database.
 *
 * Expected folder layout (drop files here):
 *   <input-folder>/<year>/<month>/<bank>.csv
 *
 * Example:
 *   /app/csv-input/2025/Jan/westpac.csv
 *   /app/csv-input/2025/Jan/anz.csv
 *   /app/csv-input/2026/Jan/commbank.csv
 *
 * Valid month folder names (case-sensitive): Jan, Feb, Mar, Apr, May, Jun,
 *                                            Jul, Aug, Sep, Oct, Nov, Dec
 *
 * After processing:
 *   - Success: file is renamed to <bank>.csv.done (kept in place, skipped on next scan)
 *   - Failure: file is copied to <input-folder>/unprocessed/<year>/<month>/<bank>.csv
 *              (original stays in place and will be retried on next scan)
 */
@Service
@ConditionalOnProperty(name = "reprocess.only", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class CsvSchedulerService {

    private final TransactionService transactionService;
    private final CsvFileManager csvFileManager;

    @Value("${csv.scheduler.default-currency:AUD}")
    private String defaultCurrency;

    @Value("${csv.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedRateString = "${csv.scheduler.interval-ms:300000}")
    @SchedulerLock(name = "csvScheduler", lockAtMostFor = "PT9M", lockAtLeastFor = "PT1M")
    public void scanAndUploadCsvFiles() {
        if (!schedulerEnabled) {
            log.debug("CSV scheduler is disabled, skipping scan.");
            return;
        }

        File inputFolder = csvFileManager.getInputFolder();
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            log.debug("CSV input folder does not exist or is not a directory: {}", inputFolder.getPath());
            return;
        }

        File[] yearFolders = inputFolder.listFiles(f -> f.isDirectory() && csvFileManager.isValidYear(f.getName()));
        if (yearFolders == null || yearFolders.length == 0) {
            log.debug("No year folders (e.g. 2025, 2026) found in: {}", inputFolder.getPath());
            return;
        }

        for (File yearFolder : yearFolders) {
            processYearFolder(inputFolder, yearFolder);
        }
    }

    private void processYearFolder(File inputFolder, File yearFolder) {
        File[] monthFolders = yearFolder.listFiles(f -> f.isDirectory() && csvFileManager.isValidMonth(f.getName()));
        if (monthFolders == null || monthFolders.length == 0) {
            log.debug("No valid month folders (e.g. Jan, Feb) found in: {}", yearFolder.getName());
            return;
        }

        for (File monthFolder : monthFolders) {
            processMonthFolder(inputFolder, yearFolder.getName(), monthFolder);
        }
    }

    private void processMonthFolder(File inputFolder, String year, File monthFolder) {
        File[] csvFiles = monthFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            log.debug("No CSV files in: {}/{}", year, monthFolder.getName());
            return;
        }

        log.info("Found {} CSV file(s) in {}/{}", csvFiles.length, year, monthFolder.getName());

        for (File csvFile : csvFiles) {
            processFile(inputFolder, year, monthFolder.getName(), csvFile);
        }
    }

    private void processFile(File inputFolder, String year, String month, File csvFile) {
        log.info("Processing {}/{}/{}", year, month, csvFile.getName());

        try {
            List<Transaction> transactions = transactionService.processCSVFile(csvFile, defaultCurrency);
            log.info("Imported {} transaction(s) from {}/{}/{}", transactions.size(), year, month, csvFile.getName());
            File doneFile = new File(csvFile.getParentFile(), csvFile.getName() + ".done");
            if (!csvFile.renameTo(doneFile)) {
                log.warn("Could not rename processed file to .done: {}/{}/{}", year, month, csvFile.getName());
            }
        } catch (Exception e) {
            log.error("Failed to process {}/{}/{} — {}", year, month, csvFile.getName(), e.getMessage());
            copyToUnprocessed(inputFolder, year, month, csvFile);
        }
    }

    private void copyToUnprocessed(File inputFolder, String year, String month, File csvFile) {
        File destination = new File(inputFolder,
                CsvFileManager.UNPROCESSED_DIR + File.separator + year + File.separator + month);
        destination.mkdirs();

        File destFile = new File(destination, csvFile.getName());

        if (destFile.exists()) {
            String baseName = csvFile.getName().replaceAll("\\.csv$", "");
            int counter = 1;
            do {
                destFile = new File(destination, baseName + "_" + counter + ".csv");
                counter++;
            } while (destFile.exists());
        }

        try {
            Files.copy(csvFile.toPath(), destFile.toPath());
            log.info("Copied failed file to unprocessed/{}/{}/{}", year, month, destFile.getName());
        } catch (IOException ex) {
            log.warn("Could not copy failed file to unprocessed: {}/{}/{}", year, month, csvFile.getName());
        }
    }
}