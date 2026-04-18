#!/bin/bash

# Script to build both Docker images.
# Run from anywhere — uses the script's own location to find the project root.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "Project root: $PROJECT_ROOT"

# Clean up dangling images and stopped containers
echo "Cleaning up Docker environment..."
docker system prune -f

# Build frontend image
echo "Building frontend Docker image..."
docker build -t monthly-expense-frontend:latest "$SCRIPT_DIR"
echo "Frontend Docker image built successfully."

# Build backend image
echo "Building backend Docker image..."
docker build -t monthly-expense-backend:latest -f "$PROJECT_ROOT/backend/Dockerfile" "$PROJECT_ROOT/backend"
echo "Backend Docker image built successfully."

echo "All Docker images built successfully."
