package com.expense.monthly.service;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 */
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTransaction() {
        // Arrange
        TransactionDTO dto = new TransactionDTO(
                LocalDate.of(2023, 1, 15),
                "Grocery shopping",
                new BigDecimal("125.50"),
                "Groceries"
        );

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .date(dto.getDate())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .month(1)
                .year(2023)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = transactionService.saveTransaction(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(dto.getDate(), result.getDate());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getCategory(), result.getCategory());
        assertEquals(1, result.getMonth());
        assertEquals(2023, result.getYear());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testSaveTransactions() {
        // Arrange
        List<TransactionDTO> dtos = Arrays.asList(
                new TransactionDTO(
                        LocalDate.of(2023, 1, 15),
                        "Grocery shopping",
                        new BigDecimal("125.50"),
                        "Groceries"
                ),
                new TransactionDTO(
                        LocalDate.of(2023, 1, 20),
                        "Monthly rent",
                        new BigDecimal("1200.00"),
                        "Housing"
                )
        );

        List<Transaction> savedTransactions = Arrays.asList(
                Transaction.builder()
                        .id(1L)
                        .date(dtos.get(0).getDate())
                        .description(dtos.get(0).getDescription())
                        .amount(dtos.get(0).getAmount())
                        .category(dtos.get(0).getCategory())
                        .month(1)
                        .year(2023)
                        .build(),
                Transaction.builder()
                        .id(2L)
                        .date(dtos.get(1).getDate())
                        .description(dtos.get(1).getDescription())
                        .amount(dtos.get(1).getAmount())
                        .category(dtos.get(1).getCategory())
                        .month(1)
                        .year(2023)
                        .build()
        );

        when(transactionRepository.saveAll(anyList())).thenReturn(savedTransactions);

        // Act
        List<Transaction> results = transactionService.saveTransactions(dtos);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());

        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetAllTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(
                Transaction.builder()
                        .id(1L)
                        .date(LocalDate.of(2023, 1, 15))
                        .description("Grocery shopping")
                        .amount(new BigDecimal("125.50"))
                        .category("Groceries")
                        .month(1)
                        .year(2023)
                        .build(),
                Transaction.builder()
                        .id(2L)
                        .date(LocalDate.of(2023, 1, 20))
                        .description("Monthly rent")
                        .amount(new BigDecimal("1200.00"))
                        .category("Housing")
                        .month(1)
                        .year(2023)
                        .build()
        );

        when(transactionRepository.findAll()).thenReturn(transactions);

        // Act
        List<Transaction> results = transactionService.getAllTransactions();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testCalculateTotalAmount() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("1325.50");
        when(transactionRepository.calculateTotalAmount()).thenReturn(expectedTotal);

        // Act
        BigDecimal result = transactionService.calculateTotalAmount();

        // Assert
        assertEquals(expectedTotal, result);
        verify(transactionRepository, times(1)).calculateTotalAmount();
    }

    @Test
    void testCalculateMonthlyTotals() {
        // Arrange
        int year = 2023;
        BigDecimal januaryTotal = new BigDecimal("1325.50");
        BigDecimal februaryTotal = new BigDecimal("1450.75");

        when(transactionRepository.calculateMonthlyTotal(1, year)).thenReturn(januaryTotal);
        when(transactionRepository.calculateMonthlyTotal(2, year)).thenReturn(februaryTotal);
        // Other months return null

        // Act
        Map<Integer, BigDecimal> results = transactionService.calculateMonthlyTotals(year);

        // Assert
        assertNotNull(results);
        assertEquals(12, results.size()); // Should have entries for all 12 months
        assertEquals(januaryTotal, results.get(1));
        assertEquals(februaryTotal, results.get(2));
        assertEquals(BigDecimal.ZERO, results.get(3)); // Other months should be zero

        verify(transactionRepository, times(12)).calculateMonthlyTotal(anyInt(), eq(year));
    }

    @Test
    void testDeleteAllTransactions() {
        // Act
        transactionService.deleteAllTransactions();

        // Assert
        verify(transactionRepository, times(1)).deleteAll();
    }
}