# MCP Server with KIND PostgreSQL - Complete Setup Guide

## 🎯 Overview

Your MCP server is now fully integrated with the PostgreSQL database running in your KIND (Kubernetes IN Docker) cluster!

---

## ✅ What's Configured

### **Database Connection:**
- **KIND PostgreSQL Service:** `monthly-expense-postgresql` (namespace: `monthly-expense`)
- **Database Name:** `monthly_expense_db`
- **Username:** `postgres`
- **Password:** `4bTp9VmJ0D%` (from Kubernetes secret)
- **Connection Method:** Port-forward from KIND to localhost:5432

### **MCP Server:**
- **Port:** 8082
- **WebSocket Endpoint:** `ws://localhost:8082/mcp`
- **HTTP Endpoint:** `POST http://localhost:8082/api/mcp`
- **Health Check:** `http://localhost:8082/actuator/health`

### **Registered MCP Tools (6):**
1. `query_expenses` - Search/filter transactions
2. `get_category_totals` - Spending by category
3. `get_monthly_summary` - Monthly breakdown
4. `get_total_spending` - Overall statistics
5. `search_transactions` - Full-text search
6. `get_primary_currency` - Get currency code

---

## 🚀 Quick Start

### **Start Everything:**
```bash
cd /Users/sandeepkamath/sandeep/MonthlyExpense/mcp-server
./start-mcp-kind.sh
```

### **Stop Everything:**
```bash
./stop-mcp-kind.sh
```

### **Check Status:**
```bash
# Check if MCP server is running
curl http://localhost:8082/actuator/health

# Check if port-forward is active
lsof -i :5432

# View MCP server logs
tail -f /tmp/mcp-server.log

# View PostgreSQL port-forward logs
tail -f /tmp/postgres-port-forward.log
```

---

## 📝 Manual Startup (Alternative)

If you want to start components individually:

### **1. Start PostgreSQL Port-Forward:**
```bash
kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &
```

### **2. Start MCP Server:**
```bash
cd /Users/sandeepkamath/sandeep/MonthlyExpense/mcp-server
java -jar target/mcp-server-1.0.0.jar \
  --spring.config.location=file:./application-postgres.properties &
```

---

## 🧪 Testing the MCP Server

### **Test 1: List Available Tools**
```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }' | jq
```

### **Test 2: Get Total Spending**
```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "get_total_spending",
      "arguments": {}
    }
  }' | jq
```

### **Test 3: Query Expenses by Category**
```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "query_expenses",
      "arguments": {
        "category": "Groceries"
      }
    }
  }' | jq
```

### **Test 4: Get Category Totals**
```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "4",
    "method": "tools/call",
    "params": {
      "name": "get_category_totals",
      "arguments": {}
    }
  }' | jq
```

---

## 💬 Using with Claude Desktop

### **Configuration:**

Edit your Claude Desktop config file:
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Add this configuration:
```json
{
  "mcpServers": {
    "monthly-expenses": {
      "url": "ws://localhost:8082/mcp"
    }
  }
}
```

### **Usage Examples:**

Once configured, you can ask Claude:

- "What did I spend on groceries last month?"
- "Show me all transactions over $100"
- "What's my total spending?"
- "Which category do I spend the most on?"
- "Find all transactions at Woolworths"
- "What's my average transaction amount?"

---

## 📂 File Structure

```
mcp-server/
├── target/mcp-server-1.0.0.jar          # Compiled MCP server
├── application-postgres.properties       # PostgreSQL configuration
├── start-mcp-kind.sh                    # Start script
├── stop-mcp-kind.sh                     # Stop script
├── README.md                            # Main documentation
├── USAGE_GUIDE.md                       # Detailed usage guide
└── KIND_SETUP.md                        # This file
```

---

## 🔧 Troubleshooting

### **Port 5432 already in use:**
```bash
# Check what's using port 5432
lsof -i :5432

# Kill existing port-forward
pkill -f 'port-forward.*postgresql'

# Restart port-forward
kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &
```

### **Port 8082 already in use:**
```bash
# Check what's using port 8082
lsof -i :8082

# Kill existing MCP server
pkill -f 'mcp-server-1.0.0.jar'

# Restart MCP server
./start-mcp-kind.sh
```

### **KIND cluster not accessible:**
```bash
# Check Docker Desktop is running
docker ps

# Check kubectl can connect
kubectl get nodes

# Check PostgreSQL pod is running
kubectl get pod -n monthly-expense monthly-expense-postgresql-0
```

### **MCP server can't connect to PostgreSQL:**
```bash
# Verify port-forward is active
lsof -i :5432

# Test PostgreSQL connection from kubectl
kubectl exec -n monthly-expense monthly-expense-postgresql-0 -- env PGPASSWORD='4bTp9VmJ0D%' psql -U postgres -d monthly_expense_db -c "\dt"

# Check MCP server logs
tail -100 /tmp/mcp-server.log
```

---

## 📊 Current Database Statistics

From your KIND PostgreSQL database:
- **Total Spending:** $8,724.88
- **Total Transactions:** 119
- **Average Transaction:** $73.32
- **Categories:** 16

**Top Spending Categories:**
1. Rent: $3,780.00
2. Day Care: $894.50
3. Groceries: $873.80
4. Shopping: $867.53
5. Eating Out: $750.80

---

## 🔐 Security Notes

- PostgreSQL password is stored in Kubernetes secret
- MCP server authentication is disabled (set `mcp.server.auth.enabled=true` for production)
- Port-forward is only accessible from localhost
- KIND cluster is isolated within Docker Desktop

---

## 📚 Additional Resources

- **MCP Specification:** https://spec.modelcontextprotocol.io/
- **Spring Boot Docs:** https://docs.spring.io/spring-boot/docs/current/reference/
- **KIND Documentation:** https://kind.sigs.k8s.io/
- **PostgreSQL Docs:** https://www.postgresql.org/docs/

---

## ✅ Next Steps

1. ✅ **Done:** MCP server connected to KIND PostgreSQL
2. ✅ **Done:** All 6 tools working and tested
3. **Optional:** Configure Claude Desktop to use the MCP server
4. **Optional:** Add more custom MCP tools (see USAGE_GUIDE.md)
5. **Optional:** Enable API key authentication for production

---

**Questions or Issues?** Check the logs:
- MCP Server: `/tmp/mcp-server.log`
- PostgreSQL Port-Forward: `/tmp/postgres-port-forward.log`
