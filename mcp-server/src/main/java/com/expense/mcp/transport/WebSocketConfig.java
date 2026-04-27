package com.expense.mcp.transport;

import com.expense.mcp.config.McpServerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for MCP server.
 * Registers the WebSocket endpoint at the configured path (default: /mcp)
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(prefix = "mcp.server.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final McpWebSocketHandler webSocketHandler;
    private final McpServerConfig serverConfig;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String path = serverConfig.getWebsocket().getPath();

        log.info("Registering WebSocket handler at path: {}", path);

        registry.addHandler(webSocketHandler, path)
                .setAllowedOrigins("*"); // Configure CORS as needed

        log.info("WebSocket endpoint registered successfully: ws://localhost:8082{}", path);
    }
}
