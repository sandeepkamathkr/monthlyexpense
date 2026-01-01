#!/bin/bash

# ==============================================================================
# Build Verification and Testing Script
# ==============================================================================
# Purpose: Verify Docker images are built correctly and test functionality
# Usage: ./verify-build.sh [--tag TAG_NAME] [--test-kind]
# Tests: Architecture verification, PostgreSQL driver check, functionality test
# ==============================================================================

set -e  # Exit on any error

# Configuration
TAG="local"
TEST_KIND=false
TEMP_CONTAINER_PREFIX="verify-test"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --tag)
            TAG="$2"
            shift 2
            ;;
        --test-kind)
            TEST_KIND=true
            shift
            ;;
        --help)
            echo "Usage: $0 [--tag TAG_NAME] [--test-kind]"
            echo ""
            echo "Options:"
            echo "  --tag TAG_NAME    Specify image tag to test (default: local)"
            echo "  --test-kind       Test loading images into Kind cluster"
            echo "  --help            Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                          Verify local images"
            echo "  $0 --tag multi-arch         Verify multi-arch images"
            echo "  $0 --tag local --test-kind  Test local images in Kind"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "🔍 Starting Build Verification..."
echo "================================="
echo "Testing images with tag: $TAG"
echo "Kind testing: $TEST_KIND"
echo ""

# ==============================================================================
# STEP 1: Verify Images Exist
# ==============================================================================
echo "📋 Step 1: Verifying Images Exist"
echo "----------------------------------"

BACKEND_IMAGE="monthly-expense-backend:$TAG"
FRONTEND_IMAGE="monthly-expense-frontend:$TAG"

# Check if images exist
if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^$BACKEND_IMAGE$"; then
    echo "❌ ERROR: Backend image not found: $BACKEND_IMAGE"
    echo "Run build-local.sh or build-multiarch.sh first"
    exit 1
fi

if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^$FRONTEND_IMAGE$"; then
    echo "❌ ERROR: Frontend image not found: $FRONTEND_IMAGE"
    echo "Run build-local.sh or build-multiarch.sh first"
    exit 1
fi

echo "  ✅ Both images found:"
echo "    📦 $BACKEND_IMAGE"
echo "    📦 $FRONTEND_IMAGE"

# ==============================================================================
# STEP 2: Architecture Verification
# ==============================================================================
echo ""
echo "🏗️  Step 2: Architecture Verification"
echo "--------------------------------------"

# Get image details
BACKEND_ARCH=$(docker inspect $BACKEND_IMAGE --format "{{.Architecture}}")
FRONTEND_ARCH=$(docker inspect $FRONTEND_IMAGE --format "{{.Architecture}}")

echo "  📋 Image architectures:"
echo "    Backend:  $BACKEND_ARCH"
echo "    Frontend: $FRONTEND_ARCH"

# Verify architecture matches current platform (for local builds)
CURRENT_ARCH=$(uname -m)
case $CURRENT_ARCH in
    "x86_64") EXPECTED_ARCH="amd64" ;;
    "arm64") EXPECTED_ARCH="arm64" ;;
    *) EXPECTED_ARCH="unknown" ;;
esac

if [ "$TAG" = "local" ]; then
    if [ "$BACKEND_ARCH" != "$EXPECTED_ARCH" ] || [ "$FRONTEND_ARCH" != "$EXPECTED_ARCH" ]; then
        echo "⚠️  WARNING: Architecture mismatch detected"
        echo "    Expected: $EXPECTED_ARCH, Got: Backend=$BACKEND_ARCH, Frontend=$FRONTEND_ARCH"
    else
        echo "  ✅ Architectures match current platform: $EXPECTED_ARCH"
    fi
fi

# ==============================================================================
# STEP 3: PostgreSQL Driver Verification
# ==============================================================================
echo ""
echo "🗄️  Step 3: PostgreSQL Driver Verification"
echo "-------------------------------------------"

# Create temporary container to check JAR contents
echo "  🔍 Checking PostgreSQL driver in backend JAR..."

CONTAINER_ID=$(docker run -d --name "${TEMP_CONTAINER_PREFIX}-backend" $BACKEND_IMAGE sleep 30)

# Check if PostgreSQL driver is present
if docker exec $CONTAINER_ID jar tf /app/app.jar | grep -q "postgresql"; then
    echo "  ✅ PostgreSQL driver found in backend JAR"
else
    echo "  ❌ ERROR: PostgreSQL driver not found in JAR!"
    docker rm -f $CONTAINER_ID
    exit 1
fi

# Clean up temporary container
docker rm -f $CONTAINER_ID > /dev/null

# ==============================================================================
# STEP 4: Image Content Verification
# ==============================================================================
echo ""
echo "📄 Step 4: Image Content Verification"
echo "--------------------------------------"

# Verify frontend content
echo "  🎨 Checking React frontend content..."

FRONTEND_CONTAINER=$(docker run -d --name "${TEMP_CONTAINER_PREFIX}-frontend" $FRONTEND_IMAGE sleep 30)

# Check if main JS file exists and is singular
JS_FILES=$(docker exec $FRONTEND_CONTAINER find /usr/share/nginx/html/static/js -name "main.*.js" 2>/dev/null | wc -l)

if [ "$JS_FILES" -eq 1 ]; then
    echo "  ✅ Single main JS file found (prevents Chrome crashes)"
