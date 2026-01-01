# ==============================================================================
# Multi-Architecture Spring Boot Backend Dockerfile
# ==============================================================================
# Purpose: Build production-ready backend container for Monthly Expense Tracker
# Supports: linux/amd64, linux/arm64 (Apple Silicon), windows/amd64
# Base Image: Amazon Corretto 17 (OpenJDK) on Alpine Linux
# ==============================================================================

# Use platform-specific base image with full registry path
# TARGETPLATFORM is automatically set by Docker buildx for multi-arch builds
FROM --platform=${TARGETPLATFORM} docker.io/library/amazoncorretto:17-alpine

# ==============================================================================
# IMAGE METADATA
# ==============================================================================
# Labels help with image management and identification
LABEL maintainer="Monthly Expense Team"
LABEL description="Monthly Expense Tracker Backend - Spring Boot Application"
LABEL version="1.2.0"
LABEL platform="${TARGETPLATFORM}"
LABEL component="backend"
LABEL language="java"
LABEL framework="spring-boot"

# ==============================================================================
# SYSTEM SETUP
# ==============================================================================

# Set consistent working directory
WORKDIR /app

# Install essential packages for health checks and debugging
# curl: Required for health check endpoint testing
RUN apk add --no-cache curl

# ==============================================================================
# SECURITY: NON-ROOT USER SETUP
# ==============================================================================
# Create dedicated user and group for running the application
# This follows security best practices by avoiding root execution
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# ==============================================================================
# APPLICATION JAR DEPLOYMENT
# ==============================================================================

# Copy the Spring Boot JAR file with proper ownership
# --chown ensures the file is owned by the application user
COPY --chown=appuser:appgroup target/monthly-1.2.0.jar app.jar

# ==============================================================================
# BUILD VERIFICATION
# ==============================================================================
# Critical: Verify PostgreSQL driver is included in the JAR
# This prevents runtime errors when connecting to PostgreSQL database
# If PostgreSQL driver is missing, the build will fail early
RUN jar tf app.jar | grep -q "postgresql" || \
    (echo "ERROR: PostgreSQL driver not found in JAR file!" && \
     echo "Ensure Maven build includes PostgreSQL dependency" && \
     exit 1)

# ==============================================================================
# FILESYSTEM PERMISSIONS
# ==============================================================================

# Create application data directory with proper permissions
# This directory can be used for file storage, logs, or temporary data
RUN mkdir -p /app/data && \
    chown -R appuser:appgroup /app/data

# Switch to non-root user for security
# All subsequent commands and the final application run as this user
USER appuser

# ==============================================================================
# NETWORK CONFIGURATION
# ==============================================================================

# Expose the application port
# This must match server.port in application.properties (default: 8081)
EXPOSE 8081

# ==============================================================================
# HEALTH MONITORING
# ==============================================================================

# Configure health check using Spring Boot Actuator
# --interval: Check every 30 seconds
# --timeout: Wait max 10 seconds for response
# --start-period: Wait 60 seconds after start before first check
# --retries: Fail after 3 consecutive failures
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# ==============================================================================
# JVM OPTIMIZATION
# ==============================================================================

# Configure JVM for containerized environments
# UseContainerSupport: Auto-detect container memory limits
# MaxRAMPercentage: Use maximum 75% of available container memory
# UseG1GC: Use G1 garbage collector for better performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# ==============================================================================
# APPLICATION STARTUP
# ==============================================================================

# Start the Spring Boot application with Docker profile
# Docker profile activates PostgreSQL configuration
# Uses shell form to allow environment variable expansion
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=docker"]

# ==============================================================================
# USAGE EXAMPLES:
# ==============================================================================
# Local build (current platform):
#   docker build -t monthly-expense-backend .
#
# Multi-architecture build:
#   docker buildx build --platform linux/amd64,linux/arm64 -t monthly-expense-backend .
#
# Architecture verification:
#   docker inspect monthly-expense-backend | grep Architecture
# ==============================================================================