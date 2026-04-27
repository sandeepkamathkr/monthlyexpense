package com.expense.mcp.tools;

import com.expense.mcp.annotations.McpParam;
import com.expense.mcp.annotations.McpResource;
import com.expense.mcp.annotations.McpTool;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tools for querying and analyzing expenses.
 * Reuses existing TransactionService from the backend module.
 */
@McpResource(description = "Tools for querying and analyzing monthly expenses")
@Slf4j
@RequiredArgsConstructor
public class ExpenseTools {

    private final TransactionService transactionService;

    /**
     * Query expenses with various filters.
     * Returns transactions matching the specified criteria.
     */
    @McpTool(
            name = "query_expenses",
            description = "Query expenses by date range, category, description, or amount range. " +
                    "All parameters are optional - use any combination to filter results."
    )
    public Map<String, Object> queryExpenses(
            @McpParam(
                    name = "startDate",
                    description = "Start date in YYYY-MM-DD format (e.g., '2025-01-01')",
                    format = "date",
                    required = false
            ) LocalDate startDate,

            @McpParam(
                    name = "endDate",
                    description = "End date in YYYY-MM-DD format (e.g., '2025-12-31')",
                    format = "date",
                    required = false
            ) LocalDate endDate,

            @McpParam(
                    name = "category",
                    description = "Expense category (e.g., 'Groceries', 'Bills', 'Transport')",
                    required = false
            ) String category,

            @McpParam(
                    name = "description",
                    description = "Search text to find in transaction descriptions (case-insensitive)",
                    required = false
            ) String description,

            @McpParam(
                    name = "minAmount",
                    description = "Minimum transaction amount",
                    required = false
            ) BigDecimal minAmount,

            @McpParam(
                    name = "maxAmount",
                    description = "Maximum transaction amount",
                    required = false
            ) BigDecimal maxAmount,

            @McpParam(
                    name = "month",
                    description = "Month (1-12)",
                    min = "1",
                    max = "12",
                    required = false
            ) Integer month,

            @McpParam(
                    name = "year",
                    description = "Year (e.g., 2025)",
                    min = "2000",
                    max = "2100",
                    required = false
            ) Integer year
    ) {
        log.info("Query expenses: startDate={}, endDate={}, category={}, description={}, month={}, year={}",
                startDate, endDate, category, description, month, year);

        // Start with all transactions or filter by month/year
        List<Transaction> transactions;
        if (month != null && year != null) {
            transactions = transactionService.getTransactionsByMonth(month, year);
        } else {
            transactions = transactionService.getAllTransactions();
        }

        // Apply filters
        if (category != null && !category.isEmpty()) {
            String categoryLower = category.toLowerCase();
            transactions = transactions.stream()
                    .filter(t -> t.getCategory().toLowerCase().equals(categoryLower))
                    .collect(Collectors.toList());
        }

        if (description != null && !description.isEmpty()) {
            String descLower = description.toLowerCase();
            transactions = transactions.stream()
                    .filter(t -> t.getDescription().toLowerCase().contains(descLower))
                    .collect(Collectors.toList());
        }

        if (startDate != null) {
            transactions = transactions.stream()
                    .filter(t -> !t.getDate().isBefore(startDate))
                    .collect(Collectors.toList());
        }

        if (endDate != null) {
            transactions = transactions.stream()
                    .filter(t -> !t.getDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        if (minAmount != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getAmount().compareTo(minAmount) >= 0)
                    .collect(Collectors.toList());
        }

        if (maxAmount != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getAmount().compareTo(maxAmount) <= 0)
                    .collect(Collectors.toList());
        }

        // Calculate total
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("count", transactions.size());
        result.put("total", total);
        result.put("transactions", transactions);

        return result;
    }

    /**
     * Get spending totals grouped by category.
     */
    @McpTool(
            name = "get_category_totals",
            description = "Get total spending by category. Optionally filter by month and year. " +
                    "Returns a breakdown showing how much was spent in each category."
    )
    public Map<String, Object> getCategoryTotals(
            @McpParam(
                    name = "month",
                    description = "Month (1-12). If provided, requires year as well.",
                    min = "1",
                    max = "12",
                    required = false
            ) Integer month,

            @McpParam(
                    name = "year",
                    description = "Year (e.g., 2025)",
                    min = "2000",
                    max = "2100",
                    required = false
            ) Integer year
    ) {
        log.info("Get category totals: month={}, year={}", month, year);

        Map<String, BigDecimal> categoryTotals = transactionService.calculateTotalsByCategory(month, year);

        // Sort by amount (descending)
        List<Map.Entry<String, BigDecimal>> sortedEntries = categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // Calculate grand total
        BigDecimal grandTotal = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("categoryTotals", categoryTotals);
        result.put("sortedCategories", sortedEntries.stream()
                .map(e -> Map.of("category", e.getKey(), "amount", e.getValue()))
                .collect(Collectors.toList()));
        result.put("grandTotal", grandTotal);
        result.put("categoryCount", categoryTotals.size());

        return result;
    }

