package com.expense.mcp.security;

import com.expense.mcp.config.McpServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Filter for API key authentication.
 * Checks for valid API key in the configured header.
 *
 * Only active when mcp.server.auth.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "mcp.server.auth", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final McpServerConfig serverConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip authentication for health check endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/health") || requestURI.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get API key from header
        String headerName = serverConfig.getAuth().getHeaderName();
        String providedApiKey = request.getHeader(headerName);

        if (providedApiKey == null || providedApiKey.isEmpty()) {
            log.warn("Missing API key in request: uri={}", requestURI);
            sendUnauthorizedResponse(response, "Missing API key");
            return;
        }

        // Validate API key
        String[] validApiKeys = serverConfig.getAuth().getApiKeys();
        boolean isValid = Arrays.asList(validApiKeys).contains(providedApiKey);

        if (!isValid) {
            log.warn("Invalid API key in request: uri={}", requestURI);
            sendUnauthorizedResponse(response, "Invalid API key");
            return;
        }

        // API key is valid, continue
        log.debug("API key validated successfully");
        filterChain.doFilter(request, response);
    }

    /**
     * Send 401 Unauthorized response with JSON error
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> error = Map.of(
                "jsonrpc", "2.0",
                "id", null,
                "error", Map.of(
                        "code", -32003,
                        "message", message
                )
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
