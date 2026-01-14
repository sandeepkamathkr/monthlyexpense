package com.expense.mcp.transport;

import com.expense.mcp.core.McpToolExecutor;
import com.expense.mcp.core.McpToolRegistry;
import com.expense.mcp.protocol.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles MCP requests and generates responses.
 * Used by both WebSocket and HTTP transport layers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpRequestHandler {

    private final McpToolRegistry toolRegistry;
    private final McpToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    /**
     * Handle an MCP request and generate a response
     *
     * @param request MCP request
     * @return MCP response, or null for notifications (no response needed)
     */
    public McpResponse handleRequest(McpRequest request) {
        log.debug("Handling MCP request: method={}, id={}", request.getMethod(), request.getId());

        try {
            // Handle notifications (no response required)
            if (request.isNotification() || request.isNotificationMethod()) {
                handleNotification(request);
                return null; // No response for notifications
            }

            // Route to appropriate handler based on method
            if (request.isInitialize()) {
                return handleInitialize(request);
            } else if (request.isToolsList()) {
                return handleToolsList(request);
            } else if (request.isToolCall()) {
                return handleToolCall(request);
            } else {
                return McpResponse.error(
                        request.getId(),
                        McpError.methodNotFound(request.getMethod())
                );
            }

        } catch (Exception e) {
            log.error("Error handling MCP request", e);
            return McpResponse.error(
                    request.getId(),
                    McpError.internalError("Request handling failed: " + e.getMessage())
            );
        }
    }

    /**
     * Handle MCP notifications (one-way messages, no response)
     *
     * Common notifications:
     * - notifications/initialized - Client finished initialization
     * - notifications/cancelled - Client cancelled a request
     * - notifications/progress - Progress update
     *
     * @param request Notification request
     */
    private void handleNotification(McpRequest request) {
        log.debug("Received notification: {}", request.getMethod());

        // Handle specific notifications if needed
        switch (request.getMethod()) {
            case "notifications/initialized":
                log.info("Client initialization complete");
                break;
            case "notifications/cancelled":
                log.debug("Client cancelled request");
                break;
            case "notifications/progress":
                log.debug("Progress notification received");
                break;
            default:
                log.debug("Unknown notification: {}", request.getMethod());
        }
    }

    /**
     * Handle initialize request
     * Returns server capabilities and information
     */
    private McpResponse handleInitialize(McpRequest request) {
        log.info("Handling initialize request");

        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        result.put("serverInfo", Map.of(
                "name", "monthly-expense-mcp-server",
                "version", "1.0.0",
                "description", "MCP server for Monthly Expense Tracker - AI-assisted expense analysis"
        ));
        result.put("capabilities", Map.of(
                "tools", Map.of("supported", true),
                "resources", Map.of("supported", false),
                "prompts", Map.of("supported", false)
        ));

        return McpResponse.success(request.getId(), result);
    }

    /**
     * Handle tools/list request
     * Returns list of available tools
     */
    private McpResponse handleToolsList(McpRequest request) {
        log.info("Handling tools/list request");

        List<McpToolDefinition> tools = toolRegistry.getAllToolDefinitions();

        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);

        return McpResponse.success(request.getId(), result);
    }

    /**
     * Handle tools/call request
     * Executes a tool and returns the result
     */
    @SuppressWarnings("unchecked")
    private McpResponse handleToolCall(McpRequest request) {
        Map<String, Object> params = request.getParams();
        if (params == null) {
            return McpResponse.error(
                    request.getId(),
                    McpError.invalidParams("Missing params")
            );
        }

        String toolName = (String) params.get("name");
        if (toolName == null || toolName.isEmpty()) {
            return McpResponse.error(
                    request.getId(),
                    McpError.invalidParams("Missing tool name")
            );
        }

        Map<String, Object> arguments = params.containsKey("arguments")
                ? (Map<String, Object>) params.get("arguments")
                : new HashMap<>();

        log.info("Executing tool: {} with arguments: {}", toolName, arguments);

        // Check if tool exists
        if (!toolRegistry.hasTool(toolName)) {
            return McpResponse.error(
                    request.getId(),
                    McpError.toolNotFound(toolName)
            );
        }

        // Execute tool
        McpToolResult toolResult = toolExecutor.execute(toolName, arguments);

        // Build response
        return McpResponse.success(request.getId(), toolResult);
    }

    /**
     * Parse JSON string to McpRequest
     *
     * @param json JSON string
     * @return Parsed request
     * @throws Exception if parsing fails
     */
    public McpRequest parseRequest(String json) throws Exception {
        return objectMapper.readValue(json, McpRequest.class);
    }

    /**
     * Serialize McpResponse to JSON string
     *
     * @param response Response object
     * @return JSON string
     * @throws Exception if serialization fails
     */
    public String serializeResponse(McpResponse response) throws Exception {
        return objectMapper.writeValueAsString(response);
    }
}
