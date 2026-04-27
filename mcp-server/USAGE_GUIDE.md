# MCP Server Usage Guide

## 🎯 Quick Start Guide

### Step 1: Build and Run

```bash
# From project root
cd /Users/sandeepkamath/sandeep/MonthlyExpense

# Build both modules
mvn -f backend/pom.xml clean install -DskipTests
mvn -f mcp-server/pom.xml clean package -DskipTests

# Run MCP server
cd mcp-server
mvn spring-boot:run
```

**Server URLs:**
- WebSocket: `ws://localhost:8082/mcp`
- HTTP: `http://localhost:8082/api/mcp`
- Health: `http://localhost:8082/actuator/health`

### Step 2: Test the Server

**Test 1: List Available Tools**

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

Expected response:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "tools": [
      {
        "name": "query_expenses",
        "description": "Query expenses by date range, category, description, or amount range...",
        "inputSchema": { ... }
      },
      ...
    ]
  }
}
```

**Test 2: Query Expenses**

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "query_expenses",
      "arguments": {
        "category": "Groceries"
      }
    }
  }' | jq
```

**Test 3: Get Category Totals**

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "get_category_totals",
      "arguments": {}
    }
  }' | jq
```

## 📊 Available Tools Reference

### 1. query_expenses

Search and filter transactions.

**Parameters:**
- `startDate` (optional): Start date in YYYY-MM-DD format
- `endDate` (optional): End date in YYYY-MM-DD format
- `category` (optional): Category name (e.g., "Groceries")
- `description` (optional): Search text in description
- `minAmount` (optional): Minimum amount
- `maxAmount` (optional): Maximum amount
- `month` (optional): Month (1-12)
- `year` (optional): Year

**Example:**
```json
{
  "name": "query_expenses",
  "arguments": {
    "category": "Groceries",
    "month": 12,
    "year": 2025,
    "minAmount": 50
  }
}
```

### 2. get_category_totals

Get spending totals grouped by category.

**Parameters:**
- `month` (optional): Month (1-12)
- `year` (optional): Year

**Example:**
```json
{
  "name": "get_category_totals",
  "arguments": {
    "month": 12,
    "year": 2025
  }
}
```

### 3. get_monthly_summary

Get monthly breakdown for a specific year.

**Parameters:**
- `year` (required): Year to analyze

**Example:**
```json
{
  "name": "get_monthly_summary",
  "arguments": {
    "year": 2025
  }
}
```

### 4. get_total_spending

Get overall spending statistics.

**Parameters:** None

**Example:**
```json
{
  "name": "get_total_spending",
  "arguments": {}
}
```

### 5. search_transactions

Full-text search on transaction descriptions.

**Parameters:**
- `searchText` (required): Text to search for

**Example:**
```json
{
  "name": "search_transactions",
  "arguments": {
    "searchText": "coffee"
  }
}
```

### 6. get_primary_currency

Get the primary currency code.

**Parameters:** None

**Example:**
```json
{
  "name": "get_primary_currency",
  "arguments": {}
}
```

## 🤖 Using with Claude Desktop

### 1. Install Claude Desktop

Download from: https://claude.ai/download

### 2. Configure MCP Server

Edit Claude Desktop's config file:

**macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Add this configuration:

```json
{
  "mcpServers": {
    "monthly-expenses": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/sandeepkamath/sandeep/MonthlyExpense/mcp-server/target/mcp-server-1.0.0.jar"
      ],
      "env": {
        "DATABASE_URL": "jdbc:postgresql://localhost:5432/monthly_expense_db",
        "DATABASE_USER": "your_username",
        "DATABASE_PASSWORD": "your_password"
      }
    }
  }
}
```

### 3. Restart Claude Desktop

Close and reopen Claude Desktop. The MCP server should start automatically.

### 4. Interact with Your Expenses

Now you can ask Claude natural language questions:

- "What did I spend on groceries last month?"
- "Show me all transactions over $100 in December"
- "What's my total spending for 2025?"
- "Which category do I spend the most on?"
- "Find all transactions containing 'Starbucks'"

## 🔒 Security Configuration

### Enable API Key Authentication

1. Edit `application.yml`:

```yaml
mcp:
  server:
    auth:
      enabled: true
      api-keys:
        - "your-secret-api-key-here"
        - "another-api-key"
```

2. Restart server

3. Include API key in requests:

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -H "X-MCP-API-Key: your-secret-api-key-here" \
  -d '{...}'
```

## 🗄️ Database Configuration

### Using PostgreSQL (Production)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/monthly_expense_db
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Using H2 (Development)

Default configuration (no changes needed):

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/expense_db
    username: sa
    password: password
```

## 🐛 Troubleshooting

### Server won't start

**Check port availability:**
```bash
lsof -i :8082
```

If port is in use, change it in `application.yml`:
```yaml
server:
  port: 8083
```

### Tools not discovered

Check logs for:
```
Registered tool: query_expenses
Registered tool: get_category_totals
...
```

If tools aren't showing, verify `ExpenseTools.java` has `@McpResource` annotation.

### Database connection error

Verify database is running:
```bash
# PostgreSQL
psql -h localhost -U postgres -d monthly_expense_db

# H2
# Check console at http://localhost:8082/h2-console
```

### API key authentication failing

Check header name matches configuration:
```yaml
mcp:
  server:
    auth:
      header-name: X-MCP-API-Key  # Default
```

## 📝 Adding Custom Tools

### Example: Add a "get_expensive_transactions" tool

1. Open `ExpenseTools.java`

2. Add new method:

```java
@McpTool(
    name = "get_expensive_transactions",
    description = "Get transactions above a certain amount threshold"
)
public Map<String, Object> getExpensiveTransactions(
    @McpParam(
        name = "threshold",
        description = "Minimum amount to consider expensive",
        required = true
    ) BigDecimal threshold,

    @McpParam(
        name = "limit",
        description = "Maximum number of results to return",
        required = false,
        defaultValue = "10"
    ) Integer limit
) {
    List<Transaction> transactions = transactionService.getAllTransactions()
        .stream()
        .filter(t -> t.getAmount().compareTo(threshold) >= 0)
        .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
        .limit(limit)
        .collect(Collectors.toList());

    BigDecimal total = transactions.stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return Map.of(
        "count", transactions.size(),
        "total", total,
        "threshold", threshold,
        "transactions", transactions
    );
}
```

3. Rebuild and restart:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

4. Test new tool:

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "get_expensive_transactions",
      "arguments": {
        "threshold": 100,
        "limit": 5
      }
    }
  }' | jq
```

That's it! The tool is automatically discovered and registered.

## 🚀 Performance Tips

1. **Enable JPA query caching** for frequently accessed data
2. **Add database indexes** on commonly queried fields
3. **Use connection pooling** for production PostgreSQL
4. **Limit result sizes** with pagination parameters
5. **Enable HTTP compression** for large responses

## 📦 Deployment

See the main README for Docker and Kubernetes deployment instructions.

## 💡 Best Practices

1. **Always validate input parameters** using `@McpParam` constraints
2. **Return structured data** (Maps/Objects) not just strings
3. **Include counts and totals** in query results
4. **Log tool executions** for debugging
5. **Handle exceptions gracefully** and return meaningful errors
6. **Document tool descriptions** clearly for Claude to understand when to use them
7. **Use semantic tool names** (e.g., `get_monthly_summary` not `monthlySum`)

## 📚 Additional Resources

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [Claude Desktop Docs](https://docs.anthropic.com/claude/docs)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/)
