#!/bin/bash

#
# Stop MCP Server and PostgreSQL port-forward
#

echo "Stopping MCP Server and PostgreSQL port-forward..."
echo ""

# Stop MCP server
MCP_PID=$(lsof -ti :8082)
if [ -n "$MCP_PID" ]; then
    echo "Stopping MCP Server (PID: $MCP_PID)..."
    kill $MCP_PID 2>/dev/null || true
    echo "✅ MCP Server stopped"
else
    echo "⚠️  MCP Server not running"
fi

# Stop port-forward
PF_PID=$(ps aux | grep 'port-forward.*postgresql' | grep -v grep | awk '{print $2}')
if [ -n "$PF_PID" ]; then
    echo "Stopping PostgreSQL port-forward (PID: $PF_PID)..."
    kill $PF_PID 2>/dev/null || true
    echo "✅ Port-forward stopped"
else
    echo "⚠️  PostgreSQL port-forward not running"
fi

echo ""
echo "========================================="
echo "✅ All services stopped"
echo "========================================="
