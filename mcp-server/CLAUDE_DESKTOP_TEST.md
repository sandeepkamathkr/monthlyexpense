# Testing MCP Server with Claude Desktop

## ✅ Configuration Complete!

Your Claude Desktop is now configured to use the Monthly Expense MCP server.

---

## 🚀 **Step-by-Step Testing**

### **Step 1: Restart Claude Desktop**

**IMPORTANT:** You must restart Claude Desktop for the configuration changes to take effect.

1. **Quit Claude Desktop** completely:
   - macOS: `Cmd + Q` or Claude Desktop → Quit
   - Ensure it's fully closed (check Activity Monitor if needed)

2. **Reopen Claude Desktop**:
   - The MCP server will start automatically when Claude launches
   - Wait ~30 seconds for the server to initialize

---

### **Step 2: Verify MCP Server is Running**

Open Terminal and check:

```bash
# Check if MCP server is running
lsof -i :8082

# Check PostgreSQL port-forward is active
lsof -i :5432

# View MCP server logs (if needed)
tail -f /tmp/mcp-server-claude.log
```

**Expected:** You should see Java processes on both ports.

---

### **Step 3: Check MCP Connection in Claude Desktop**

Look for the **MCP indicator** in Claude Desktop:
- Bottom right corner should show "5 servers connected" (or similar)
- Click on it to see the list - "monthly-expenses" should appear

---

### **Step 4: Test with Simple Questions**

Try these questions in Claude Desktop:

#### **Test 1: Basic Query**
```
What's my total spending?
```

**Expected Response:**
Claude should call the `get_total_spending` tool and tell you:
- Total amount: $8,724.88
- Transaction count: 119
- Average amount: $73.32

---

#### **Test 2: Category Breakdown**
```
Show me my spending by category
```

**Expected Response:**
Claude should call `get_category_totals` and show:
- Rent: $3,780.00 (top category)
- Day Care: $894.50
- Groceries: $873.80
- etc.

---

#### **Test 3: Specific Category Query**
```
How much did I spend on groceries?
```

**Expected Response:**
Claude should call `query_expenses` with category="Groceries" and tell you the total.

---

#### **Test 4: Monthly Analysis**
```
Show me my spending for 2025 by month
```

**Expected Response:**
Claude should call `get_monthly_summary` with year=2025 and show monthly totals.

---

#### **Test 5: Search Transactions**
```
Find all transactions at Woolworths
```

**Expected Response:**
Claude should call `search_transactions` with searchText="Woolworths" and list matching transactions.

---

#### **Test 6: Complex Query**
```
What are my top 3 spending categories and how much did I spend in December?
```

**Expected Response:**
Claude should make multiple tool calls:
1. `get_category_totals` to get top categories
2. `query_expenses` with month=12 to get December spending

---

## 🐛 **Troubleshooting**

### **Problem: MCP server not showing in Claude Desktop**

**Solution:**
1. Check Claude Desktop logs:
   ```bash
   tail -100 ~/Library/Logs/Claude/mcp*.log
   ```

2. Verify config syntax:
   ```bash
   cat ~/Library/Application\ Support/Claude/claude_desktop_config.json | jq
   ```

3. Restart Claude Desktop again

---

### **Problem: "Tool execution failed" errors**

**Solution:**
1. Ensure PostgreSQL port-forward is running:
   ```bash
   lsof -i :5432
   ```

2. If not running, start it:
   ```bash
   kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &
   ```

3. Restart Claude Desktop

---

### **Problem: Connection timeout**

**Solution:**
1. Check MCP server is running:
   ```bash
   lsof -i :8082
   ```

2. Check MCP server logs for errors:
   ```bash
   ps aux | grep mcp-server-1.0.0.jar
   ```

3. Manually start the server to see errors:
   ```bash
   cd /Users/sandeepkamath/sandeep/MonthlyExpense/mcp-server
   java -jar target/mcp-server-1.0.0.jar \
     --spring.config.location=file:./application-postgres.properties
   ```

---

### **Problem: "Database connection failed"**

**Solution:**
1. Verify KIND cluster is running:
   ```bash
   kubectl get nodes
   ```

2. Verify PostgreSQL pod is running:
   ```bash
   kubectl get pod -n monthly-expense monthly-expense-postgresql-0
   ```

3. Test PostgreSQL connection:
   ```bash
   kubectl exec -n monthly-expense monthly-expense-postgresql-0 -- \
     env PGPASSWORD='4bTp9VmJ0D%' psql -U postgres -d monthly_expense_db -c "\dt"
   ```

---

## 📝 **Example Conversation Flow**

Here's a complete example of what you can do:

**You:** "Hi Claude, I want to analyze my expenses"

**Claude:** "I'd be happy to help! I have access to your expense data through the monthly-expenses MCP server. What would you like to know?"

**You:** "What's my total spending and which category am I spending the most on?"

**Claude:** *[Calls get_total_spending and get_category_totals]*

"Based on your expense data:
- **Total Spending:** $8,724.88 across 119 transactions
- **Average Transaction:** $73.32

**Top Spending Categories:**
1. 🏠 Rent: $3,780.00 (43% of total)
2. 👶 Day Care: $894.50 (10%)
3. 🛒 Groceries: $873.80 (10%)
4. 🛍️ Shopping: $867.53 (10%)
5. 🍽️ Eating Out: $750.80 (9%)

Your rent is by far your largest expense, accounting for nearly half of your total spending."

**You:** "Can you find all my grocery transactions?"

**Claude:** *[Calls query_expenses with category="Groceries"]*

"I found X grocery transactions totaling $873.80. Here are the details..."

---

## 🎯 **Advanced Queries to Try**

Once the basic tests work, try these:

1. **Trend Analysis:**
   - "How does my December spending compare to other months?"
   - "What's my average monthly spending?"

2. **Budget Insights:**
   - "Which categories could I reduce spending on?"
   - "Show me all transactions over $200"

3. **Search & Filter:**
   - "Find all transactions at Coles or Woolworths"
   - "Show me all eating out expenses in December"

4. **Statistical Analysis:**
   - "What's the largest single transaction I made?"
   - "How many transactions do I make per month on average?"

---

## 📊 **Available MCP Tools**

Claude has access to these 6 tools:

1. **query_expenses** - Search/filter transactions by date, category, amount
2. **get_category_totals** - Spending breakdown by category
3. **get_monthly_summary** - Monthly spending for a specific year
4. **get_total_spending** - Overall spending statistics
5. **search_transactions** - Full-text search on descriptions
6. **get_primary_currency** - Get currency code

---

## 🔧 **Manual Testing (Without Claude Desktop)**

If you want to test the tools directly:

```bash
# Test via HTTP API
curl -X POST http://localhost:8082/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/call",
    "params": {
      "name": "get_total_spending",
      "arguments": {}
    }
  }' | jq
```

---

## ✅ **Success Indicators**

You'll know it's working when:
- ✅ Claude mentions using the "monthly-expenses" tool
- ✅ Claude returns actual numbers from your database
- ✅ Claude can answer follow-up questions using the data
- ✅ You see tool calls in real-time (MCP indicator shows activity)

---

## 📚 **Next Steps**

After successful testing:
1. ✅ Explore natural language queries with your expense data
2. ✅ Ask Claude for spending insights and recommendations
3. ✅ Use Claude to find patterns in your spending
4. ✅ Get budget recommendations based on your data

---

## 🎉 **You're All Set!**

Your MCP server is configured and ready to use with Claude Desktop. Restart Claude Desktop and start asking questions about your expenses!
