package com.expense.monthly.controller;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * Upload a CSV file with transactions.
     *
     * @param file CSV file
     * @return List of processed transactions
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCSVFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("currency") String currency) {
        log.info("Received file upload request: {} with currency: {}", file.getOriginalFilename(), currency);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a CSV file to upload.");
        }

        try {
            List<Transaction> savedTransactions = transactionService.processCSVFile(file, currency);
            List<TransactionDTO> dtos = savedTransactions.stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "File processed successfully");
            response.put("transactions", dtos);
            response.put("count", dtos.size());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to process CSV file", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "UPLOAD_FAILED");
            errorResponse.put("message", "Failed to process CSV file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (IllegalArgumentException e) {
            log.error("Validation error processing file", e);
            Map<String, Object> errorResponse = new HashMap<>();
            
            String errorMessage = e.getMessage();
            if (errorMessage.startsWith("MIXED_CURRENCIES_IN_CSV:")) {
                errorResponse.put("error", "MIXED_CURRENCIES_IN_CSV");
                errorResponse.put("message", errorMessage.substring("MIXED_CURRENCIES_IN_CSV:".length()).trim());
            } else if (errorMessage.startsWith("CURRENCY_MISMATCH:")) {
                errorResponse.put("error", "CURRENCY_MISMATCH");
                errorResponse.put("message", errorMessage.substring("CURRENCY_MISMATCH:".length()).trim());
            } else if (errorMessage.startsWith("INVALID_CURRENCY:")) {
                errorResponse.put("error", "INVALID_CURRENCY");
                errorResponse.put("message", errorMessage.substring("INVALID_CURRENCY:".length()).trim());
            } else {
                errorResponse.put("error", "VALIDATION_ERROR");
                errorResponse.put("message", errorMessage);
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Error processing file", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all transactions, optionally filtered by category and/or description.
     *
     * @param category Optional category to filter by
     * @param description Optional description to filter by
     * @return List of transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description) {
        List<Transaction> transactions;

        if (category != null && !category.isEmpty() && description != null && !description.isEmpty()) {
            log.info("Fetching transactions filtered by category: {} and description containing: {}", category, description);
            // Get transactions by category first, then filter by description
            transactions = transactionService.getTransactionsByCategory(category).stream()
                    .filter(t -> t.getDescription().toLowerCase().contains(description.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (category != null && !category.isEmpty()) {
            log.info("Fetching transactions filtered by category: {}", category);
            transactions = transactionService.getTransactionsByCategory(category);
        } else if (description != null && !description.isEmpty()) {
            log.info("Fetching transactions filtered by description containing: {}", description);
            transactions = transactionService.getTransactionsByDescription(description);
        } else {
            log.info("Fetching all transactions");
            transactions = transactionService.getAllTransactions();
        }

        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get transactions for a specific month and year.
     *
     * @param month Month (1-12)
     * @param year Year (1900-2100)
     * @return List of transactions
     */
    @GetMapping("/month")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByMonth(
            @RequestParam @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") int month, 
            @RequestParam @Min(value = 1900, message = "Year must be between 1900 and 2100") @Max(value = 2100, message = "Year must be between 1900 and 2100") int year) {
        log.info("Fetching transactions for month: {}, year: {}", month, year);
        List<Transaction> transactions = transactionService.getTransactionsByMonth(month, year);
        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get transactions by category.
     *
     * @param category Category name
     * @return List of transactions
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(
            @PathVariable String category) {
        log.info("Fetching transactions for category: {}", category);
        List<Transaction> transactions = transactionService.getTransactionsByCategory(category);
        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get total amount of all transactions.
     *
     * @return Total amount
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount() {
        log.info("Calculating total amount");
        BigDecimal total = transactionService.calculateTotalAmount();
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    /**
     * Get monthly totals for a specific year.
     *
     * @param year Year (1900-2100)
     * @return Map of month to total amount
     */
    @GetMapping("/monthly-totals")
    public ResponseEntity<Map<Integer, BigDecimal>> getMonthlyTotals(
            @RequestParam @Min(value = 1900, message = "Year must be between 1900 and 2100") @Max(value = 2100, message = "Year must be between 1900 and 2100") int year) {
        log.info("Calculating monthly totals for year: {}", year);
        Map<Integer, BigDecimal> monthlyTotals = transactionService.calculateMonthlyTotals(year);
        return ResponseEntity.ok(monthlyTotals);
    }

    /**
     * Get totals by category, optionally filtered by month and year.
     *
     * @param month Optional month (1-12)
     * @param year Optional year (1900-2100)
     * @return Map of category to total amount
     */
    @GetMapping("/category-totals")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        // Manual validation for month
        if (month != null && (month < 1 || month > 12)) {
            log.warn("Invalid month parameter: {}", month);
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        
        // Manual validation for year
        if (year != null && (year < 1900 || year > 2100)) {
            log.warn("Invalid year parameter: {}", year);
            throw new IllegalArgumentException("Year must be between 1900 and 2100");
        }
        
        if (month != null && year != null) {
            log.info("Calculating totals by category for month: {}, year: {}", month, year);
        } else {
            log.info("Calculating totals by category for all transactions");
        }
        Map<String, BigDecimal> categoryTotals = transactionService.calculateTotalsByCategory(month, year);
        return ResponseEntity.ok(categoryTotals);
    }

    /**
     * Get the primary currency from existing transactions.
     *
     * @return Primary currency or null if no transactions exist
     */
    @GetMapping("/primary-currency")
    public ResponseEntity<Map<String, String>> getPrimaryCurrency() {
        log.info("Getting primary currency");
        String primaryCurrency = transactionService.getPrimaryCurrency();
        Map<String, String> response = new HashMap<>();
        response.put("primaryCurrency", primaryCurrency);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset all data.
     *
     * @return Success message
     */
    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetData() {
        log.info("Resetting all data");
        transactionService.deleteAllTransactions();
        //transactionService.executeH2ResetScript();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All data has been reset successfully");
        return ResponseEntity.ok(response);
    }
}