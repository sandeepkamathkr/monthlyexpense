#!/bin/bash

# Claude Explain-First Setup for New Projects
# Usage: setup-new-project.sh [project-directory]

set -e

# Configuration
TEMPLATE_PATH="$HOME/.config/claude/CLAUDE-template.md"
SCRIPT_NAME="setup-new-project"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Help function
show_help() {
    echo "Claude Explain-First Setup for New Projects"
    echo ""
    echo "Usage:"
    echo "  $SCRIPT_NAME [project-directory]"
    echo "  $SCRIPT_NAME                    # Use current directory"
    echo ""
    echo "Examples:"
    echo "  $SCRIPT_NAME /path/to/my-new-app"
    echo "  $SCRIPT_NAME ./frontend-project"
    echo "  cd my-project && $SCRIPT_NAME"
    echo ""
    echo "This script will:"
    echo "  1. Copy CLAUDE.md template to the project"
    echo "  2. Show customization instructions"
    echo "  3. Verify explain-first behavior is active"
}

# Main function
setup_project() {
    local project_dir="${1:-$(pwd)}"
    
    # Resolve absolute path
    project_dir=$(realpath "$project_dir")
    
    echo -e "${BLUE}🚀 Setting up Claude explain-first behavior for project...${NC}"
    echo -e "📁 Project directory: $project_dir"
    echo ""
    
    # Check if directory exists
    if [ ! -d "$project_dir" ]; then
        echo -e "${YELLOW}⚠️  Directory doesn't exist. Creating: $project_dir${NC}"
        mkdir -p "$project_dir"
    fi
    
    # Check if template exists
    if [ ! -f "$TEMPLATE_PATH" ]; then
        echo -e "${YELLOW}❌ Template not found at: $TEMPLATE_PATH${NC}"
        echo "Please run the global Claude setup first."
        exit 1
    fi
    
    # Copy template
    local claude_file="$project_dir/CLAUDE.md"
    
    if [ -f "$claude_file" ]; then
        echo -e "${YELLOW}⚠️  CLAUDE.md already exists. Backing up...${NC}"
        cp "$claude_file" "$claude_file.backup.$(date +%s)"
    fi
    
    cp "$TEMPLATE_PATH" "$claude_file"
    echo -e "${GREEN}✅ CLAUDE.md copied to project${NC}"
    
    # Show next steps
    echo ""
    echo -e "${BLUE}📝 NEXT STEPS:${NC}"
    echo "1. Open $claude_file"
    echo "2. Customize the [BRACKETED] sections with your project details:"
    echo "   - Project Overview"
    echo "   - Technology Stack" 
    echo "   - Development Commands"
    echo "   - Project Structure"
    echo "3. Remove the 'Template Usage Instructions' section at the bottom"
    echo ""
    echo -e "${GREEN}🎓 Explain-first behavior is now active for this project!${NC}"
    echo ""
    echo -e "${BLUE}Test it by asking Claude:${NC}"
    echo '"Add a simple Hello World feature to this project"'
    echo ""
    echo -e "Claude should automatically explain what it plans to do before making changes."
}

# Handle command line arguments
case "${1:-}" in
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        setup_project "$1"
        ;;
esac