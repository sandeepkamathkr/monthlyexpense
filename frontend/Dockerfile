# ==============================================================================
# Multi-Architecture React Frontend Dockerfile
# ==============================================================================
# Purpose: Build production-ready frontend container for Monthly Expense Tracker
# Supports: linux/amd64, linux/arm64 (Apple Silicon), windows/amd64
# Architecture: Multi-stage build for optimized final image size
# ==============================================================================

# ==============================================================================
# STAGE 1: BUILDER - File Preparation and Validation
# ==============================================================================
# Use BUILDPLATFORM for builder stage to optimize cross-compilation
# This allows building on the host architecture even when targeting different platforms
FROM --platform=${BUILDPLATFORM} docker.io/library/alpine:3.16 as builder

# Builder stage metadata
LABEL stage="builder"
LABEL description="File preparation stage for React frontend"

# Set working directory for build operations
WORKDIR /app

# Install essential tools for validation and health checks
# curl: Required for testing and validation
RUN apk add --no-cache curl

# Copy React build artifacts and configuration
# Note: React build must be completed BEFORE Docker build
# Run: npm run build && cp -r build/* public/
COPY public/ ./public/
COPY nginx.conf ./nginx.conf

# ==============================================================================
# CRITICAL BUILD VALIDATION
# ==============================================================================
# Verify React build exists before proceeding
# This prevents deployment of incomplete builds
RUN if [ ! -d "public/static" ]; then \
        echo "ERROR: React build artifacts not found!"; \
        echo "Please run the following commands first:"; \
        echo "  cd frontend"; \
        echo "  npm run build"; \
        echo "  cp -r build/* public/"; \
        exit 1; \
    fi

# Verify static assets exist
RUN if [ ! -d "public/static/js" ] || [ ! -d "public/static/css" ]; then \
        echo "ERROR: Static assets (JS/CSS) not found in build!"; \
        echo "Ensure React build completed successfully"; \
        exit 1; \
    fi

# ==============================================================================
# STAGE 2: PRODUCTION - Optimized Runtime Image
# ==============================================================================
# Use TARGETPLATFORM for final stage to match deployment architecture
FROM --platform=${TARGETPLATFORM} docker.io/library/nginx:alpine

# ==============================================================================
# IMAGE METADATA
# ==============================================================================
LABEL maintainer="Monthly Expense App Team"
LABEL description="Frontend for Monthly Expense Tracker - React SPA"
LABEL version="1.2.0"
LABEL platform="${TARGETPLATFORM}"
LABEL component="frontend"
LABEL technology="react"
LABEL webserver="nginx"

# ==============================================================================
# SYSTEM SETUP
# ==============================================================================

# Install curl for health checks and debugging
RUN apk add --no-cache curl

# ==============================================================================
# SECURITY: NON-ROOT USER SETUP
# ==============================================================================
# Create dedicated user for nginx process
# This follows security best practices
RUN addgroup -g 1001 -S nginxgroup && \
    adduser -S nginxuser -u 1001 -G nginxgroup

# ==============================================================================
# APPLICATION DEPLOYMENT
# ==============================================================================

# Copy React build files from builder stage with proper ownership
# /usr/share/nginx/html/ is the default nginx document root
COPY --from=builder --chown=nginxuser:nginxgroup /app/public/ /usr/share/nginx/html/

# Copy custom nginx configuration for SPA routing
# This handles React Router client-side routing
COPY --from=builder --chown=nginxuser:nginxgroup /app/nginx.conf /etc/nginx/conf.d/default.conf

# ==============================================================================
# CRITICAL BUILD VERIFICATION
# ==============================================================================
# Prevent Chrome crash issue caused by multiple main.*.js files
# This validation ensures only one main JavaScript file exists
# Multiple main files can cause resource conflicts and browser crashes
RUN JS_COUNT=$(find /usr/share/nginx/html/static/js -name "main.*.js" 2>/dev/null | wc -l) && \
    if [ "$JS_COUNT" -eq 0 ]; then \
        echo "ERROR: No main JavaScript file found!"; \
        echo "React build may have failed"; \
        ls -la /usr/share/nginx/html/static/js/ || echo "JS directory not found"; \
        exit 1; \
    elif [ "$JS_COUNT" -gt 1 ]; then \
        echo "ERROR: Found $JS_COUNT main JS files! This causes Chrome crashes."; \
        echo "Found files:"; \
        find /usr/share/nginx/html/static/js -name "main.*.js" 2>/dev/null; \
        echo "Solution: Clean build directory before npm run build"; \
        exit 1; \
    fi && \
    echo "✅ Build verification passed: Single main JS file confirmed"

# Verify CSS files exist
RUN if [ ! -f /usr/share/nginx/html/static/css/main.*.css ]; then \
        echo "WARNING: No main CSS file found"; \
        ls -la /usr/share/nginx/html/static/css/ || echo "CSS directory not found"; \
    fi

# ==============================================================================
# NGINX CONFIGURATION AND PERMISSIONS
# ==============================================================================

# Set proper permissions for nginx directories
# This allows nginx to run as non-root user
RUN chown -R nginxuser:nginxgroup /var/cache/nginx && \
    chown -R nginxuser:nginxgroup /var/log/nginx && \
    chown -R nginxuser:nginxgroup /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown nginxuser:nginxgroup /var/run/nginx.pid

# Configure logging for container environments
# Direct logs to stdout/stderr for better container log management
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log

# Switch to non-root user for security
USER nginxuser

# ==============================================================================
# NETWORK CONFIGURATION
# ==============================================================================

# Expose HTTP port
# Standard nginx port for web traffic
EXPOSE 80

# ==============================================================================
# HEALTH MONITORING
# ==============================================================================

# Configure health check for container orchestration
# Tests that nginx is serving the React application correctly
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# ==============================================================================
# APPLICATION STARTUP
# ==============================================================================

# Start nginx in foreground mode
# Required for containerized environments
CMD ["nginx", "-g", "daemon off;"]

# ==============================================================================
# USAGE EXAMPLES:
# ==============================================================================
# Prerequisites:
#   cd frontend
#   npm run build
#   rm -rf public/static && cp -r build/* public/
#
# Local build (current platform):
#   docker build -t monthly-expense-frontend .
#
# Multi-architecture build:
#   docker buildx build --platform linux/amd64,linux/arm64 -t monthly-expense-frontend .
#
# Verify build contents:
#   docker run --rm monthly-expense-frontend find /usr/share/nginx/html -name "*.js"
# ==============================================================================
