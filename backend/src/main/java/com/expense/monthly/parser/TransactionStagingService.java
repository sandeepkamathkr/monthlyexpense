package com.expense.monthly.parser;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.repository.ImportFailureRepository;
import com.expense.monthly.scheduler.CsvFileManager;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Writes a reviewed list of TransactionDTOs as a standardised CSV into the scheduler's
 * year/month folder so the existing scheduler picks it up and saves it to the DB.
 *
 * Standardised CSV format (what the existing scheduler + CsvToBean expect):
 *   Date,Description,Amount,Currency,Category
 *   15/01/2026,COLES,45.50,AUD,Groceries
 *
 * Re-import support:
 *   Before writing the new file this service clears stale data for the same month:
 *   1. Deletes DB transactions for that month/year
 *   2. Deletes all .csv / .csv.done / .csv.failed files in the month folder
 *   3. Deletes ImportFailure records whose path starts with "YYYY/MMM/"
 *
 * Adding a new bank: zero changes here — just add a YAML entry to bank.formats[].
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionStagingService {

    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransactionService transactionService;
    private final CsvFileManager csvFileManager;
    private final ImportFailureRepository importFailureRepository;

    @Value("${csv.scheduler.input-folder:./csv-input}")
    private String inputFolderPath;

    /**
     * Stages the reviewed transactions for scheduler pickup.
     *
     * @param transactions     list of reviewed TransactionDTOs (must not be empty)
     * @param originalFilename original filename from the user's upload (for the new file name)
     * @return relative path of the written file, e.g. "2026/May/1745123456-commbank.csv"
     * @throws IOException if the directory cannot be created or the file cannot be written
     */
    public String stage(List<TransactionDTO> transactions, String originalFilename) throws IOException {
        return stage(transactions, originalFilename, false);
    }

    public String stage(List<TransactionDTO> transactions, String originalFilename, boolean skipCleanup) throws IOException {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("Cannot stage an empty transaction list.");
        }

        // Derive year and month from the first transaction's date
        LocalDate firstDate = transactions.get(0).getDate();
        int year = firstDate.getYear();
        int monthInt = firstDate.getMonthValue();
        String monthAbbr = firstDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        log.info("Staging {} transactions for {}/{} (skipCleanup={})", transactions.size(), year, monthAbbr, skipCleanup);

        if (!skipCleanup) {
            // Cleanup: remove stale data for this month before writing the first file
            log.info("Cleaning up existing data for {}/{}", year, monthAbbr);
            transactionService.deleteTransactionsByMonth(monthInt, year);
            csvFileManager.deleteMonthFiles(String.valueOf(year), monthAbbr);
            importFailureRepository.deleteByFilePathStartingWith(year + "/" + monthAbbr + "/");
        }

        // --- Write the standardised CSV ---
        File monthDir = new File(inputFolderPath, year + File.separator + monthAbbr);
        if (!monthDir.exists() && !monthDir.mkdirs()) {
            throw new IOException("Could not create directory: " + monthDir.getPath());
        }

        String filename = System.currentTimeMillis() + "-" + sanitise(originalFilename);
        File outputFile = new File(monthDir, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("Date,Description,Amount,Currency,Category");
            for (TransactionDTO tx : transactions) {
                writer.printf("%s,%s,%s,%s,%s%n",
                        tx.getDate().format(CSV_DATE_FMT),
                        escapeCsv(tx.getDescription()),
                        tx.getAmount().toPlainString(),
                        tx.getCurrency() != null ? tx.getCurrency() : "AUD",
                        escapeCsv(tx.getCategory()));
            }
        }

        String relativePath = year + "/" + monthAbbr + "/" + filename;
        log.info("Staged file written: {}", relativePath);
        return relativePath;
    }

    /** Wraps a CSV field in double-quotes if it contains a comma or double-quote. */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Strips path-traversal characters from a user-supplied filename. */
    private String sanitise(String filename) {
        if (filename == null || filename.isBlank()) return "upload.csv";
        // Keep only safe characters: letters, digits, dash, underscore, dot
        return filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
