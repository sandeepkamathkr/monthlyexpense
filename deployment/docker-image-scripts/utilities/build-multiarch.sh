#!/bin/bash

# ==============================================================================
# Multi-Architecture Build Script
# ==============================================================================
# Purpose: Build Docker images for multiple architectures (Mac, Windows, Linux)
# Usage: ./build-multiarch.sh [--push] [--registry USERNAME/REPONAME]
# Platforms: linux/amd64, linux/arm64, windows/amd64
# Requirements: Docker buildx (included in Docker Desktop)
# ==============================================================================

set -e  # Exit on any error

# Configuration
DEFAULT_TAG="multi-arch"
PLATFORMS="linux/amd64,linux/arm64"
PUSH_TO_REGISTRY=false
REGISTRY_PREFIX=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --push)
            PUSH_TO_REGISTRY=true
            shift
            ;;
        --registry)
            REGISTRY_PREFIX="$2/"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [--push] [--registry USERNAME/REPONAME]"
            echo ""
            echo "Options:"
            echo "  --push                 Push images to Docker registry"
            echo "  --registry PREFIX      Use custom registry prefix (e.g., 'myuser')"
            echo "  --help                 Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                     Build locally for multiple architectures"
            echo "  $0 --push             Build and push to Docker Hub"
            echo "  $0 --registry myuser   Build with custom registry prefix"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "🚀 Starting Multi-Architecture Build..."
echo "======================================="
echo "Platforms: $PLATFORMS"
echo "Registry: ${REGISTRY_PREFIX:-Docker Hub default}"
echo "Push to registry: $PUSH_TO_REGISTRY"
echo ""

# ==============================================================================
# STEP 1: Verify Prerequisites
# ==============================================================================
echo "🔍 Step 1: Verifying Prerequisites"
echo "-----------------------------------"

# Check if Docker buildx is available
if ! docker buildx version >/dev/null 2>&1; then
    echo "❌ ERROR: Docker buildx not found"
    echo "Please install Docker Desktop or enable buildx plugin"
    exit 1
fi

# Check if builder exists, create if not
if ! docker buildx ls | grep -q "multiarch-builder"; then
    echo "  🔨 Creating multiarch-builder instance..."
    docker buildx create --name multiarch-builder --use --bootstrap
else
    echo "  ✅ Using existing multiarch-builder instance"
    docker buildx use multiarch-builder
fi

# Verify platforms are supported
echo "  📋 Available platforms:"
docker buildx ls | grep multiarch-builder

echo "  ✅ Prerequisites verified"

# ==============================================================================
# STEP 2: Clean and Build Backend JAR
# ==============================================================================
echo ""
echo "📦 Step 2: Building Backend JAR (Fresh Build)"
echo "----------------------------------------------"

cd backend

# Clean previous builds to prevent cache issues
echo "  🧹 Cleaning previous Maven builds..."
mvn clean

# Build fresh JAR with PostgreSQL dependencies
echo "  🔨 Building Spring Boot JAR..."
mvn package -DskipTests

# Verify JAR was created and contains PostgreSQL driver
if [ ! -f "target/monthly-1.2.0.jar" ]; then
    echo "❌ ERROR: JAR file not found at target/monthly-1.2.0.jar"
    exit 1
fi

# Verify PostgreSQL driver is included
if ! jar tf target/monthly-1.2.0.jar | grep -q postgresql; then
    echo "❌ ERROR: PostgreSQL driver not found in JAR!"
    echo "Check Maven dependencies in pom.xml"
    exit 1
fi

echo "  ✅ Backend JAR built with PostgreSQL driver"

cd ..

# ==============================================================================
# STEP 3: Build Frontend React App
# ==============================================================================
echo ""
echo "🎨 Step 3: Building React Frontend"
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
npm run build

