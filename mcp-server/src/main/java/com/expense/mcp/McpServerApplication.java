package com.expense.mcp;

import com.expense.mcp.transport.McpStdioTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

/**
 * Main application class for the MCP Server.
 *
 * This Spring Boot application provides an MCP (Model Context Protocol) server
 * that allows AI assistants like Claude to interact with the Monthly Expense Tracker
 * application using natural language.
 *
 * The server supports TWO execution modes:
 *
 * 1. STDIO MODE (for Claude Desktop):
 *    - Communication via stdin/stdout pipes
 *    - No web server (no HTTP/WebSocket)
 *    - Automatically detected when launched by Claude Desktop
 *    - Usage: java -jar mcp-server.jar --mode=stdio
 *
 * 2. WEB MODE (for manual testing):
 *    - Full Spring Boot web server on port 8082
 *    - HTTP endpoint: http://localhost:8082/api/mcp
 *    - WebSocket endpoint: ws://localhost:8082/mcp
 *    - Usage: java -jar mcp-server.jar --mode=web (default)
 *
 * Mode Detection:
 * - Explicit: --mode=stdio or --mode=web command-line argument
 * - Auto-detect: If no console available → stdio mode (Claude Desktop launch)
 * - Default: Web mode (manual testing)
 *
 * The server:
 * - Scans for @McpTool annotated methods and registers them automatically
 * - Reuses all existing services from the backend module
 * - Supports API key authentication for security (web mode)
 * - Backend services: Scanned from com.expense.monthly package
 * - Database: Inherited from backend configuration
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.expense.mcp",       // MCP server components
        "com.expense.monthly"    // Backend components (services, repositories, config)
})
@EntityScan("com.expense.monthly.model")  // Scan backend entities
@EnableJpaRepositories("com.expense.monthly.repository")  // Scan backend repositories
@Slf4j
public class McpServerApplication {

    public static void main(String[] args) {
        // Detect execution mode
        String mode = detectMode(args);

        log.info("Starting MCP Server for Monthly Expense Tracker in {} mode...", mode.toUpperCase());

        if ("stdio".equalsIgnoreCase(mode)) {
            startStdioMode(args);
        } else {
            startWebMode(args);
        }
    }

    /**
     * Detect execution mode from command-line arguments or environment.
     *
     * Priority:
     * 1. --mode=stdio or --mode=web command-line argument
     * 2. MCP_MODE environment variable
     * 3. Auto-detect: System.console() == null → stdio (Claude Desktop)
     * 4. Default: web mode
     *
     * @param args command-line arguments
     * @return "stdio" or "web"
     */
    private static String detectMode(String[] args) {
        // Check command-line arguments
        for (String arg : args) {
            if (arg.startsWith("--mode=")) {
                String mode = arg.substring("--mode=".length());
                log.info("Mode detected from command-line: {}", mode);
                return mode;
            }
        }

        // Check environment variable
        String envMode = System.getenv("MCP_MODE");
        if (envMode != null && !envMode.isEmpty()) {
            log.info("Mode detected from environment: {}", envMode);
            return envMode;
        }

        // Auto-detect: No console = likely launched by Claude Desktop
        if (System.console() == null) {
            log.info("No console detected - Assuming stdio mode (Claude Desktop launch)");
            return "stdio";
        }

        // Default to web mode
        log.info("Defaulting to web mode (manual launch)");
        return "web";
    }

    /**
     * Start server in STDIO mode.
     *
     * This mode:
     * - Does NOT start a web server (no Tomcat, no HTTP/WebSocket)
     * - Uses Spring Boot's non-web application context
     * - Communicates via stdin/stdout pipes
     * - Blocks on stdin until shutdown signal
     *
     * Used by: Claude Desktop (auto-launched)
     *
     * @param args command-line arguments
     */
    private static void startStdioMode(String[] args) {
        log.info("Initializing Spring Boot context...");

        // Create Spring application without web server
        SpringApplication app = new SpringApplication(McpServerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);  // CRITICAL: No web server!
        app.setBannerMode(Banner.Mode.OFF);  // Suppress banner for clean output

        // Start Spring context (but not web server)
        // This may take several seconds (database connections, JPA initialization, etc.)
        ApplicationContext context = app.run(args);

        log.info("Spring Boot context initialized successfully");

        // IMPORTANT: Wait for all beans to be fully initialized
        // This ensures database connections, JPA repositories, and all services are ready
        // before we start accepting MCP requests from Claude Desktop
        try {
            // Small delay to ensure all async initialization completes
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.warn("Interrupted during initialization delay", e);
        }

        // Get stdio transport bean
        McpStdioTransport transport = context.getBean(McpStdioTransport.class);

        // Register shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received");
            transport.shutdown();
        }));

        try {
            // Start stdio transport - BLOCKS until stdin closes
            log.info("MCP Server ready - Starting stdio transport");
            transport.start();

            log.info("Stdio transport stopped - Exiting");
        } catch (Exception e) {
            log.error("Fatal error in stdio transport", e);
            System.exit(1);
        }
    }

    /**
     * Start server in WEB mode.
     *
     * This mode:
     * - Starts full Spring Boot web server (Tomcat)
     * - HTTP endpoint: http://localhost:8082/api/mcp
     * - WebSocket endpoint: ws://localhost:8082/mcp
     * - Runs indefinitely until stopped
     *
     * Used by: Manual testing, development
     *
     * @param args command-line arguments
     */
    private static void startWebMode(String[] args) {
        log.info("Initializing WEB transport (HTTP + WebSocket)...");

        // Standard Spring Boot startup (current behavior)
        SpringApplication.run(McpServerApplication.class, args);

        log.info("MCP Server started in WEB mode");
        log.info("HTTP endpoint: http://localhost:8082/api/mcp");
        log.info("WebSocket endpoint: ws://localhost:8082/mcp");
    }
}
