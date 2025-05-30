package com.expense.monthly.controller;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Upload a CSV file with transactions.
     *
     * @param file CSV file
     * @return List of processed transactions
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCSVFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a CSV file to upload.");
        }

        try {
            List<Transaction> savedTransactions = transactionService.processCSVFile(file);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process CSV file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Get all transactions.
     *
     * @return List of all transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        log.info("Fetching all transactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<TransactionDTO> dtos = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get transactions for a specific month and year.
     *
     * @param month Month (1-12)
     * @param year Year
     * @return List of transactions
     */
    @GetMapping("/month")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByMonth(
            @RequestParam int month, @RequestParam int year) {
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
     * @param year Year
     * @return Map of month to total amount
     */
    @GetMapping("/monthly-totals")
    public ResponseEntity<Map<Integer, BigDecimal>> getMonthlyTotals(@RequestParam int year) {
        log.info("Calculating monthly totals for year: {}", year);
        Map<Integer, BigDecimal> monthlyTotals = transactionService.calculateMonthlyTotals(year);
        return ResponseEntity.ok(monthlyTotals);
    }

    /**
     * Get totals by category.
     *
     * @return Map of category to total amount
     */
    @GetMapping("/category-totals")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals() {
        log.info("Calculating totals by category");
        Map<String, BigDecimal> categoryTotals = transactionService.calculateTotalsByCategory();
        return ResponseEntity.ok(categoryTotals);
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
        Map<String, String> response = new HashMap<>();
        response.put("message", "All data has been reset successfully");
        return ResponseEntity.ok(response);
    }
}