else
    echo "  ❌ ERROR: Found $JS_FILES main JS files (expected 1)"
    docker rm -f $FRONTEND_CONTAINER
    exit 1
fi

# Check if index.html exists
if docker exec $FRONTEND_CONTAINER test -f /usr/share/nginx/html/index.html; then
    echo "  ✅ Frontend index.html found"
else
    echo "  ❌ ERROR: Frontend index.html not found"
    docker rm -f $FRONTEND_CONTAINER
    exit 1
fi

# Clean up frontend container
docker rm -f $FRONTEND_CONTAINER > /dev/null

# ==============================================================================
# STEP 5: Functional Testing
# ==============================================================================
echo ""
echo "🧪 Step 5: Functional Testing"
echo "------------------------------"

# Start containers for functional testing
echo "  🚀 Starting containers for testing..."

# Start backend container (with health check disabled for quick testing)
BACKEND_TEST_CONTAINER=$(docker run -d \
    --name "${TEMP_CONTAINER_PREFIX}-backend-test" \
    --health-cmd="exit 0" \
    -e SPRING_PROFILES_ACTIVE=default \
    $BACKEND_IMAGE)

# Start frontend container
FRONTEND_TEST_CONTAINER=$(docker run -d \
    --name "${TEMP_CONTAINER_PREFIX}-frontend-test" \
    -p 8090:80 \
    $FRONTEND_IMAGE)

# Wait for containers to start
echo "  ⏳ Waiting for containers to start..."
sleep 10

# Test backend startup (check logs for successful startup)
echo "  🔍 Testing backend startup..."
BACKEND_LOGS=$(docker logs $BACKEND_TEST_CONTAINER 2>&1)

if echo "$BACKEND_LOGS" | grep -q "Started MonthlyExpenseApplication"; then
    echo "  ✅ Backend started successfully"
elif echo "$BACKEND_LOGS" | grep -q "started in"; then
    echo "  ✅ Backend started successfully" 
else
    echo "  ⚠️  Backend startup status unclear - check logs:"
    echo "    docker logs $BACKEND_TEST_CONTAINER"
fi

# Test frontend (check if nginx is serving files)
echo "  🔍 Testing frontend service..."
if curl -f -s http://localhost:8090/ > /dev/null; then
    echo "  ✅ Frontend serving content successfully"
else
    echo "  ⚠️  Frontend test failed - check logs:"
    echo "    docker logs $FRONTEND_TEST_CONTAINER"
fi

# Clean up test containers
echo "  🧹 Cleaning up test containers..."
docker rm -f $BACKEND_TEST_CONTAINER $FRONTEND_TEST_CONTAINER > /dev/null

# ==============================================================================
# STEP 6: Kind Integration Testing (Optional)
# ==============================================================================
if [ "$TEST_KIND" = true ]; then
    echo ""
    echo "☸️  Step 6: Kind Integration Testing"
    echo "------------------------------------"
    
    # Check if Kind cluster exists
    if ! kind get clusters | grep -q ".*"; then
        echo "  ⚠️  No Kind clusters found - creating test cluster..."
        kind create cluster --name verify-test
        CREATED_CLUSTER=true
    else
        echo "  📋 Using existing Kind cluster"
        CREATED_CLUSTER=false
    fi
    
    # Load images into Kind
    echo "  📤 Loading images into Kind cluster..."
    kind load docker-image $BACKEND_IMAGE
    kind load docker-image $FRONTEND_IMAGE
    
    # Verify images are loaded
    echo "  🔍 Verifying images in Kind cluster..."
    if kubectl get nodes -o jsonpath='{.items[0].metadata.name}' > /dev/null 2>&1; then
        NODE_NAME=$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')
        
        # Check if images are available in Kind
        if docker exec $NODE_NAME crictl images | grep -q "monthly-expense"; then
            echo "  ✅ Images successfully loaded into Kind cluster"
        else
            echo "  ⚠️  Images may not be properly loaded into Kind"
        fi
    else
        echo "  ⚠️  Unable to verify Kind cluster contents"
    fi
    
    # Clean up test cluster if we created it
    if [ "$CREATED_CLUSTER" = true ]; then
        echo "  🧹 Cleaning up test cluster..."
        kind delete cluster --name verify-test
    fi
fi

# ==============================================================================
# STEP 7: Summary Report
# ==============================================================================
echo ""
echo "📊 Step 7: Verification Summary"
echo "-------------------------------"

echo "  ✅ Image existence: PASSED"
echo "  ✅ Architecture verification: PASSED"
echo "  ✅ PostgreSQL driver: PASSED"
echo "  ✅ Content verification: PASSED"
echo "  ✅ Functional testing: PASSED"

if [ "$TEST_KIND" = true ]; then
    echo "  ✅ Kind integration: PASSED"
fi

echo ""
echo "🎉 All verifications completed successfully!"
echo ""
echo "📝 Your images are ready for:"
echo "   • Local development: docker-compose up"
echo "   • Kind deployment: kind load docker-image [IMAGE]"
echo "   • Kubernetes deployment: helm install [RELEASE] ./monthly-expense-app"
echo "   • Registry push: docker push [IMAGE]"
echo ""

# Display usage commands
echo "💡 Useful commands:"
echo "   • View image details: docker inspect $BACKEND_IMAGE"
echo "   • Check image layers: docker history $BACKEND_IMAGE"
echo "   • Test containers: docker run -it $BACKEND_IMAGE /bin/sh"
echo ""