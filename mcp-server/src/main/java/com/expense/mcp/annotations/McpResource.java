package com.expense.mcp.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marks a class as an MCP resource containing tool methods.
 * Classes annotated with @McpResource will be scanned for @McpTool methods
 * and automatically registered with the MCP server.
 *
 * Example usage:
 * <pre>
 * @McpResource
 * public class ExpenseTools {
 *     @McpTool(name = "query_expenses", description = "Search expenses")
 *     public List&lt;Transaction&gt; queryExpenses(...) { ... }
 * }
 * </pre>
 *
 * This annotation is also a Spring @Component, so classes will be
 * automatically discovered by Spring's component scanning.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface McpResource {

    /**
     * Optional name for the resource
     * If not specified, the class name will be used
     */
    String value() default "";

    /**
     * Optional description of what this resource provides
     */
    String description() default "";
}
