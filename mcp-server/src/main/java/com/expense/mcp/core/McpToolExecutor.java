package com.expense.mcp.core;

import com.expense.mcp.protocol.McpError;
import com.expense.mcp.protocol.McpToolResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Executes MCP tools using reflection.
 * Handles parameter conversion, method invocation, and result serialization.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpToolExecutor {

    private final McpToolRegistry toolRegistry;
    private final McpParameterConverter parameterConverter;
    private final ObjectMapper objectMapper;

    /**
     * Execute a tool by name with the given arguments
     *
     * @param toolName Tool name
     * @param arguments Tool arguments as a map
     * @return Tool result
     */
    public McpToolResult execute(String toolName, Map<String, Object> arguments) {
        log.debug("Executing tool: {} with arguments: {}", toolName, arguments);

        try {
            // Get registered tool
            RegisteredTool tool = toolRegistry.getTool(toolName)
                    .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolName));

            // Prepare method arguments
            Object[] methodArgs = prepareMethodArguments(tool, arguments);

            // Invoke method
            Object result = invokeMethod(tool, methodArgs);

            // Convert result to MCP format
            return convertToMcpResult(result);

        } catch (IllegalArgumentException e) {
            // Parameter validation or tool not found errors
            log.warn("Tool execution failed due to invalid arguments: {}", e.getMessage());
            return McpToolResult.error(e.getMessage());

        } catch (Exception e) {
            // Unexpected errors
            log.error("Tool execution failed: " + toolName, e);
            return McpToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }

    /**
     * Prepare method arguments from JSON arguments map
     */
    private Object[] prepareMethodArguments(RegisteredTool tool, Map<String, Object> arguments) {
        ParameterMetadata[] parameters = tool.getParameters();
        Object[] methodArgs = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            ParameterMetadata param = parameters[i];
            Object argValue = arguments != null ? arguments.get(param.getName()) : null;

            try {
                methodArgs[i] = parameterConverter.convert(argValue, param);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid parameter '" + param.getName() + "': " + e.getMessage()
                );
            }
        }

        return methodArgs;
    }

    /**
     * Invoke the tool method
     */
    private Object invokeMethod(RegisteredTool tool, Object[] methodArgs) throws Exception {
        Method method = tool.getMethod();
        Object instance = tool.getInstance();

        // Make method accessible if needed
        if (!method.canAccess(instance)) {
            method.setAccessible(true);
        }

        return method.invoke(instance, methodArgs);
    }

    /**
     * Convert method result to MCP tool result format
     */
    private McpToolResult convertToMcpResult(Object result) {
        if (result == null) {
            return McpToolResult.text("Operation completed successfully (no result)");
        }

        // If result is already a McpToolResult, return it
        if (result instanceof McpToolResult) {
            return (McpToolResult) result;
        }

        // If result is a string, return as text
        if (result instanceof String) {
            return McpToolResult.text((String) result);
        }

        // For other types, serialize to JSON
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
            return McpToolResult.text(json);
        } catch (Exception e) {
            log.error("Failed to serialize tool result to JSON", e);
            return McpToolResult.text(result.toString());
        }
    }

    /**
     * Validate tool arguments before execution
     * Returns an error if validation fails, null if valid
     */
    public McpError validateArguments(String toolName, Map<String, Object> arguments) {
        try {
            RegisteredTool tool = toolRegistry.getTool(toolName)
                    .orElse(null);

            if (tool == null) {
                return McpError.toolNotFound(toolName);
            }

            // Try to prepare arguments (will throw if invalid)
            prepareMethodArguments(tool, arguments);

            return null; // Valid

        } catch (IllegalArgumentException e) {
            return McpError.invalidParams(e.getMessage());
        } catch (Exception e) {
            return McpError.internalError("Validation failed: " + e.getMessage());
        }
    }
}
