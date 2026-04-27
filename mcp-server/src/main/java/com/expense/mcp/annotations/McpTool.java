package com.expense.mcp.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as an MCP tool that can be called by Claude.
 * The method will be registered with the MCP server and exposed to clients.
 *
 * Example usage:
 * <pre>
 * @McpTool(
 *     name = "query_expenses",
 *     description = "Query expenses by date range, category, or merchant"
 * )
 * public List&lt;Transaction&gt; queryExpenses(
 *     @McpParam(name = "startDate", required = false) LocalDate start,
 *     @McpParam(name = "endDate", required = false) LocalDate end,
 *     @McpParam(name = "category", required = false) String category
 * ) {
 *     // Implementation
 * }
 * </pre>
 *
 * Method requirements:
 * - Must be public
 * - Can have any return type (will be serialized to JSON)
 * - Parameters should be annotated with @McpParam
 * - Can throw exceptions (will be converted to MCP errors)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {

    /**
     * Tool name (unique identifier)
     * Convention: snake_case (e.g., "query_expenses", "get_category_totals")
     *
     * This is the name Claude will use to invoke the tool.
     */
    String name();

    /**
     * Human-readable description of what the tool does
     *
     * This description helps Claude understand:
     * - What the tool does
     * - When to use it
     * - What results to expect
     *
     * Be specific and include examples if helpful.
     */
    String description();

    /**
     * Optional category/group for organizing tools
     * Examples: "query", "analysis", "reporting"
     */
    String category() default "";

    /**
     * Whether this tool requires authentication
     * Default: true (inherits from server-level auth settings)
     */
    boolean requiresAuth() default true;

    /**
     * Optional examples of how to use this tool
     * Format: JSON strings showing example invocations
     */
    String[] examples() default {};
}
