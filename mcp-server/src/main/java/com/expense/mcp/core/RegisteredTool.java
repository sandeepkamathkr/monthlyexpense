package com.expense.mcp.core;

import com.expense.mcp.protocol.McpToolDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * Represents a registered MCP tool with its metadata.
 * Combines the tool definition (for clients) with execution metadata (for the server).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredTool {

    /**
     * Tool definition (sent to clients)
     */
    private McpToolDefinition definition;

    /**
     * The object instance containing the tool method
     */
    private Object instance;

    /**
     * The method to invoke when the tool is called
     */
    private Method method;

    /**
     * Parameter metadata for validation and conversion
     */
    private ParameterMetadata[] parameters;

    /**
     * Tool category (for organization)
     */
    private String category;

    /**
     * Whether this tool requires authentication
     */
    private boolean requiresAuth;

    /**
     * Get the tool name
     */
    public String getName() {
        return definition.getName();
    }

    /**
     * Get the tool description
     */
    public String getDescription() {
        return definition.getDescription();
    }
}
