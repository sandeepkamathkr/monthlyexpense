package com.expense.monthly.service;

import com.expense.monthly.model.ImportFailure;
import com.expense.monthly.repository.ImportFailureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages ImportFailure records — creation, retrieval, dismissal, and download.
 *
 * When the CSV scheduler fails to process a file it:
 *   1. Renames the file to .csv.failed
 *   2. Calls recordFailure() here to persist the error in the DB
 *
 * From the Import page the user can:
 *   - Download the .csv.failed file, fix it, and re-upload via the Import UI
 *   - Dismiss the record to hide it from the panel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportFailureService {

    private final ImportFailureRepository importFailureRepository;

    @Value("${csv.scheduler.input-folder:./csv-input}")
    private String inputFolderPath;

    /**
     * Records a new failure for a file that the scheduler could not process.
     *
     * @param filePath     relative path inside the scheduler folder, e.g. "2026/May/commbank.csv.failed"
     * @param errorMessage actionable description of what went wrong
     */
    @Transactional
    public void recordFailure(String filePath, String errorMessage) {
        ImportFailure failure = new ImportFailure();
        failure.setFilePath(filePath);
        failure.setErrorMessage(errorMessage);
        failure.setFailedAt(LocalDateTime.now());
        failure.setStatus("FAILED");
        importFailureRepository.save(failure);
        log.info("Recorded import failure for: {}", filePath);
    }

    /**
     * Returns all failures with status FAILED (shown in the Import page panel).
     */
    public List<ImportFailure> listFailures() {
        return importFailureRepository.findByStatus("FAILED");
    }

    /**
     * Marks a failure as DISMISSED so it no longer appears in the Import page panel.
     * The .csv.failed file is NOT deleted — the user may still want to re-upload it.
     *
     * @param id ImportFailure record id
     */
    @Transactional
    public void dismiss(Long id) {
        ImportFailure failure = importFailureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Import failure not found: " + id));
        failure.setStatus("DISMISSED");
        importFailureRepository.save(failure);
        log.info("Dismissed import failure id={}", id);
    }

    /**
     * Reads the .csv.failed file from disk and returns its bytes for download.
     * The user downloads, fixes the CSV on their computer, then re-uploads via Import.
     *
     * @param id ImportFailure record id
     * @return raw file content
     * @throws IOException if the file cannot be read
     */
    public byte[] downloadFailedFile(Long id) throws IOException {
        ImportFailure failure = importFailureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Import failure not found: " + id));
        File file = new File(inputFolderPath, failure.getFilePath());
        if (!file.exists()) {
            throw new IllegalStateException(
                    "Failed file no longer exists on disk: " + failure.getFilePath() +
                    ". It may have been cleaned up. You can dismiss this record.");
        }
        return Files.readAllBytes(file.toPath());
    }
}
