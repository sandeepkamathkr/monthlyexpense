package com.expense.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a JSON-RPC 2.0 request from the MCP client (Claude).
 *
 * Example request:
 * {
 *   "jsonrpc": "2.0",
 *   "id": "req-123",
 *   "method": "tools/call",
 *   "params": {
 *     "name": "query_expenses",
 *     "arguments": {
 *       "category": "Groceries",
 *       "month": 12
 *     }
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpRequest {

    /**
     * JSON-RPC version (must be "2.0")
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    /**
     * Request identifier (can be string or number)
     * Used to match requests with responses
     */
    @JsonProperty("id")
    private Object id;

    /**
     * Method name being called
     * MCP methods: "initialize", "tools/list", "tools/call", "resources/list", etc.
     */
    @JsonProperty("method")
    private String method;

    /**
     * Method parameters as a map
     * Structure depends on the method being called
     */
    @JsonProperty("params")
    private Map<String, Object> params;

    /**
     * Get a typed parameter value from the params map
     *
     * @param key Parameter name
     * @param type Expected type
     * @return Parameter value cast to the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, Class<T> type) {
        Object value = params != null ? params.get(key) : null;
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    /**
     * Get a parameter value as a String
     *
     * @param key Parameter name
     * @return Parameter value as String, or null if not found
     */
    public String getParamAsString(String key) {
        Object value = params != null ? params.get(key) : null;
        return value != null ? value.toString() : null;
    }

    /**
     * Check if this is a tool call request
     *
     * @return true if method is "tools/call"
     */
    public boolean isToolCall() {
        return "tools/call".equals(method);
    }

    /**
     * Check if this is a tools list request
     *
     * @return true if method is "tools/list"
     */
    public boolean isToolsList() {
        return "tools/list".equals(method);
    }

    /**
     * Check if this is an initialize request
     *
     * @return true if method is "initialize"
     */
    public boolean isInitialize() {
        return "initialize".equals(method);
    }

    /**
     * Check if this is a notification (one-way message, no response expected)
     *
     * In JSON-RPC 2.0:
     * - Requests have an "id" field (require response)
     * - Notifications have NO "id" field (no response needed)
     *
     * @return true if this is a notification
     */
    public boolean isNotification() {
        return id == null;
    }

    /**
     * Check if method is a notification method (starts with "notifications/")
     *
     * @return true if method starts with "notifications/"
     */
    public boolean isNotificationMethod() {
        return method != null && method.startsWith("notifications/");
    }
}
