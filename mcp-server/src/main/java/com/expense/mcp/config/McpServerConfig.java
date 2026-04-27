package com.expense.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the MCP server.
 *
 * Configure via application.yml:
 * <pre>
 * mcp:
 *   server:
 *     enabled: true
 *     port: 8082
 *     websocket:
 *       path: /mcp
 *       enabled: true
 *     http:
 *       enabled: true
 *     auth:
 *       enabled: true
 *       api-keys:
 *         - "your-api-key-here"
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "mcp.server")
@Data
public class McpServerConfig {

    /**
     * Whether MCP server is enabled
     */
    private boolean enabled = true;

    /**
     * WebSocket configuration
     */
    private WebSocketConfig websocket = new WebSocketConfig();

    /**
     * HTTP configuration
     */
    private HttpConfig http = new HttpConfig();

    /**
     * Authentication configuration
     */
    private AuthConfig auth = new AuthConfig();

    @Data
    public static class WebSocketConfig {
        /**
         * WebSocket endpoint path
         */
        private String path = "/mcp";

        /**
         * Whether WebSocket transport is enabled
         */
        private boolean enabled = true;

        /**
         * Maximum message size (bytes)
         */
        private int maxMessageSize = 1048576; // 1MB

        /**
         * Session timeout (milliseconds)
         */
        private long sessionTimeout = 300000; // 5 minutes
    }

    @Data
    public static class HttpConfig {
        /**
         * Whether HTTP transport is enabled
         */
        private boolean enabled = true;

        /**
         * HTTP endpoint path
         */
        private String path = "/api/mcp";
    }

    @Data
    public static class AuthConfig {
        /**
         * Whether authentication is enabled
         */
        private boolean enabled = false; // Disabled by default for local dev

        /**
         * List of valid API keys
         */
        private String[] apiKeys = new String[0];

        /**
         * API key header name
         */
        private String headerName = "X-MCP-API-Key";
    }
}
