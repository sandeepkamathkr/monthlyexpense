package com.expense.monthly.service;

import com.expense.monthly.model.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvWriteService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void updateAmountInCsv(Transaction tx, BigDecimal newAmount) {
        File f = resolveFile(tx.getSourceFile());
        if (f == null) return;

        try {
            List<String[]> rows = readAll(f);
            if (rows.isEmpty()) return;

            String[] header = rows.get(0);
            int dateCol = indexOf(header, "Date");
            int descCol = indexOf(header, "Description");
            int amtCol  = indexOf(header, "Amount");
            if (dateCol < 0 || descCol < 0 || amtCol < 0) {
                log.warn("CSV missing required columns in {}", f);
                return;
            }

            String expectedDate = tx.getDate().format(DATE_FMT);
            String expectedDesc = tx.getDescription();
            BigDecimal expectedAmt = tx.getAmount();

            boolean matched = false;
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length <= Math.max(dateCol, Math.max(descCol, amtCol))) continue;
                if (row[dateCol].equals(expectedDate)
                        && row[descCol].equals(expectedDesc)
                        && amountsEqual(row[amtCol], expectedAmt)) {
                    row[amtCol] = newAmount.toPlainString();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                log.warn("No matching CSV row found for transaction {} in {}", tx.getId(), f);
                return;
            }
            writeAll(f, rows);
        } catch (IOException | CsvException e) {
            log.warn("CSV write-back failed for transaction {}: {}", tx.getId(), e.getMessage());
        }
    }

    public void removeRowFromCsv(Transaction tx) {
        File f = resolveFile(tx.getSourceFile());
        if (f == null) return;

        try {
            List<String[]> rows = readAll(f);
            if (rows.isEmpty()) return;

            String[] header = rows.get(0);
            int dateCol = indexOf(header, "Date");
            int descCol = indexOf(header, "Description");
            if (dateCol < 0 || descCol < 0) {
                log.warn("CSV missing required columns in {}", f);
                return;
            }

            String expectedDate = tx.getDate().format(DATE_FMT);
            String expectedDesc = tx.getDescription();

            List<String[]> remaining = rows.stream()
                    .filter(row -> row == header
                            || row.length <= Math.max(dateCol, descCol)
                            || !(row[dateCol].equals(expectedDate) && row[descCol].equals(expectedDesc)))
                    .collect(Collectors.toList());

            // Keep header always; add it back if stream removed it
            if (remaining.isEmpty() || remaining.get(0) != header) {
                remaining.add(0, header);
            }

            writeAll(f, remaining);
        } catch (IOException | CsvException e) {
            log.warn("CSV row removal failed for transaction {}: {}", tx.getId(), e.getMessage());
        }
    }

    private File resolveFile(String path) {
        if (path == null) {
            log.debug("sourceFile is null — skipping CSV write-back");
            return null;
        }
        File f = new File(path);
        if (f.exists()) return f;
        File done = new File(path + ".done");
        if (done.exists()) return done;
        log.warn("CSV source file not found at {} or {}.done — skipping write-back", path, path);
        return null;
    }

    private List<String[]> readAll(File f) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(f))) {
            return reader.readAll();
        }
    }

    private void writeAll(File f, List<String[]> rows) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(f))) {
            writer.writeAll(rows);
        }
    }

    private int indexOf(String[] header, String name) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private boolean amountsEqual(String csvValue, BigDecimal expected) {
        try {
            return new BigDecimal(csvValue.trim()).compareTo(expected) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
