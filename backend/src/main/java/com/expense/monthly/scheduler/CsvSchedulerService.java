package com.expense.monthly.scheduler;

import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.ImportFailureService;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import java.io.File;
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
 *   /app/csv-input/2026/May/commbank.csv
 *
 * Valid month folder names (case-sensitive): Jan, Feb, Mar, Apr, May, Jun,
 *                                            Jul, Aug, Sep, Oct, Nov, Dec
 *
 * After processing:
 *   - Success: file is renamed to <bank>.csv.done (skipped on next scan)
 *   - Failure: file is renamed to <bank>.csv.failed (skipped on next scan)
 *              + an ImportFailure DB record is created with an actionable error message
 *              → visible in the Import page "Failed Imports" panel
 *              → user can download the .csv.failed file, fix it, and re-upload via Import UI
 */
@Service
@ConditionalOnProperty(name = "reprocess.only", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class CsvSchedulerService {

    private final TransactionService transactionService;
    private final CsvFileManager csvFileManager;
    private final ImportFailureService importFailureService;

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
            processYearFolder(yearFolder);
        }
    }

    private void processYearFolder(File yearFolder) {
        File[] monthFolders = yearFolder.listFiles(f -> f.isDirectory() && csvFileManager.isValidMonth(f.getName()));
        if (monthFolders == null || monthFolders.length == 0) {
            log.debug("No valid month folders (e.g. Jan, Feb) found in: {}", yearFolder.getName());
            return;
        }

        for (File monthFolder : monthFolders) {
            processMonthFolder(yearFolder.getName(), monthFolder);
        }
    }

    private void processMonthFolder(String year, File monthFolder) {
        File[] csvFiles = monthFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            log.debug("No CSV files in: {}/{}", year, monthFolder.getName());
            return;
        }

        log.info("Found {} CSV file(s) in {}/{}", csvFiles.length, year, monthFolder.getName());

        for (File csvFile : csvFiles) {
            processFile(year, monthFolder.getName(), csvFile);
        }
    }

    private void processFile(String year, String month, File csvFile) {
        log.info("Processing {}/{}/{}", year, month, csvFile.getName());

        try {
            List<Transaction> transactions = transactionService.processCSVFile(csvFile, defaultCurrency);
            log.info("Imported {} transaction(s) from {}/{}/{}", transactions.size(), year, month, csvFile.getName());

            File doneFile = new File(csvFile.getParentFile(), csvFile.getName() + ".done");
            if (!csvFile.renameTo(doneFile)) {
                log.warn("Could not rename processed file to .done: {}/{}/{}", year, month, csvFile.getName());
            }
        } catch (Exception e) {
            // Rename to .csv.failed so the scheduler skips it on the next scan
            File failedFile = new File(csvFile.getParentFile(), csvFile.getName() + ".failed");
            if (csvFile.renameTo(failedFile)) {
                String relativePath = year + "/" + month + "/" + failedFile.getName();
                importFailureService.recordFailure(relativePath, e.getMessage());
                log.error("Failed to process {}/{}/{} — recorded in Import page. Error: {}",
                        year, month, csvFile.getName(), e.getMessage());
            } else {
                log.error("Failed to process {}/{}/{} AND could not rename to .failed. Error: {}",
                        year, month, csvFile.getName(), e.getMessage());
            }
        }
    }
}
