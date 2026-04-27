#!/bin/bash

#
# Build script for MCP Server
# Builds both backend and mcp-server modules
#

set -e  # Exit on error

echo "========================================="
echo "Building Monthly Expense MCP Server"
echo "========================================="
echo ""

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
echo "Project root: $PROJECT_ROOT"
echo ""

# Step 1: Build backend module
echo "Step 1/2: Building backend module..."
echo "-----------------------------------"
cd "$PROJECT_ROOT/backend"
mvn clean install -DskipTests
echo "✓ Backend build successful"
echo ""

# Step 2: Build MCP server
echo "Step 2/2: Building MCP server..."
echo "-----------------------------------"
cd "$PROJECT_ROOT/mcp-server"
mvn clean package -DskipTests
echo "✓ MCP server build successful"
echo ""

# Display build artifacts
echo "========================================="
echo "Build Complete!"
echo "========================================="
echo ""
echo "Artifacts created:"
echo "  Backend JAR:     $PROJECT_ROOT/backend/target/monthly-1.2.0.jar"
echo "  MCP Server JAR:  $PROJECT_ROOT/mcp-server/target/mcp-server-1.0.0.jar"
echo ""
echo "To run the MCP server:"
echo "  cd $PROJECT_ROOT/mcp-server"
echo "  java -jar target/mcp-server-1.0.0.jar"
echo ""
echo "Or use Maven:"
echo "  cd $PROJECT_ROOT/mcp-server"
echo "  mvn spring-boot:run"
echo ""
echo "MCP Server will be available at:"
echo "  WebSocket: ws://localhost:8082/mcp"
echo "  HTTP:      http://localhost:8082/api/mcp"
echo "========================================="
