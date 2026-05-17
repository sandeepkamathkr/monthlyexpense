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

    List<Transaction> findByMonthAndYear(int month, int year);
    List<Transaction> findByYear(int year);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByMonthAndYear(int month, int year);

    List<Transaction> findByCategoryIgnoreCase(String category);
    List<Transaction> findByDescriptionContainingIgnoreCase(String description);

    // Excluded-false variants used by all read queries
    List<Transaction> findByExcludedFalse();
    List<Transaction> findByMonthAndYearAndExcludedFalse(int month, int year);
    List<Transaction> findByYearAndExcludedFalse(int year);
    List<Transaction> findByCategoryIgnoreCaseAndExcludedFalse(String category);
    List<Transaction> findByDescriptionContainingIgnoreCaseAndExcludedFalse(String description);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.excluded = false")
    BigDecimal calculateTotalAmount();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.month = ?1 AND t.year = ?2 AND t.excluded = false")
    BigDecimal calculateMonthlyTotal(int month, int year);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE LOWER(t.category) = LOWER(?1) AND t.excluded = false")
    BigDecimal calculateTotalByCategory(String category);
}
