# 🚀 Quick Start Guide - Claude Desktop MCP Integration

## ✅ **Configuration Status**

### **What's Ready:**
- ✅ MCP server built and configured
- ✅ PostgreSQL connection to KIND cluster configured
- ✅ Claude Desktop config file updated
- ✅ PostgreSQL port-forward running
- ✅ 6 expense analysis tools available

---

## 🎯 **Quick Start (3 Steps)**

### **1. Ensure Prerequisites are Running**

```bash
# Start PostgreSQL port-forward (if not already running)
kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &

# Verify it's running
lsof -i :5432
```

### **2. Restart Claude Desktop**

- **Quit:** `Cmd + Q`
- **Reopen:** Claude Desktop will auto-start the MCP server
- **Wait:** ~30 seconds for initialization

### **3. Test with a Simple Question**

Open Claude Desktop and ask:
```
What's my total spending?
```

**Expected:** Claude calls the tool and shows $8,724.88 total

---

## 📝 **Quick Test Questions**

Copy and paste these into Claude Desktop:

```
1. What's my total spending?

2. Show me my spending by category

3. How much did I spend on groceries?

4. Find all transactions at Woolworths

5. What's my largest expense?

6. Show me December spending
```

---

## 🔍 **Verify MCP is Connected**

Look for indicators in Claude Desktop:
- **Bottom right:** Server connection status (click to see "monthly-expenses")
- **Tool usage:** Claude mentions using your expense tools
- **Real data:** Actual dollar amounts from your database

---

## 🐛 **Quick Troubleshooting**

### **Issue: MCP server not connecting**
```bash
# Check if port-forward is running
lsof -i :5432

# If not, restart it
kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &

# Restart Claude Desktop
```

### **Issue: Can't see monthly-expenses server**
```bash
# Verify config file
cat ~/Library/Application\ Support/Claude/claude_desktop_config.json | jq

# Check Claude logs
tail -100 ~/Library/Logs/Claude/mcp*.log
```

---

## 📂 **Important Files**

- **Config:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **MCP Server:** `/Users/sandeepkamath/sandeep/MonthlyExpense/mcp-server/`
- **Test Guide:** `CLAUDE_DESKTOP_TEST.md` (detailed testing)
- **KIND Setup:** `KIND_SETUP.md` (manual setup)

---

## 🎯 **Current Database Stats**

Your KIND PostgreSQL contains:
- **Total:** $8,724.88
- **Transactions:** 119
- **Categories:** 16
- **Top Category:** Rent ($3,780.00)

---

## 💡 **What You Can Do**

Ask Claude natural questions like:
- "What did I spend on X last month?"
- "Show me my top 5 spending categories"
- "Find all transactions over $100"
- "How does my December spending compare to November?"
- "Which categories could I reduce?"
- "What's my average transaction amount?"

Claude will automatically call the right MCP tools to answer!

---

## 🔄 **Daily Usage**

### **Start of Day:**
```bash
# Ensure PostgreSQL port-forward is running
lsof -i :5432 || kubectl port-forward -n monthly-expense service/monthly-expense-postgresql 5432:5432 &
```

### **Use Claude Desktop:**
Just ask questions - MCP server starts automatically!

### **End of Day:**
No cleanup needed - everything stops when you quit Claude Desktop

---

## 📞 **Need Help?**

- **Detailed Testing:** See `CLAUDE_DESKTOP_TEST.md`
- **KIND Setup:** See `KIND_SETUP.md`
- **Usage Examples:** See `USAGE_GUIDE.md`
- **Technical Docs:** See `README.md`

---

## ✅ **Ready to Test!**

**Next Step:** Restart Claude Desktop and ask "What's my total spending?"

Your MCP server will automatically connect and provide real data! 🎉
