package com.expense.monthly.repository;

import com.expense.monthly.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for TransactionRepository.
 * These tests interact with an actual H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testSaveAndFindTransaction() {
        // Create a test transaction
        Transaction transaction = Transaction.builder()
                .date(LocalDate.of(2023, 1, 15))
                .description("Test Transaction")
                .amount(new BigDecimal("100.00"))
                .category("Test")
                .build();
        
        // The month and year should be set automatically by the @PrePersist hook
        
        // Save the transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Verify the transaction was saved with an ID
        assertNotNull(savedTransaction.getId());
        
        // Verify the month and year were set correctly
        assertEquals(1, savedTransaction.getMonth());
        assertEquals(2023, savedTransaction.getYear());
        
        // Find the transaction by month and year
        List<Transaction> foundTransactions = transactionRepository.findByMonthAndYear(1, 2023);
        
        // Verify we found our transaction
        assertEquals(1, foundTransactions.size());
        assertEquals("Test Transaction", foundTransactions.get(0).getDescription());
    }
}