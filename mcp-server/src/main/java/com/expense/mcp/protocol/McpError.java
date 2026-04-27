package com.expense.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a JSON-RPC 2.0 error object.
 *
 * Standard error codes (from JSON-RPC 2.0 spec):
 * - -32700: Parse error (invalid JSON)
 * - -32600: Invalid Request
 * - -32601: Method not found
 * - -32602: Invalid params
 * - -32603: Internal error
 * - -32000 to -32099: Server error (implementation-defined)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpError {

    /**
     * Error code (integer)
     */
    @JsonProperty("code")
    private int code;

    /**
     * Error message (human-readable)
     */
    @JsonProperty("message")
    private String message;

    /**
     * Additional error data (optional)
     */
    @JsonProperty("data")
    private Object data;

    /**
     * Standard JSON-RPC 2.0 error codes
     */
    public static class ErrorCode {
        public static final int PARSE_ERROR = -32700;
        public static final int INVALID_REQUEST = -32600;
        public static final int METHOD_NOT_FOUND = -32601;
        public static final int INVALID_PARAMS = -32602;
        public static final int INTERNAL_ERROR = -32603;
        public static final int TOOL_NOT_FOUND = -32001;
        public static final int TOOL_EXECUTION_ERROR = -32002;
        public static final int AUTHENTICATION_ERROR = -32003;
    }

    /**
     * Create a parse error
     */
    public static McpError parseError(String message) {
        return new McpError(ErrorCode.PARSE_ERROR, message, null);
    }

    /**
     * Create an invalid request error
     */
    public static McpError invalidRequest(String message) {
        return new McpError(ErrorCode.INVALID_REQUEST, message, null);
    }

    /**
     * Create a method not found error
     */
    public static McpError methodNotFound(String method) {
        return new McpError(ErrorCode.METHOD_NOT_FOUND, "Method not found: " + method, null);
    }

    /**
     * Create an invalid params error
     */
    public static McpError invalidParams(String message) {
        return new McpError(ErrorCode.INVALID_PARAMS, message, null);
    }

    /**
     * Create an internal error
     */
    public static McpError internalError(String message) {
        return new McpError(ErrorCode.INTERNAL_ERROR, message, null);
    }

    /**
     * Create a tool not found error
     */
    public static McpError toolNotFound(String toolName) {
        return new McpError(ErrorCode.TOOL_NOT_FOUND, "Tool not found: " + toolName, null);
    }

    /**
     * Create a tool execution error
     */
    public static McpError toolExecutionError(String message, Object details) {
        return new McpError(ErrorCode.TOOL_EXECUTION_ERROR, message, details);
    }

    /**
     * Create an authentication error
     */
    public static McpError authenticationError(String message) {
        return new McpError(ErrorCode.AUTHENTICATION_ERROR, message, null);
    }
}
