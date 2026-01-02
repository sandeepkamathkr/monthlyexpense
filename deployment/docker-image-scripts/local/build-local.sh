#!/bin/bash

# ==============================================================================
# Local Development Build Script
# ==============================================================================
# Purpose: Build Docker images for current platform only (fast development)
# Usage: ./build-local.sh
# Platform: Builds for your current machine architecture
# ==============================================================================

set -e  # Exit on any error

echo "🚀 Starting Local Development Build..."
echo "Platform: $(uname -m)"
echo "=================================="

# ==============================================================================
# STEP 1: Clean and Build Backend JAR
# ==============================================================================
echo ""
echo "📦 Step 1: Building Backend JAR (Fresh Build)"
echo "----------------------------------------------"

cd ../../../backend

# Clean previous builds to prevent cache issues
echo "  🧹 Cleaning previous Maven builds..."
mvn clean

# Build fresh JAR with PostgreSQL dependencies
echo "  🔨 Building Spring Boot JAR..."
mvn package -DskipTests

# Verify JAR was created
if [ ! -f "target/monthly-1.2.0.jar" ]; then
    echo "❌ ERROR: JAR file not found at target/monthly-1.2.0.jar"
    echo "Maven build may have failed"
    exit 1
fi

echo "  ✅ Backend JAR built successfully"

cd ..

# ==============================================================================
# STEP 2: Build Frontend React App
# ==============================================================================
echo ""
echo "🎨 Step 2: Building React Frontend"
echo "-----------------------------------"

cd frontend

# Clean previous builds
echo "  🧹 Cleaning previous React builds..."
rm -rf build
rm -rf public/static

# Install dependencies (if needed)
if [ ! -d "node_modules" ]; then
    echo "  📦 Installing npm dependencies..."
    npm install
fi

# Build React application
echo "  🔨 Building React application..."
npm run build-safe

# Copy build files to public directory (required by Dockerfile)
echo "  📁 Copying build files to public directory..."
cp -r build/* public/

# Verify single main JS file (prevent Chrome crashes)
JS_COUNT=$(find public/static/js -name "main.*.js" 2>/dev/null | wc -l)
if [ "$JS_COUNT" -ne 1 ]; then
    echo "❌ ERROR: Found $JS_COUNT main JS files! Expected exactly 1."
    echo "This will cause Chrome crashes. Clean build and try again."
    exit 1
fi

echo "  ✅ Frontend build completed successfully"

cd ..

# ==============================================================================
# STEP 3: Build Docker Images (Current Platform)
# ==============================================================================
echo ""
echo "🐳 Step 3: Building Docker Images (Local Platform)"
echo "---------------------------------------------------"

# Build backend image
echo "  🔨 Building backend image..."
docker build -t monthly-expense-backend:local ./backend

# Build frontend image  
echo "  🔨 Building frontend image..."
docker build -t monthly-expense-frontend:local ./frontend

echo "  ✅ Docker images built successfully"

# ==============================================================================
# STEP 4: Verification
# ==============================================================================
echo ""
echo "🔍 Step 4: Verifying Build Results"
echo "-----------------------------------"

# Verify images exist
echo "  📋 Built images:"
docker images | grep "monthly-expense"

# Show architecture information
echo ""
echo "  🏗️  Image architectures:"
docker inspect monthly-expense-backend:local --format "Backend: {{.Architecture}}"
docker inspect monthly-expense-frontend:local --format "Frontend: {{.Architecture}}"

echo ""
echo "🎉 Local build completed successfully!"
echo ""
echo "📝 Next steps:"
echo "   • Test images: docker-compose up"
echo "   • Load into Kind: kind load docker-image monthly-expense-backend:local"
echo "   • Deploy with Helm: helm install test-app ./monthly-expense-app"
echo ""