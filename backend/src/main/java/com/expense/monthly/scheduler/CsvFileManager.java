package com.expense.monthly.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class CsvFileManager {

    static final String UNPROCESSED_DIR = "unprocessed";

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
                        log.info("Reset: {} → {}", doneFile.getName(), originalName);
                    } else {
                        log.warn("Could not reset: {}", doneFile.getPath());
                    }
                }
            }
        }
    }
}