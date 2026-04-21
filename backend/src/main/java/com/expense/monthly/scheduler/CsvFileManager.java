package com.expense.monthly.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility component for file operations within the CSV scheduler input folder.
 *
 * Folder convention:
 *   {input-folder}/{year}/{month}/{name}.csv        — pending
 *   {input-folder}/{year}/{month}/{name}.csv.done   — processed successfully
 *   {input-folder}/{year}/{month}/{name}.csv.failed — failed; visible in Import page
 */
@Component
@Slf4j
public class CsvFileManager {

    static final Set<String> VALID_MONTHS = new HashSet<>(Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ));

    @Value("${csv.scheduler.input-folder:/app/csv-input}")
    private String inputFolderPath;

    public File getInputFolder() {
        return new File(inputFolderPath);
    }

    public boolean isValidYear(String name) {
        if (!name.matches("\\d{4}")) return false;
        int year = Integer.parseInt(name);
        return year >= 2000 && year <= 2099;
    }

    public boolean isValidMonth(String name) {
        return VALID_MONTHS.contains(name);
    }

    /**
     * Renames all .csv.done files back to .csv so the scheduler will reprocess them.
     * Called by the Reset endpoint to requeue all previously imported files.
     */
    public void resetDoneFiles(File inputFolder) {
        File[] yearFolders = inputFolder.listFiles(f -> f.isDirectory() && isValidYear(f.getName()));
        if (yearFolders == null) return;

        for (File yearFolder : yearFolders) {
            File[] monthFolders = yearFolder.listFiles(f -> f.isDirectory() && isValidMonth(f.getName()));
            if (monthFolders == null) continue;

            for (File monthFolder : monthFolders) {
                File[] doneFiles = monthFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv.done"));
                if (doneFiles == null) continue;

                for (File doneFile : doneFiles) {
                    String originalName = doneFile.getName().replaceAll("\\.done$", "");
                    File csvFile = new File(doneFile.getParentFile(), originalName);
                    if (doneFile.renameTo(csvFile)) {
                        log.info("Reset: {} \u2192 {}", doneFile.getName(), originalName);
                    } else {
                        log.warn("Could not reset: {}", doneFile.getPath());
                    }
                }
            }
        }
    }

    /**
     * Renames all .csv.failed files back to .csv so the scheduler will retry them.
     * Called by the Reset endpoint to requeue all previously failed files.
     */
    public void resetFailedFiles(File inputFolder) {
        File[] yearFolders = inputFolder.listFiles(f -> f.isDirectory() && isValidYear(f.getName()));
        if (yearFolders == null) return;

        for (File yearFolder : yearFolders) {
            File[] monthFolders = yearFolder.listFiles(f -> f.isDirectory() && isValidMonth(f.getName()));
            if (monthFolders == null) continue;

            for (File monthFolder : monthFolders) {
                File[] failedFiles = monthFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv.failed"));
                if (failedFiles == null) continue;

                for (File failedFile : failedFiles) {
                    String originalName = failedFile.getName().replaceAll("\\.failed$", "");
                    File csvFile = new File(failedFile.getParentFile(), originalName);
                    if (failedFile.renameTo(csvFile)) {
                        log.info("Reset failed: {} \u2192 {}", failedFile.getName(), originalName);
                    } else {
                        log.warn("Could not reset failed file: {}", failedFile.getPath());
                    }
                }
            }
        }
    }

    /**
     * Deletes ALL .csv, .csv.done, and .csv.failed files in a specific year/month folder.
     * Called during re-import staging so old processed files don't conflict with the new upload.
     *
     * @param year      four-digit year string, e.g. "2026"
     * @param monthAbbr three-letter month abbreviation, e.g. "Jan"
     */
    public void deleteMonthFiles(String year, String monthAbbr) {
        File monthFolder = new File(getInputFolder(), year + File.separator + monthAbbr);
        if (!monthFolder.exists()) return;

        File[] files = monthFolder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".csv") || lower.endsWith(".csv.done") || lower.endsWith(".csv.failed");
        });

        if (files == null) return;
        for (File f : files) {
            if (f.delete()) {
                log.info("Deleted month file: {}/{}/{}", year, monthAbbr, f.getName());
            } else {
                log.warn("Could not delete: {}/{}/{}", year, monthAbbr, f.getName());
            }
        }
    }
}
