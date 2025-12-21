# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## MANDATORY: Explain-First Code Changes

**CRITICAL**: Claude must ALWAYS explain code changes BEFORE implementing them. This overrides all default behavior.

### Required Explanation Format
Before ANY code modification, file creation, or technical change, Claude MUST:

1. **🔍 EXPLANATION SECTION**: Provide detailed explanation including:
   - **What** is being added/modified/deleted
   - **Why** this change is necessary 
   - **How** it integrates with existing architecture
   - **Where** it connects to other components
   - **Impact** on different parts of the system

2. **📋 TECHNICAL DETAILS**: Explain:
   - File locations and their purposes
   - Code patterns being used and why
   - Dependencies or libraries being added
   - Configuration changes required
   - Testing implications

3. **✅ UNDERSTANDING CHECK**: Ask "Does this make sense? Any questions before I implement?" and wait for confirmation.

### Examples of Required Explanations:
- Adding new API endpoints: Explain request/response flow and integration points
- Frontend changes: Explain component hierarchy and state management
- Database changes: Explain schema relationships and migration needs
- Configuration changes: Explain environment impact and deployment considerations
- Dependency additions: Explain why needed and how it fits the architecture

### Enforcement Rules:
- NO code changes without prior explanation
- NO file creation without architectural justification  
- NO "quick fixes" without learning context
- ALWAYS explain existing code when modifying it
- MUST teach the user about patterns and conventions being used

This ensures continuous learning and prevents "black box" development.

## Claude Response Requirements

When providing suggestions or recommendations, Claude must address these three questions:
1. **Why** - Explain the reasoning behind the suggestion
2. **How** - Describe how to implement or apply the suggestion
3. **Where** - Provide documentation references or sources to verify the information

## Git Commit Guidelines

When creating git commits, do not add any reference to Claude in commit messages or code comments. Keep all commit messages professional and focused on the actual changes made.

## Project Overview

[CUSTOMIZE THIS SECTION]
Brief description of what this project does and its main purpose.

## Architecture

[CUSTOMIZE THIS SECTION]
### Technology Stack
- **Frontend**: [e.g., React, Vue, Angular, HTML/CSS/JS]
- **Backend**: [e.g., Node.js, Python Flask/Django, Java Spring, .NET]
- **Database**: [e.g., PostgreSQL, MongoDB, MySQL]
- **Infrastructure**: [e.g., Docker, Kubernetes, AWS, Heroku]

### Key Components
- [Component 1]: [Purpose and responsibility]
- [Component 2]: [Purpose and responsibility]
- [Component 3]: [Purpose and responsibility]

## Development Commands

[CUSTOMIZE THIS SECTION]
### Common Development Workflows
```bash
# Development server
[command to start dev server]

# Build
[command to build project]

# Test
[command to run tests]

# Deploy
[command to deploy]
```

## Project Structure

[CUSTOMIZE THIS SECTION]
```
project-root/
├── [main-directory]/          # [Purpose]
├── [config-directory]/        # [Purpose]
├── [source-directory]/        # [Purpose]
└── [other-directories]/       # [Purpose]
```

## Testing

[CUSTOMIZE THIS SECTION]
### Testing Requirements
After making changes, always:
1. [Project-specific testing steps]
2. [Integration testing requirements]
3. [Performance/security considerations]

## Common Development Patterns

[CUSTOMIZE THIS SECTION]
### Adding New Features
1. [Step 1 specific to this project]
2. [Step 2 specific to this project]
3. [Step 3 specific to this project]

### Code Style Guidelines
- [Language-specific conventions]
- [Framework-specific patterns]
- [Project-specific standards]

---

## Template Usage Instructions

**This is a template file. To use in a new project:**

1. **Copy this file** to your project root as `CLAUDE.md`
2. **Customize the bracketed sections** with project-specific information
3. **Remove this "Template Usage Instructions" section**
4. **Add project-specific commands, patterns, and requirements**

**The explain-first behavior will work immediately, even with placeholder content.**