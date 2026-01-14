package com.expense.mcp.transport;

import com.expense.mcp.config.McpServerConfig;
import com.expense.mcp.protocol.McpError;
import com.expense.mcp.protocol.McpRequest;
import com.expense.mcp.protocol.McpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP controller for MCP protocol.
 * Provides a simple HTTP POST endpoint for MCP requests.
 *
 * Endpoint: POST /api/mcp
 * Content-Type: application/json
 *
 * Example request:
 * POST http://localhost:8082/api/mcp
 * {
 *   "jsonrpc": "2.0",
 *   "id": "req-1",
 *   "method": "tools/list",
 *   "params": {}
 * }
 */
@RestController
@RequestMapping("${mcp.server.http.path:/api/mcp}")
@ConditionalOnProperty(prefix = "mcp.server.http", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class McpHttpController {

    private final McpRequestHandler requestHandler;
    private final McpServerConfig serverConfig;

    /**
     * Handle MCP requests via HTTP POST
     *
     * @param request MCP request
     * @return MCP response
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<McpResponse> handleRequest(@RequestBody McpRequest request) {
        log.debug("Received HTTP MCP request: method={}, id={}", request.getMethod(), request.getId());

        try {
            McpResponse response = requestHandler.handleRequest(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error handling HTTP MCP request", e);

            McpResponse errorResponse = McpResponse.error(
                    request.getId(),
                    McpError.internalError("Request handling failed: " + e.getMessage())
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MCP HTTP endpoint is healthy");
    }
}
