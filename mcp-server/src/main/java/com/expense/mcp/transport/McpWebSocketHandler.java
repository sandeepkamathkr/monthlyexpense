package com.expense.mcp.transport;

import com.expense.mcp.config.McpServerConfig;
import com.expense.mcp.protocol.McpError;
import com.expense.mcp.protocol.McpRequest;
import com.expense.mcp.protocol.McpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket handler for MCP protocol.
 * Handles WebSocket connections and routes messages to McpRequestHandler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McpWebSocketHandler extends TextWebSocketHandler {

    private final McpRequestHandler requestHandler;
    private final McpServerConfig serverConfig;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: sessionId={}", session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: sessionId={}, payload={}", session.getId(), payload);

        try {
            // Parse request
            McpRequest request = requestHandler.parseRequest(payload);

            // Handle request
            McpResponse response = requestHandler.handleRequest(request);

            // Send response
            String responseJson = requestHandler.serializeResponse(response);
            session.sendMessage(new TextMessage(responseJson));

            log.debug("Sent WebSocket response: sessionId={}, payload={}", session.getId(), responseJson);

        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);

            // Send error response
            McpResponse errorResponse = McpResponse.error(
                    null,
                    McpError.parseError("Failed to parse request: " + e.getMessage())
            );

            try {
                String errorJson = requestHandler.serializeResponse(errorResponse);
                session.sendMessage(new TextMessage(errorJson));
            } catch (Exception sendError) {
                log.error("Failed to send error response", sendError);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: sessionId={}, status={}", session.getId(), status);
        super.afterConnectionClosed(session, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: sessionId=" + session.getId(), exception);
        super.handleTransportError(session, exception);
    }
}
