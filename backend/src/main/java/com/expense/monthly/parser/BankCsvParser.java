package com.expense.monthly.parser;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.service.CategoryOverrideService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses raw bank CSV files into TransactionDTOs.
 *
 * Format detection is purely content-based — no filename checks.
 * Supported formats are defined in application.yml under bank.formats[].
 * Adding a new bank = add one YAML entry; zero Java changes required.
 *
 * Detection logic: for each configured format, try to parse the date column
 * of the first data row using that format's datePattern. First match wins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankCsvParser {

    private final BankFormatsProperties bankFormatsProperties;
    private final TransactionCategorizer categorizer;
    private final CategoryOverrideService categoryOverrideService;

    /**
     * Parses a raw bank CSV upload into a list of TransactionDTOs.
     * Rows that are income/credits, empty, or match exclude filters are skipped.
     *
     * @param file     multipart file from the HTTP upload
     * @param currency ISO currency code (e.g. "AUD")
     * @return parsed, categorized transactions ready for review
     */
    public List<TransactionDTO> parse(MultipartFile file, String currency) throws IOException {
        List<String[]> rows = readAllRows(file);
        if (rows.isEmpty()) {
            log.warn("Uploaded file is empty: {}", file.getOriginalFilename());
            return List.of();
        }

        BankFormatConfig format = detectFormat(rows);
        if (format == null) {
            throw new IllegalArgumentException(
                    "Could not detect bank format. Ensure the file is a supported CSV export " +
                    "(CommBank or HSBC). Check that the date column uses DD/MM/YYYY or DD Mon YYYY format.");
        }

        log.info("Detected format '{}' for file: {}", format.getName(), file.getOriginalFilename());

        List<TransactionDTO> result = new ArrayList<>();
        // Skip header row if the format has one
        int startRow = format.isHasHeader() ? 1 : 0;

        for (int i = startRow; i < rows.size(); i++) {
            String[] row = rows.get(i);
            try {
                TransactionDTO dto = parseRow(row, format, currency, i + 1);
                if (dto != null) {
                    result.add(dto);
                }
            } catch (Exception e) {
                // Re-throw with row context so the caller can show an actionable error
                throw new IllegalArgumentException("Row " + (i + 1) + ": " + e.getMessage(), e);
            }
        }

        // Apply saved category overrides so the review UI pre-selects the user's choices
        Map<String, String> overrideMap = categoryOverrideService.getAllOverridesAsMap();
        for (TransactionDTO dto : result) {
            String key = dto.getDescription().toLowerCase().trim();
            if (overrideMap.containsKey(key)) {
                dto.setCategory(overrideMap.get(key));
                dto.setHasOverride(Boolean.TRUE);
            }
        }

        log.info("Parsed {} transactions from {}", result.size(), file.getOriginalFilename());
        return result;
    }

    /**
     * Detects which configured format matches this file by trying each format's
     * datePattern against the first data row's date column.
     */
    private BankFormatConfig detectFormat(List<String[]> rows) {
        for (BankFormatConfig format : bankFormatsProperties.getFormats()) {
            // Determine which row to test (skip header if declared)
            int testRowIndex = format.isHasHeader() ? 1 : 0;
            if (testRowIndex >= rows.size()) continue;

            String[] testRow = rows.get(testRowIndex);
            int dateCol = format.getColumns().getDate();
            if (dateCol >= testRow.length) continue;

            String rawDate = testRow[dateCol].replaceAll("\uFEFF", "").trim();
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format.getDatePattern(), Locale.ENGLISH);
                LocalDate.parse(rawDate, fmt);
                return format; // first format whose date parses successfully wins
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    /**
     * Parses a single CSV row using the detected format config.
     * Returns null if the row should be skipped (income, empty, excluded).
     */
    private TransactionDTO parseRow(String[] row, BankFormatConfig format, String currency, int rowNum) {
        BankFormatConfig.ColumnConfig cols = format.getColumns();

        // Validate column count
        int maxCol = Math.max(cols.getDate(), Math.max(cols.getAmount(), cols.getDescription()));
        if (row.length <= maxCol) {
            throw new IllegalArgumentException(
                    "Expected at least " + (maxCol + 1) + " columns but found " + row.length +
                    ". Check the file uses comma separators.");
        }

        // Parse date
        String rawDate = row[cols.getDate()].replaceAll("\uFEFF", "").trim();
        LocalDate date = parseDate(rawDate, format.getDatePattern(), rowNum);
        if (date == null) return null;

        // Parse description
        String description = row[cols.getDescription()].trim();
        if (description.isEmpty()) {
            log.debug("Row {}: skipping empty description", rowNum);
            return null;
        }

        // Check exclude filters
        if (categorizer.isExcluded(description)) {
            log.debug("Row {}: excluded — {}", rowNum, description);
            return null;
        }

        // Parse amount
        String rawAmount = row[cols.getAmount()];
        double amount = parseAmount(rawAmount, format.isAmountSigned(), rowNum);
        if (amount <= 0) {
            log.debug("Row {}: skipping non-expense amount {}", rowNum, rawAmount);
            return null;
        }

        String category = categorizer.categorize(description);

        TransactionDTO dto = new TransactionDTO();
        dto.setDate(date);
        dto.setDescription(description);
        dto.setAmount(BigDecimal.valueOf(amount));
        dto.setCurrency(currency);
        dto.setCategory(category);
        return dto;
    }

    /**
     * Parses a date string using the format's configured pattern.
     * Throws an actionable IllegalArgumentException on failure.
     */
    private LocalDate parseDate(String raw, String datePattern, int rowNum) {
        if (raw.isEmpty()) {
            log.debug("Row {}: empty date — skipping", rowNum);
            return null;
        }
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(datePattern, Locale.ENGLISH);
            return LocalDate.parse(raw, fmt);
        } catch (DateTimeParseException e) {
            // Determine a human-readable example for the error message
            String example = datePattern.equals("dd/MM/yyyy") ? "15/01/2026" : "15 Jan 2026";
            throw new IllegalArgumentException(
                    "Date \"" + raw + "\" is not in the expected format. " +
                    "Expected " + datePattern.toUpperCase() + " (e.g. " + example + ")");
        }
    }

    /**
     * Parses and cleans an amount string.
     * - Strips quotes, commas, currency symbols
     * - If amountSigned=true (CommBank): negative = expense (keep), positive = income (skip → return 0)
     * - If amountSigned=false (HSBC): always positive, return as-is
     * - Returns Math.abs() so all stored amounts are positive expenses
     */
    private double parseAmount(String raw, boolean amountSigned, int rowNum) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        String cleaned = raw.replaceAll("[\"',\\s]", "").trim();
        // Strip leading currency symbols like $ £ €
        cleaned = cleaned.replaceAll("^[^\\d\\-.]", "");
        try {
            double value = Double.parseDouble(cleaned);
            if (amountSigned) {
                // CommBank: negative = debit/expense, positive = credit/income
                return value < 0 ? Math.abs(value) : 0;
            } else {
                // HSBC: always positive debits
                return Math.abs(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Amount \"" + raw.trim() + "\" could not be parsed. " +
                    "Remove currency symbols — expected a plain number (e.g. 45.50)");
        }
    }

    private List<String[]> readAllRows(MultipartFile file) throws IOException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            return reader.readAll();
        } catch (CsvException e) {
            throw new IOException("Failed to read CSV file: " + e.getMessage(), e);
        }
    }
}
