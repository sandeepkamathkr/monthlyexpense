#!/bin/bash

# Script to build the Docker image with optimized settings to minimize disk space usage

# Clean up any dangling images and containers first
echo "Cleaning up Docker environment..."
docker system prune -f

# Navigate to the frontend directory and build the frontend image
echo "Building frontend Docker image..."
cd /Users/sandeepkamath/sandeep/MonthlyExpense/frontend
docker build -t monthly-expense-frontend:latest .
echo "Frontend Docker image built successfully."

# Navigate to the backend directory and build the backend image
echo "Building backend Docker image..."
cd /Users/sandeepkamath/sandeep/MonthlyExpense/backend
docker build -t monthly-expense-backend:latest -f Dockerfile .
echo "Backend Docker image built successfully."

echo "All Docker images built."