    /**
     * Get monthly spending summary for a specific year.
     */
    @McpTool(
            name = "get_monthly_summary",
            description = "Get total spending for each month in a specific year. " +
                    "Returns a breakdown by month (1-12) with totals."
    )
    public Map<String, Object> getMonthlySummary(
            @McpParam(
                    name = "year",
                    description = "Year to analyze (e.g., 2025)",
                    min = "2000",
                    max = "2100",
                    required = true
            ) Integer year
    ) {
        log.info("Get monthly summary for year: {}", year);

        Map<Integer, BigDecimal> monthlyTotals = transactionService.calculateMonthlyTotals(year);

        // Calculate yearly total
        BigDecimal yearTotal = monthlyTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average monthly spending
        BigDecimal avgMonthly = monthlyTotals.isEmpty()
                ? BigDecimal.ZERO
                : yearTotal.divide(BigDecimal.valueOf(monthlyTotals.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Find highest spending month
        Map.Entry<Integer, BigDecimal> highestMonth = monthlyTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("monthlyTotals", monthlyTotals);
        result.put("yearTotal", yearTotal);
        result.put("avgMonthly", avgMonthly);
        if (highestMonth != null) {
            result.put("highestSpendingMonth", Map.of(
                    "month", highestMonth.getKey(),
                    "amount", highestMonth.getValue()
            ));
        }

        return result;
    }

    /**
     * Get total spending across all transactions.
     */
    @McpTool(
            name = "get_total_spending",
            description = "Get the total amount of all expenses in the database. " +
                    "Returns overall spending statistics."
    )
    public Map<String, Object> getTotalSpending() {
        log.info("Get total spending");

        BigDecimal total = transactionService.calculateTotalAmount();
        List<Transaction> allTransactions = transactionService.getAllTransactions();

        // Calculate average transaction amount
        BigDecimal avgAmount = allTransactions.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(allTransactions.size()), 2, BigDecimal.ROUND_HALF_UP);

        // Find largest transaction
        Transaction largestTransaction = allTransactions.stream()
                .max((t1, t2) -> t1.getAmount().compareTo(t2.getAmount()))
                .orElse(null);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", total);
        result.put("transactionCount", allTransactions.size());
        result.put("averageAmount", avgAmount);
        if (largestTransaction != null) {
            result.put("largestTransaction", Map.of(
                    "amount", largestTransaction.getAmount(),
                    "description", largestTransaction.getDescription(),
                    "category", largestTransaction.getCategory(),
                    "date", largestTransaction.getDate()
            ));
        }

        return result;
    }

    /**
     * Search transactions by description text.
     */
    @McpTool(
            name = "search_transactions",
            description = "Full-text search on transaction descriptions. " +
                    "Returns all transactions containing the search text (case-insensitive)."
    )
    public Map<String, Object> searchTransactions(
            @McpParam(
                    name = "searchText",
                    description = "Text to search for in transaction descriptions",
                    required = true
            ) String searchText
    ) {
        log.info("Search transactions: searchText={}", searchText);

        List<Transaction> transactions = transactionService.getTransactionsByDescription(searchText);

        // Calculate total of matching transactions
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("searchText", searchText);
        result.put("count", transactions.size());
        result.put("total", total);
        result.put("transactions", transactions);

        return result;
    }

    /**
     * Get primary currency used in the database.
     */
    @McpTool(
            name = "get_primary_currency",
            description = "Get the primary currency code used for transactions (e.g., 'USD', 'EUR', 'GBP')."
    )
    public Map<String, Object> getPrimaryCurrency() {
        log.info("Get primary currency");

        String primaryCurrency = transactionService.getPrimaryCurrency();

        Map<String, Object> result = new HashMap<>();
        result.put("currency", primaryCurrency);

        return result;
    }
}
