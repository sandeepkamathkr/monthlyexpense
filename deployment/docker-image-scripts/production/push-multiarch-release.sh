#!/bin/bash

# ==============================================================================
# Production Multi-Architecture Release Script
# ==============================================================================
# Purpose: Build and push versioned multi-arch images for automatic platform detection
# Usage: ./push-multiarch-release.sh --version 1.3.0 --registry sandeepkamathkr
# Result: Users get correct architecture automatically when pulling images
# ==============================================================================

set -e

# Configuration
BACKEND_VERSION=""
FRONTEND_VERSION=""
REGISTRY=""
DRY_RUN=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --backend-version)
            BACKEND_VERSION="$2"
            shift 2
            ;;
        --frontend-version)
            FRONTEND_VERSION="$2"
            shift 2
            ;;
        --registry)
            REGISTRY="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --help)
            echo "Usage: $0 --backend-version VERSION --frontend-version VERSION --registry USERNAME"
            echo ""
            echo "Options:"
            echo "  --backend-version  Backend version (e.g., 1.3.0)"
            echo "  --frontend-version Frontend version (e.g., 1.2.0)"
            echo "  --registry        Docker Hub username"
            echo "  --dry-run         Show commands without executing"
            echo ""
            echo "Example:"
            echo "  $0 --backend-version 1.3.0 --frontend-version 1.2.0 --registry sandeepkamathkr"
            echo ""
            echo "Result: Creates images that automatically work on any platform:"
            echo "  - Mac M1/M2 pulls ARM64 version"
            echo "  - Windows/Linux pulls AMD64 version"
            echo "  - All from same tag (e.g., backend:1.3.0)"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate inputs
if [ -z "$BACKEND_VERSION" ] || [ -z "$FRONTEND_VERSION" ] || [ -z "$REGISTRY" ]; then
    echo "❌ ERROR: Missing required arguments"
    echo "Run with --help for usage"
    exit 1
fi

echo "🚀 Multi-Architecture Release Build"
echo "===================================="
echo "Backend Version:  $BACKEND_VERSION"
echo "Frontend Version: $FRONTEND_VERSION"
echo "Registry:        $REGISTRY"
echo "Platforms:       linux/amd64, linux/arm64"
echo ""

# Step 1: Setup buildx
echo "📦 Step 1: Setting up Docker buildx"
echo "------------------------------------"
if ! docker buildx ls | grep -q "multiarch-builder"; then
    docker buildx create --name multiarch-builder --use --bootstrap
else
    docker buildx use multiarch-builder
fi

# Step 2: Build backend
echo ""
echo "🔨 Step 2: Building Backend JAR"
echo "--------------------------------"
cd ../../../backend
mvn clean package -DskipTests
cd ..

# Step 3: Build frontend
echo ""
echo "🎨 Step 3: Building Frontend"
echo "-----------------------------"
cd frontend
rm -rf build public/static
npm run build-safe
cd ..

# Step 4: Build and push multi-arch images
echo ""
echo "🐳 Step 4: Building Multi-Arch Images"
echo "--------------------------------------"

# Backend with version tag and latest
echo "Building backend:$BACKEND_VERSION for all architectures..."
if [ "$DRY_RUN" = false ]; then
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        --tag $REGISTRY/monthly-expense-backend:$BACKEND_VERSION \
        --tag $REGISTRY/monthly-expense-backend:latest \
        --push \
        ./backend
else
    echo "[DRY RUN] Would build: $REGISTRY/monthly-expense-backend:$BACKEND_VERSION"
fi

# Frontend with version tag and latest
echo "Building frontend:$FRONTEND_VERSION for all architectures..."
if [ "$DRY_RUN" = false ]; then
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        --tag $REGISTRY/monthly-expense-frontend:$FRONTEND_VERSION \
        --tag $REGISTRY/monthly-expense-frontend:latest \
        --push \
        ./frontend
else
    echo "[DRY RUN] Would build: $REGISTRY/monthly-expense-frontend:$FRONTEND_VERSION"
fi

# Step 5: Verification
echo ""
echo "✅ Step 5: Verification"
echo "-----------------------"
if [ "$DRY_RUN" = false ]; then
    echo "Inspect multi-arch manifests:"
    docker buildx imagetools inspect $REGISTRY/monthly-expense-backend:$BACKEND_VERSION
    docker buildx imagetools inspect $REGISTRY/monthly-expense-frontend:$FRONTEND_VERSION
else
    echo "[DRY RUN] Would verify manifests"
fi

echo ""
echo "🎉 SUCCESS! Multi-arch images published"
echo ""
echo "📝 Users can now pull with automatic architecture detection:"
echo ""
echo "  On Mac M1/M2 (ARM64):"
echo "    docker pull $REGISTRY/monthly-expense-backend:$BACKEND_VERSION"
echo "    → Automatically gets ARM64 version"
echo ""
echo "  On Windows/Linux (AMD64):"
echo "    docker pull $REGISTRY/monthly-expense-backend:$BACKEND_VERSION"
echo "    → Automatically gets AMD64 version"
echo ""
echo "💡 Update Helm values.yaml:"
echo "  backend.image: $REGISTRY/monthly-expense-backend:$BACKEND_VERSION"
echo "  frontend.image: $REGISTRY/monthly-expense-frontend:$FRONTEND_VERSION"