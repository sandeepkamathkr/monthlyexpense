# Monthly Expense Tracker - MCP Server

## Overview

This is an **MCP (Model Context Protocol) server** for the Monthly Expense Tracker application. It allows AI assistants like Claude to interact with your expense data using natural language queries and analysis tools.

### What is MCP?

MCP (Model Context Protocol) is a standardized protocol that allows AI models to:
- Call functions/tools in your application
- Query databases and APIs
- Perform complex analysis
- All through natural language interaction!

### Architecture

```
┌─────────────┐          ┌──────────────────┐          ┌────────────────┐
│   Claude    │ ◄────────┤   MCP Server     │◄─────────┤ TransactionService │
│ (AI Client) │  WebSocket  │  (This Module)   │  Reuses  │  (Backend Module)  │
│             │   or HTTP   │                  │          │                    │
└─────────────┘          └──────────────────┘          └────────────────┘
                                   │
                                   ▼
                          ┌─────────────────┐
                          │   PostgreSQL    │
                          │   Database      │
                          └─────────────────┘
```

## Features

### 🔧 MCP Framework
- **Annotation-based tool definition** - Just annotate methods with `@McpTool`
- **Automatic tool discovery** - No manual registration needed
- **Type-safe parameters** - Leverages Java's type system
- **JSON-RPC 2.0 protocol** - Industry standard RPC over JSON
- **Dual transport** - WebSocket + HTTP endpoints

### 📊 Available Tools

1. **query_expenses** - Search expenses by date, category, amount, description
2. **get_category_totals** - Spending breakdown by category
3. **get_monthly_summary** - Monthly spending for a specific year
4. **get_total_spending** - Overall spending statistics
5. **search_transactions** - Full-text search on descriptions
6. **get_primary_currency** - Get the currency used in the database

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Backend module (`backend/`) built and available

### Build

```bash
# From mcp-server directory
mvn clean package

# Or build both backend and mcp-server
cd /Users/sandeepkamath/sandeep/MonthlyExpense
mvn -f backend/pom.xml clean install
mvn -f mcp-server/pom.xml clean package
```

### Run

```bash
# From mcp-server directory
mvn spring-boot:run

# Or run the JAR
java -jar target/mcp-server-1.0.0.jar
```

The MCP server will start on port **8082** by default.

**Endpoints:**
- WebSocket: `ws://localhost:8082/mcp`
- HTTP: `POST http://localhost:8082/api/mcp`
- Health: `GET http://localhost:8082/api/mcp/health`

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
mcp:
  server:
    enabled: true

    websocket:
      path: /mcp
      enabled: true

    http:
      enabled: true
      path: /api/mcp

    auth:
      enabled: false  # Set to true for production
      api-keys:
        - "your-secret-api-key-here"
```

### Database Configuration

By default, uses H2 in-memory database. For PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/monthly_expense_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Usage Examples

### HTTP Testing with curl

**List available tools:**

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'
```

**Query expenses for groceries:**

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
        "category": "Groceries",
        "month": 12,
        "year": 2025
      }
    }
  }'
```

**Get category totals:**

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tools/call",
    "params": {
      "name": "get_category_totals",
      "arguments": {
        "month": 12,
        "year": 2025
      }
    }
  }'
```

### Using with Claude Desktop

1. Install Claude Desktop app
2. Configure MCP server in Claude's settings:

```json
{
  "mcpServers": {
    "monthly-expenses": {
      "url": "ws://localhost:8082/mcp"
    }
  }
}
```

3. Ask Claude questions:
   - "What did I spend on groceries last month?"
   - "Show me all transactions over $100"
   - "What's my total spending for 2025?"

## Development

### Adding New Tools

1. Create a method in `ExpenseTools.java` (or a new `@McpResource` class)
2. Annotate with `@McpTool`:

```java
@McpTool(
    name = "my_new_tool",
    description = "What this tool does"
)
public Map<String, Object> myNewTool(
    @McpParam(name = "param1", required = true, description = "First parameter")
    String param1
) {
    // Implementation
    return Map.of("result", "value");
}
```

3. Restart the server - tool is automatically discovered!

### Project Structure

```
mcp-server/
├── src/main/java/com/expense/mcp/
│   ├── McpServerApplication.java      # Main application
│   ├── annotations/                    # @McpTool, @McpParam, @McpResource
│   ├── core/                          # Tool registry, executor, converter
│   ├── protocol/                      # JSON-RPC 2.0 models
│   ├── transport/                     # WebSocket + HTTP handlers
│   ├── security/                      # API key authentication
│   ├── config/                        # Spring configuration
│   └── tools/                         # Expense-specific tools
└── src/main/resources/
    └── application.yml                # Configuration
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=McpToolRegistryTest
```

## Security

### API Key Authentication

Enable in production:

```yaml
mcp:
  server:
    auth:
      enabled: true
      api-keys:
        - "your-secret-key-1"
        - "your-secret-key-2"
```

Then include the API key in requests:

```bash
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -H "X-MCP-API-Key: your-secret-key-1" \
  -d '...'
```

## Troubleshooting

**Port 8082 already in use:**
```yaml
server:
  port: 8083  # Change port
```

**Cannot find backend classes:**
- Ensure backend module is built: `mvn -f backend/pom.xml install`
- Check dependency version in `pom.xml` matches backend version

**Tools not discovered:**
- Check that `@McpResource` class is in `com.expense.mcp.tools` package
- Verify Spring component scanning is enabled
- Check logs for registration messages

## License

Same as parent project.

## Support

For issues or questions, please file an issue in the main repository.
