package com.expense.monthly.controller;

import com.expense.monthly.dto.CategoryOverrideDTO;
import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Category;
import com.expense.monthly.model.ImportFailure;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.parser.BankCsvParser;
import com.expense.monthly.parser.TransactionStagingService;
import com.expense.monthly.repository.ImportFailureRepository;
import com.expense.monthly.scheduler.CsvFileManager;
import com.expense.monthly.service.CategoryOverrideService;
import com.expense.monthly.service.ExclusionRuleService;
import com.expense.monthly.service.ImportFailureService;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for transaction operations.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final BankCsvParser bankCsvParser;
    private final TransactionStagingService stagingService;
    private final ImportFailureService importFailureService;
    private final ImportFailureRepository importFailureRepository;
    private final CsvFileManager csvFileManager;
    private final CategoryOverrideService categoryOverrideService;
    private final ExclusionRuleService exclusionRuleService;

    // -------------------------------------------------------------------------
    // Existing endpoints
    // -------------------------------------------------------------------------

    /**
     * Get all transactions, optionally filtered by category and/or description.
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description) {
        List<Transaction> transactions;

        if (category != null && !category.isEmpty() && description != null && !description.isEmpty()) {
            transactions = transactionService.getTransactionsByCategory(category).stream()
                    .filter(t -> t.getDescription().toLowerCase().contains(description.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (category != null && !category.isEmpty()) {
            transactions = transactionService.getTransactionsByCategory(category);
        } else if (description != null && !description.isEmpty()) {
            transactions = transactionService.getTransactionsByDescription(description);
        } else {
            transactions = transactionService.getAllTransactions();
        }

        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get transactions for a specific month and year.
     */
    @GetMapping("/month")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByMonth(
            @RequestParam @Min(value = 1, message = "Month must be between 1 and 12")
            @Max(value = 12, message = "Month must be between 1 and 12") int month,
            @RequestParam @Min(value = 1900, message = "Year must be between 1900 and 2100")
            @Max(value = 2100, message = "Year must be between 1900 and 2100") int year) {
        List<Transaction> transactions = transactionService.getTransactionsByMonth(month, year);
        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get transactions by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(
            @PathVariable String category) {
        List<Transaction> transactions = transactionService.getTransactionsByCategory(category);
        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get total amount of all transactions.
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount() {
        BigDecimal total = transactionService.calculateTotalAmount();
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly totals for a specific year.
     */
    @GetMapping("/monthly-totals")
    public ResponseEntity<Map<Integer, BigDecimal>> getMonthlyTotals(
            @RequestParam @Min(value = 1900, message = "Year must be between 1900 and 2100")
            @Max(value = 2100, message = "Year must be between 1900 and 2100") int year) {
        Map<Integer, BigDecimal> monthlyTotals = transactionService.calculateMonthlyTotals(year);
        return ResponseEntity.ok(monthlyTotals);
    }

    /**
     * Get totals by category, optionally filtered by month and year.
     */
    @GetMapping("/category-totals")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year != null && (year < 1900 || year > 2100)) {
            throw new IllegalArgumentException("Year must be between 1900 and 2100");
        }

        Map<String, BigDecimal> categoryTotals = transactionService.calculateTotalsByCategory(month, year);
        return ResponseEntity.ok(categoryTotals);
    }

    /**
     * Get the primary currency from existing transactions.
     */
    @GetMapping("/primary-currency")
    public ResponseEntity<Map<String, String>> getPrimaryCurrency() {
        String primaryCurrency = transactionService.getPrimaryCurrency();
        Map<String, String> response = new HashMap<>();
        response.put("primaryCurrency", primaryCurrency);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset all data:
     *   - Deletes all transactions from DB
     *   - Renames all .csv.done → .csv  (requeue for reprocessing)
     *   - Renames all .csv.failed → .csv (requeue for retry)
     *   - Clears all ImportFailure records
     */
    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetData() {
        log.info("Resetting all data");
        transactionService.deleteAllTransactions();

        java.io.File inputFolder = csvFileManager.getInputFolder();
        if (inputFolder.exists()) {
            csvFileManager.resetDoneFiles(inputFolder);
            csvFileManager.resetFailedFiles(inputFolder);
        }

        importFailureRepository.deleteAll();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All data has been reset. CSV files have been re-queued for processing.");
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Import endpoints — Parse, Stage, month-count
    // -------------------------------------------------------------------------

    /**
     * Parses a raw bank CSV upload and returns categorized TransactionDTOs for review.
     * Nothing is saved to the database at this point.
     *
     * @param file     raw bank CSV (CommBank, HSBC, or any other format configured in application.yml)
     * @param currency ISO currency code, defaults to AUD
     */
    @PostMapping("/parse")
    public ResponseEntity<List<TransactionDTO>> parseBankCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "AUD") String currency) throws IOException {
        log.info("Parsing bank CSV: {} ({})", file.getOriginalFilename(), currency);
        List<TransactionDTO> transactions = bankCsvParser.parse(file, currency);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Stages reviewed transactions as a standardised CSV in the scheduler's year/month folder.
     * The existing scheduler will pick up the file within 5 minutes and save it to the DB.
     *
     * If a file for the same month already exists in the DB / scheduler folder, it is replaced:
     *   1. Old DB transactions for that month are deleted
     *   2. Old .csv / .csv.done / .csv.failed files for that month are deleted
     *   3. Old ImportFailure records for that month are cleared
     *   4. The new CSV is written
     *
     * @param filename     original filename (used in the staged filename, sanitised)
     * @param transactions reviewed list of TransactionDTOs to write
     */
    @PostMapping("/stage")
    public ResponseEntity<Map<String, String>> stageTransactions(
            @RequestParam("filename") String filename,
            @RequestParam(defaultValue = "false") boolean skipCleanup,
            @RequestBody List<TransactionDTO> transactions) throws IOException {
        if (transactions == null || transactions.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No transactions provided."));
        }
        log.info("Staging {} transactions for file: {} (skipCleanup={})", transactions.size(), filename, skipCleanup);
        String path = stagingService.stage(transactions, filename, skipCleanup);
        return ResponseEntity.ok(Map.of(
                "path", path,
                "message", "Queued at " + path + " — will be processed within seconds."
        ));
    }

    /**
     * Returns the number of transactions already in the DB for a given month/year.
     * Used by the frontend Step 3 to detect conflicts before queuing a re-import.
     */
    @GetMapping("/month-count")
    public ResponseEntity<Map<String, Integer>> getMonthCount(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(1900) @Max(2100) int year) {
        int count = transactionService.getTransactionsByMonth(month, year).size();
        return ResponseEntity.ok(Map.of("count", count));
    }

    // -------------------------------------------------------------------------
    // Import Failures endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all FAILED import records for display in the Import page panel.
     */
    @GetMapping("/import-failures")
    public ResponseEntity<List<ImportFailure>> getImportFailures() {
        return ResponseEntity.ok(importFailureService.listFailures());
    }

    /**
     * Downloads the .csv.failed file so the user can fix it on their computer and re-upload.
     */
    @GetMapping("/import-failures/{id}/download")
    public ResponseEntity<Resource> downloadFailedFile(@PathVariable Long id) throws IOException {
        ImportFailure failure = importFailureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Import failure not found: " + id));
        byte[] content = importFailureService.downloadFailedFile(id);
        String downloadName = "failed-import-" + id + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ByteArrayResource(content));
    }

    /**
     * Dismisses an import failure record (hides it from the UI panel).
     */
    @DeleteMapping("/import-failures/{id}")
    public ResponseEntity<Void> dismissImportFailure(@PathVariable Long id) {
        importFailureService.dismiss(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Category endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all known categories: the canonical list merged with any extra
     * categories that exist in the DB (e.g. categories added before this list existed).
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(Category.ALL);
    }

    /**
     * Inline edit: updates a single transaction's category in the DB and saves
     * the description→category mapping as a permanent override rule.
     */
    @PatchMapping("/{id}/category")
    public ResponseEntity<TransactionDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryOverrideDTO body) {
        Transaction updated = transactionService.updateTransactionCategory(id, body.getCategory());
        categoryOverrideService.saveOverride(updated.getDescription(), body.getCategory());
        log.info("Updated category for transaction {} to '{}' and saved override", id, body.getCategory());
        return ResponseEntity.ok(TransactionDTO.fromEntity(updated));
    }

    /**
     * Batch save of description→category overrides (called after import queue).
     */
    @PostMapping("/category-overrides")
    public ResponseEntity<Void> saveCategoryOverrides(
            @RequestBody List<CategoryOverrideDTO> overrides) {
        log.info("Saving {} category override(s)", overrides == null ? 0 : overrides.size());
        categoryOverrideService.saveOverrides(overrides);
        return ResponseEntity.noContent().build();
    }

    /**
     * Batch save of permanently excluded transaction descriptions (called after import queue).
     * On re-import of the same CSV, these rows are filtered out during parsing.
     */
    @PostMapping("/exclusion-rules")
    public ResponseEntity<Void> saveExclusionRules(@RequestBody List<String> descriptions) {
        log.info("Saving {} exclusion rule(s)", descriptions == null ? 0 : descriptions.size());
        exclusionRuleService.saveExclusions(descriptions);
        return ResponseEntity.noContent().build();
    }
}
