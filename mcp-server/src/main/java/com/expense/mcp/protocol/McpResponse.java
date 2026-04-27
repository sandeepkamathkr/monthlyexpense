package com.expense.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a JSON-RPC 2.0 response from the MCP server.
 *
 * Example success response:
 * {
 *   "jsonrpc": "2.0",
 *   "id": "req-123",
 *   "result": {
 *     "content": [
 *       {
 *         "type": "text",
 *         "text": "Found 15 grocery transactions totaling $487.32"
 *       }
 *     ]
 *   }
 * }
 *
 * Example error response:
 * {
 *   "jsonrpc": "2.0",
 *   "id": "req-123",
 *   "error": {
 *     "code": -32602,
 *     "message": "Invalid params: category is required"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {

    /**
     * JSON-RPC version (must be "2.0")
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    /**
     * Request identifier (must match the request)
     */
    @JsonProperty("id")
    private Object id;

    /**
     * Result object (present on success, mutually exclusive with error)
     */
    @JsonProperty("result")
    private Object result;

    /**
     * Error object (present on failure, mutually exclusive with result)
     */
    @JsonProperty("error")
    private McpError error;

    /**
     * Create a success response
     *
     * @param id Request ID
     * @param result Result object
     * @return McpResponse with result
     */
    public static McpResponse success(Object id, Object result) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    /**
     * Create an error response
     *
     * @param id Request ID
     * @param error Error object
     * @return McpResponse with error
     */
    public static McpResponse error(Object id, McpError error) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(error)
                .build();
    }

    /**
     * Create an error response with code and message
     *
     * @param id Request ID
     * @param code Error code
     * @param message Error message
     * @return McpResponse with error
     */
    public static McpResponse error(Object id, int code, String message) {
        return error(id, new McpError(code, message, null));
    }

    /**
     * Check if this is a success response
     *
     * Note: @JsonIgnore prevents this method from being serialized as "success" field
     * in JSON output (JavaBeans convention treats isXxx() as a getter for property "xxx")
     *
     * @return true if result is present and error is null
     */
    @JsonIgnore
    public boolean isSuccess() {
        return result != null && error == null;
    }

    /**
     * Check if this is an error response
     *
     * Note: @JsonIgnore prevents this method from being serialized as "error" field conflict
     *
     * @return true if error is present
     */
    @JsonIgnore
    public boolean isError() {
        return error != null;
    }
}
