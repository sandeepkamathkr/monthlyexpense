#!/bin/bash

#
# PostgreSQL Setup Script for MCP Server
# Creates the database and user if they don't exist
#

set -e

echo "========================================="
echo "PostgreSQL Setup for MCP Server"
echo "========================================="
echo ""

# Configuration
DB_NAME="monthly_expense_db"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if PostgreSQL is running
echo "Checking PostgreSQL connection..."
if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw template1; then
    echo -e "${GREEN}✓ PostgreSQL is running${NC}"
else
    echo -e "${RED}✗ PostgreSQL is not running or not accessible${NC}"
    echo ""
    echo "Please start PostgreSQL first:"
    echo "  macOS (Homebrew): brew services start postgresql@14"
    echo "  macOS (Postgres.app): Open Postgres.app"
    echo "  Docker: docker-compose up -d"
    echo ""
    exit 1
fi

# Check if database exists
echo ""
echo "Checking if database '$DB_NAME' exists..."
if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo -e "${YELLOW}⚠ Database '$DB_NAME' already exists${NC}"
    echo ""
    read -p "Do you want to drop and recreate it? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Dropping database '$DB_NAME'..."
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -c "DROP DATABASE IF EXISTS $DB_NAME;"
        echo -e "${GREEN}✓ Database dropped${NC}"
    else
        echo "Keeping existing database."
        echo ""
        echo -e "${GREEN}✓ Setup complete - using existing database${NC}"
        exit 0
    fi
fi

# Create database
echo ""
echo "Creating database '$DB_NAME'..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -c "CREATE DATABASE $DB_NAME;"
echo -e "${GREEN}✓ Database '$DB_NAME' created successfully${NC}"

# Grant privileges (if needed)
echo ""
echo "Setting up permissions..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"
echo -e "${GREEN}✓ Permissions configured${NC}"

echo ""
echo "========================================="
echo -e "${GREEN}PostgreSQL Setup Complete!${NC}"
echo "========================================="
echo ""
echo "Database Details:"
echo "  Host:     $DB_HOST"
echo "  Port:     $DB_PORT"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo ""
echo "Connection String:"
echo "  jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
echo ""
echo "Next Steps:"
echo "  1. Start MCP Server: mvn spring-boot:run"
echo "  2. Or run JAR: java -jar target/mcp-server-1.0.0.jar"
echo ""
echo "The database schema will be created automatically when the MCP server starts."
echo "========================================="
