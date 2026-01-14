package com.expense.mcp.annotations;

import java.lang.annotation.*;

/**
 * Describes a parameter for an @McpTool method.
 * Provides metadata for parameter validation and documentation.
 *
 * Example usage:
 * <pre>
 * @McpTool(name = "query_expenses", description = "Search expenses")
 * public List&lt;Transaction&gt; queryExpenses(
 *     @McpParam(
 *         name = "category",
 *         description = "Expense category (e.g., Groceries, Bills)",
 *         required = false
 *     ) String category,
 *
 *     @McpParam(
 *         name = "minAmount",
 *         description = "Minimum transaction amount",
 *         required = false,
 *         defaultValue = "0"
 *     ) BigDecimal minAmount
 * ) {
 *     // Implementation
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpParam {

    /**
     * Parameter name as it appears in the MCP request
     * Convention: camelCase (e.g., "startDate", "category", "minAmount")
     *
     * If not specified, the actual parameter name from the method signature
     * will be used (requires -parameters compiler flag)
     */
    String name() default "";

    /**
     * Human-readable description of the parameter
     *
     * Helps Claude understand:
     * - What the parameter is for
     * - What values are acceptable
     * - Format requirements (e.g., "YYYY-MM-DD")
     */
    String description() default "";

    /**
     * Whether this parameter is required
     * Default: false (optional)
     *
     * If true and the parameter is missing from the request,
     * an invalid params error will be returned.
     */
    boolean required() default false;

    /**
     * Default value if parameter is not provided
     * Format: String representation that will be parsed to the parameter type
     *
     * Examples:
     * - "0" for numeric types
     * - "true" for boolean
     * - "2025-01-01" for dates
     * - "" for empty string
     */
    String defaultValue() default "";

    /**
     * Optional regex pattern for string validation
     * Example: "\\d{4}-\\d{2}-\\d{2}" for date validation
     */
    String pattern() default "";

    /**
     * Minimum value (for numeric parameters)
     * Example: "0" for non-negative numbers
     */
    String min() default "";

    /**
     * Maximum value (for numeric parameters)
     * Example: "12" for month parameter
     */
    String max() default "";

    /**
     * Allowed enum values (for string parameters)
     * Example: {"Groceries", "Bills", "Transport"}
     */
    String[] allowedValues() default {};

    /**
     * Format hint for parameter parsing
     * Examples: "date", "date-time", "email", "uri"
     *
     * Used for:
     * - Better documentation
     * - Client-side validation hints
     * - Parsing guidance
     */
    String format() default "";
}
