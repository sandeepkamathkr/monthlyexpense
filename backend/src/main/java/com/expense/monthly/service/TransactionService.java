package com.expense.monthly.service;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Transaction operations.
 */
public interface TransactionService {

    /**
     * Save a single transaction.
     *
     * @param transactionDTO Transaction data
     * @return Saved transaction
     */
    Transaction saveTransaction(TransactionDTO transactionDTO);

    /**
     * Save multiple transactions.
     *
     * @param transactions List of transaction data
     * @return List of saved transactions
     */
    List<Transaction> saveTransactions(List<TransactionDTO> transactions);

    /**
     * Process and save transactions from a CSV file.
     *
     * @param file CSV file
     * @return List of saved transactions
     * @throws IOException If file processing fails
     */
    List<Transaction> processCSVFile(MultipartFile file) throws IOException;

    /**
     * Get all transactions.
     *
     * @return List of all transactions
     */
    List<Transaction> getAllTransactions();

    /**
     * Get transactions for a specific month and year.
     *
     * @param month Month (1-12)
     * @param year Year
     * @return List of transactions
     */
    List<Transaction> getTransactionsByMonth(int month, int year);

    /**
     * Get transactions by category (case insensitive).
     *
     * @param category Category name
     * @return List of transactions
     */
    List<Transaction> getTransactionsByCategory(String category);

    /**
     * Get transactions containing the given description (case insensitive).
     *
     * @param description Description to search for
     * @return List of transactions
     */
    List<Transaction> getTransactionsByDescription(String description);

    /**
     * Calculate total amount of all transactions.
     *
     * @return Total amount
     */
    BigDecimal calculateTotalAmount();

    /**
     * Calculate monthly totals for a specific year.
     *
     * @param year Year
     * @return Map of month to total amount
     */
    Map<Integer, BigDecimal> calculateMonthlyTotals(int year);

    /**
     * Calculate totals by category.
     *
     * @return Map of category to total amount
     */
    Map<String, BigDecimal> calculateTotalsByCategory();

    /**
     * Delete all transactions.
     */
    void deleteAllTransactions();

    /**
     * Executes a script to reset the H2 database (e.g., DROP ALL OBJECTS).
     */
    void executeH2ResetScript(); // New method


}