# Copy build files to public directory (required by Dockerfile)
echo "  📁 Copying build files to public directory..."
cp -r build/* public/

# Critical verification: Check for single main JS file
JS_COUNT=$(find public/static/js -name "main.*.js" 2>/dev/null | wc -l)
if [ "$JS_COUNT" -ne 1 ]; then
    echo "❌ ERROR: Found $JS_COUNT main JS files! Expected exactly 1."
    echo "This will cause Chrome crashes. Clean build and try again."
    echo "Solution: rm -rf build && npm run build"
    exit 1
fi

echo "  ✅ Frontend build completed with single main JS file"

cd ..

# ==============================================================================
# STEP 4: Build Multi-Architecture Docker Images
# ==============================================================================
echo ""
echo "🐳 Step 4: Building Multi-Architecture Images"
echo "----------------------------------------------"

# Configure push options
if [ "$PUSH_TO_REGISTRY" = true ]; then
    PUSH_OPTION="--push"
    ACTION_TEXT="Building and pushing"
else
    PUSH_OPTION="--load"
    ACTION_TEXT="Building"
fi

# Build backend image for multiple architectures
echo "  🔨 $ACTION_TEXT backend image for: $PLATFORMS"
docker buildx build \
    --platform $PLATFORMS \
    --tag ${REGISTRY_PREFIX}monthly-expense-backend:$DEFAULT_TAG \
    $PUSH_OPTION \
    ./backend

# Build frontend image for multiple architectures
echo "  🔨 $ACTION_TEXT frontend image for: $PLATFORMS"
docker buildx build \
    --platform $PLATFORMS \
    --tag ${REGISTRY_PREFIX}monthly-expense-frontend:$DEFAULT_TAG \
    $PUSH_OPTION \
    ./frontend

echo "  ✅ Multi-architecture images built successfully"

# ==============================================================================
# STEP 5: Verification
# ==============================================================================
echo ""
echo "🔍 Step 5: Verifying Build Results"
echo "-----------------------------------"

if [ "$PUSH_TO_REGISTRY" = true ]; then
    echo "  📤 Images pushed to registry:"
    echo "    ${REGISTRY_PREFIX}monthly-expense-backend:$DEFAULT_TAG"
    echo "    ${REGISTRY_PREFIX}monthly-expense-frontend:$DEFAULT_TAG"
    echo ""
    echo "  🔍 Verify multi-architecture manifests:"
    echo "    docker buildx imagetools inspect ${REGISTRY_PREFIX}monthly-expense-backend:$DEFAULT_TAG"
    echo "    docker buildx imagetools inspect ${REGISTRY_PREFIX}monthly-expense-frontend:$DEFAULT_TAG"
else
    echo "  📋 Local multi-architecture images:"
    docker images | grep "monthly-expense" || echo "  (Images built for multiple platforms but not loaded locally)"
    echo ""
    echo "  💡 Note: Multi-arch images are not loaded to local Docker"
    echo "     Use --push to push to registry, or build-local.sh for local testing"
fi

# ==============================================================================
# STEP 6: Architecture Verification Commands
# ==============================================================================
echo ""
echo "🏗️  Step 6: Architecture Verification"
echo "--------------------------------------"

if [ "$PUSH_TO_REGISTRY" = true ]; then
    echo "  Run these commands to verify architectures:"
    echo ""
    echo "  # Check backend architectures:"
    echo "  docker buildx imagetools inspect ${REGISTRY_PREFIX}monthly-expense-backend:$DEFAULT_TAG"
    echo ""
    echo "  # Check frontend architectures:"
    echo "  docker buildx imagetools inspect ${REGISTRY_PREFIX}monthly-expense-frontend:$DEFAULT_TAG"
    echo ""
    echo "  # Pull specific architecture (example):"
    echo "  docker pull --platform linux/arm64 ${REGISTRY_PREFIX}monthly-expense-backend:$DEFAULT_TAG"
else
    echo "  Multi-architecture build completed but not pushed to registry."
    echo "  Use --push option to push images and verify architectures."
fi

echo ""
echo "🎉 Multi-architecture build completed successfully!"
echo ""
echo "📝 Next steps:"
if [ "$PUSH_TO_REGISTRY" = true ]; then
    echo "   • Update Helm charts to use: ${REGISTRY_PREFIX}monthly-expense-*:$DEFAULT_TAG"
    echo "   • Deploy to Kubernetes: helm install app ./monthly-expense-app"
    echo "   • Verify deployment: kubectl get pods"
else
    echo "   • Push to registry: $0 --push --registry YOUR_USERNAME"
    echo "   • Or test locally: ./build-local.sh && kind load docker-image ..."
fi
echo ""