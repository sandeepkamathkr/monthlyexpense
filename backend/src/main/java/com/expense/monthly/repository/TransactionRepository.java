package com.expense.monthly.repository;

import com.expense.monthly.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Transaction entity.
 * Provides methods for CRUD operations and custom queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific month and year.
     *
     * @param month Month (1-12)
     * @param year Year
     * @return List of transactions
     */
    List<Transaction> findByMonthAndYear(int month, int year);

    /**
     * Find all transactions for a specific year.
     *
     * @param year Year
     * @return List of transactions
     */
    List<Transaction> findByYear(int year);

    /**
     * Find all transactions by category (case insensitive).
     *
     * @param category Category name
     * @return List of transactions
     */
    List<Transaction> findByCategoryIgnoreCase(String category);

    /**
     * Find all transactions containing the given description (case insensitive).
     *
     * @param description Description to search for
     * @return List of transactions
     */
    List<Transaction> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Calculate the total amount of all transactions.
     *
     * @return Total amount
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t")
    BigDecimal calculateTotalAmount();

    /**
     * Calculate the total amount for a specific month and year.
     *
     * @param month Month (1-12)
     * @param year Year
     * @return Total amount for the month
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.month = ?1 AND t.year = ?2")
    BigDecimal calculateMonthlyTotal(int month, int year);

    /**
     * Calculate the total amount by category (case insensitive).
     *
     * @param category Category name
     * @return Total amount for the category
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE LOWER(t.category) = LOWER(?1)")
    BigDecimal calculateTotalByCategory(String category);
}
