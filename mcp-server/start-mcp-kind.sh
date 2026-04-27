#!/bin/bash

#
# Start MCP Server connected to KIND PostgreSQL
#

set -e

echo "========================================="
echo "Starting MCP Server with KIND PostgreSQL"
echo "========================================="
echo ""

cd "$(dirname "$0")"

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install kubectl."
    exit 1
fi

# Check if KIND cluster is running
if ! kubectl get nodes &> /dev/null; then
    echo "❌ KIND cluster not accessible. Please start Docker Desktop and KIND cluster."
    exit 1
fi

# Check if PostgreSQL pod is running
if ! kubectl get pod -n monthly-expense monthly-expense-postgresql-0 &> /dev/null; then
    echo "❌ PostgreSQL pod not found in KIND cluster."
    exit 1
fi

echo "✅ KIND cluster is accessible"
echo "✅ PostgreSQL pod is running"
echo ""

# Check if port-forward is already running
if lsof -i :5432 &> /dev/null; then
    echo "✅ Port 5432 is already forwarded"
else
    echo "Starting PostgreSQL port forward..."
    kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 > /tmp/postgres-port-forward.log 2>&1 &
    PF_PID=$!
    echo "✅ Port forward started (PID: $PF_PID)"
    sleep 3
fi

echo ""

# Check if MCP server is already running
if lsof -i :8082 &> /dev/null; then
    echo "⚠️  MCP server is already running on port 8082"
    read -p "Do you want to restart it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        pkill -f "mcp-server-1.0.0.jar" || true
        sleep 2
    else
        echo "Keeping existing MCP server running."
        exit 0
    fi
fi

echo "Starting MCP server..."
java -jar target/mcp-server-1.0.0.jar \
  --spring.config.location=file:./application-postgres.properties \
  > /tmp/mcp-server.log 2>&1 &

MCP_PID=$!
echo "✅ MCP server started (PID: $MCP_PID)"
echo ""

sleep 10

# Verify MCP server is running
if lsof -i :8082 &> /dev/null; then
    echo "========================================="
    echo "✅ MCP Server Started Successfully!"
    echo "========================================="
    echo ""
    echo "Endpoints:"
    echo "  WebSocket: ws://localhost:8082/mcp"
    echo "  HTTP:      http://localhost:8082/api/mcp"
    echo "  Health:    http://localhost:8082/actuator/health"
    echo ""
    echo "Database:"
    echo "  KIND PostgreSQL (port-forwarded from cluster)"
    echo "  Database: monthly_expense_db"
    echo "  Username: postgres"
    echo ""
    echo "Tools Registered: 6"
    echo "  - query_expenses"
    echo "  - get_category_totals"
    echo "  - get_monthly_summary"
    echo "  - get_total_spending"
    echo "  - search_transactions"
    echo "  - get_primary_currency"
    echo ""
    echo "Logs:"
    echo "  MCP Server:    tail -f /tmp/mcp-server.log"
    echo "  Port Forward:  tail -f /tmp/postgres-port-forward.log"
    echo ""
    echo "To stop:"
    echo "  kill $MCP_PID"
    echo "  pkill -f 'port-forward.*postgresql'"
    echo "========================================="
else
    echo "❌ MCP server failed to start. Check logs:"
    echo "  tail -50 /tmp/mcp-server.log"
    exit 1
fi
