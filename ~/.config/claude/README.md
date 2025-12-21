# Claude Global Explain-First Configuration

This directory contains global settings to enforce explain-first behavior across all Claude interactions.

## Files Created

### 1. `settings.json`
Global Claude configuration file that should be referenced by Claude sessions to enforce explain-first behavior.

### 2. Browser Setup

#### Bookmark Method:
1. Copy the JavaScript code from `browser-reminder.js`
2. Create a new bookmark in your browser
3. Set the URL to the JavaScript code (starting with `javascript:`)
4. Name it "Claude Learning Mode"
5. Click the bookmark whenever you start a Claude session

#### Browser Extension Alternative:
Create a simple Chrome/Firefox extension that automatically shows the reminder on claude.ai pages.

### 3. Terminal Integration
Added to `~/.zshrc`:
- Environment variables for Claude settings
- `claude-help` command to show reminders
- Automatic reminder when opening terminal

## Usage

### Terminal
```bash
# Show reminder
claude-help

# Environment variables are automatically set:
# CLAUDE_EXPLAIN_FIRST=true
# CLAUDE_EDUCATIONAL_MODE=true  
# CLAUDE_REQUIRE_UNDERSTANDING=true
```

### Browser
- Use the bookmark "Claude Learning Mode" before starting Claude sessions
- Or copy this prompt if Claude doesn't explain automatically:

```
"Please explain what you're about to change and why, following the explain-first approach. Include: what, why, how it integrates, where it connects, and impact analysis. Ask for understanding confirmation before implementing."
```

### Projects with CLAUDE.md
The explain-first behavior is automatically enforced in projects with the CLAUDE.md configuration.

### Projects without CLAUDE.md
Use this template prompt:
```
"Before making any code changes, please:
1. 🔍 EXPLAIN: What you're changing and why
2. 📋 DETAILS: How it integrates with existing architecture  
3. ✅ CHECK: Ask 'Does this make sense? Any questions before I implement?'

This ensures I learn about every change instead of getting black-box development."
```

## Enforcement Checklist

Before Claude makes changes, it should explain:
- [ ] **What** is being added/modified/deleted
- [ ] **Why** this change is necessary
- [ ] **How** it integrates with existing architecture
- [ ] **Where** it connects to other components  
- [ ] **Impact** on different parts of the system
- [ ] Ask for understanding confirmation

## Troubleshooting

If Claude doesn't follow explain-first automatically:
1. Reference your CLAUDE.md file (if it exists)
2. Use the environment variables as context
3. Copy the browser reminder prompt
4. Explicitly request explanations before any code changes