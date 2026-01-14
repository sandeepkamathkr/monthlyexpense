package com.expense.mcp.transport;

import com.expense.mcp.protocol.McpRequest;
import com.expense.mcp.protocol.McpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stdio transport implementation for MCP protocol.
 *
 * This transport layer handles communication with Claude Desktop via stdin/stdout:
 * - Reads JSON-RPC requests from System.in (stdin) line-by-line
 * - Writes JSON-RPC responses to System.out (stdout) as pure JSON
 * - Logs are redirected to System.err to keep stdout clean
 *
 * Communication Flow:
 * 1. Claude Desktop writes JSON-RPC to our stdin
 * 2. We read line-by-line (blocking)
 * 3. Parse JSON → McpRequest
 * 4. Pass to McpRequestHandler for processing
 * 5. Serialize response → JSON
 * 6. Write to stdout (flush immediately)
 * 7. Claude Desktop reads from our stdout
 *
 * Thread Safety:
 * - Single-threaded: Main thread blocks on stdin read
 * - stdout writes are synchronized via PrintWriter
 * - Shutdown flag is atomic for safe termination
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpStdioTransport {

    private final McpRequestHandler requestHandler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private BufferedReader stdin;
    private PrintWriter stdout;

    /**
     * Start the stdio transport loop.
     * This method blocks until shutdown() is called or stdin is closed.
     *
     * @throws Exception if initialization or I/O fails
     */
    public void start() throws Exception {
        if (!running.compareAndSet(false, true)) {
            log.warn("Stdio transport already running");
            return;
        }

        log.info("Starting MCP Stdio Transport...");

        // Initialize stdin/stdout streams
        stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        stdout = new PrintWriter(System.out, true, StandardCharsets.UTF_8); // Auto-flush enabled

        log.info("MCP Stdio Transport started - Listening on stdin");

        // Main event loop - blocks on stdin.readLine()
        try {
            while (running.get()) {
                String line = stdin.readLine();

                // EOF (Ctrl+D) or stream closed
                if (line == null) {
                    log.info("Stdin closed (EOF) - Shutting down");
                    break;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Process the request
                handleRequest(line);
            }
        } catch (Exception e) {
            log.error("Error in stdio transport loop", e);
            throw e;
        } finally {
            cleanup();
        }
    }

    /**
     * Handle a single JSON-RPC request.
     *
     * @param jsonLine JSON-RPC request as string
     */
    private void handleRequest(String jsonLine) {
        try {
            // Log to stderr (not stdout!)
            log.debug("Received: {}", jsonLine);

            // Parse request
            McpRequest request = requestHandler.parseRequest(jsonLine);

            // Handle request using existing handler
            McpResponse response = requestHandler.handleRequest(request);

            // Notifications return null (no response needed)
            if (response == null) {
                log.debug("Notification handled, no response needed");
                return;
            }

            // Serialize response
            String responseJson = requestHandler.serializeResponse(response);

            // Write to stdout (must be pure JSON, no extra characters!)
            stdout.println(responseJson);
            stdout.flush(); // Ensure immediate delivery

            // Log to stderr
            log.debug("Sent: {}", responseJson);

        } catch (Exception e) {
            log.error("Error handling request: {}", jsonLine, e);

            // Try to send error response
            try {
                McpResponse errorResponse = McpResponse.error(
                    null, // Unknown request ID
                    com.expense.mcp.protocol.McpError.parseError("Invalid JSON-RPC: " + e.getMessage())
                );
                String errorJson = requestHandler.serializeResponse(errorResponse);
                stdout.println(errorJson);
                stdout.flush();
            } catch (Exception ex) {
                log.error("Failed to send error response", ex);
            }
        }
    }

    /**
     * Shutdown the stdio transport gracefully.
     */
    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            log.warn("Stdio transport not running");
            return;
        }

        log.info("Shutting down MCP Stdio Transport...");
        cleanup();
    }

    /**
     * Clean up resources (streams).
     */
    private void cleanup() {
        try {
            if (stdin != null) {
                stdin.close();
            }
            if (stdout != null) {
                stdout.close();
            }
            log.info("MCP Stdio Transport stopped");
        } catch (Exception e) {
            log.error("Error during cleanup", e);
        }
    }

    /**
     * Check if transport is running.
     */
    public boolean isRunning() {
        return running.get();
    }
}