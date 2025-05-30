package com.expense.monthly.controller;

import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for TransactionController.
 */
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void testGetAllTransactions() throws Exception {
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

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Grocery shopping")))
                .andExpect(jsonPath("$[1].description", is("Monthly rent")));

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    void testGetTransactionsByMonth() throws Exception {
        // Arrange
        int month = 1;
        int year = 2023;

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

        when(transactionService.getTransactionsByMonth(month, year)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/month")
                        .param("month", String.valueOf(month))
                        .param("year", String.valueOf(year))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Grocery shopping")))
                .andExpect(jsonPath("$[1].description", is("Monthly rent")));

        verify(transactionService, times(1)).getTransactionsByMonth(month, year);
    }

    @Test
    void testGetTransactionsByCategory() throws Exception {
        // Arrange
        String category = "Groceries";

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
                        .id(3L)
                        .date(LocalDate.of(2023, 2, 10))
                        .description("Supermarket")
                        .amount(new BigDecimal("85.75"))
                        .category("Groceries")
                        .month(2)
                        .year(2023)
                        .build()
        );

        when(transactionService.getTransactionsByCategory(category)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/category/{category}", category)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Grocery shopping")))
                .andExpect(jsonPath("$[1].description", is("Supermarket")));

        verify(transactionService, times(1)).getTransactionsByCategory(category);
    }

    @Test
    void testGetTotalAmount() throws Exception {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("1325.50");
        when(transactionService.calculateTotalAmount()).thenReturn(totalAmount);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1325.50)));

        verify(transactionService, times(1)).calculateTotalAmount();
    }

    @Test
    void testGetMonthlyTotals() throws Exception {
        // Arrange
        int year = 2023;
        Map<Integer, BigDecimal> monthlyTotals = new HashMap<>();
        monthlyTotals.put(1, new BigDecimal("1325.50"));
        monthlyTotals.put(2, new BigDecimal("1450.75"));

        when(transactionService.calculateMonthlyTotals(year)).thenReturn(monthlyTotals);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/monthly-totals")
                        .param("year", String.valueOf(year))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['1']", is(1325.50)))
                .andExpect(jsonPath("$['2']", is(1450.75)));

        verify(transactionService, times(1)).calculateMonthlyTotals(year);
    }

    @Test
    void testGetCategoryTotals() throws Exception {
        // Arrange
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        categoryTotals.put("groceries", new BigDecimal("211.25"));
        categoryTotals.put("housing", new BigDecimal("1200.00"));

        when(transactionService.calculateTotalsByCategory()).thenReturn(categoryTotals);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/category-totals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groceries", is(211.25)))
                .andExpect(jsonPath("$.housing", is(1200.00)));

        verify(transactionService, times(1)).calculateTotalsByCategory();
    }

    @Test
    void testResetData() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/transactions/reset")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("All data has been reset successfully")));

        verify(transactionService, times(1)).deleteAllTransactions();
    }
}
