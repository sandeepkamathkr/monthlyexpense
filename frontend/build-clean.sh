#!/bin/bash

# Clean Build Script for Monthly Expense Frontend
# This script prevents the Chrome crash issue by ensuring clean builds

set -e  # Exit on any error

echo "🧹 Starting clean build process..."

# Step 1: Clean the public directory completely
echo "🗑️  Cleaning public directory..."
rm -rf public/*

# Step 2: Copy the source HTML template
echo "📄 Copying HTML template..."
cp src/index.html public/

# Step 3: Run React build
echo "⚛️  Building React application..."
npm run build-react

# Step 4: Copy fresh build files
echo "📦 Copying build files..."
cp -r build/* public/

# Step 5: Verify only one JS file exists
echo "🔍 Verifying build integrity..."
JS_COUNT=$(find public/static/js -name "main.*.js" | wc -l)
if [ "$JS_COUNT" -eq 1 ]; then
    JS_FILE=$(find public/static/js -name "main.*.js")
    echo "✅ Success! Only one JavaScript file: $(basename $JS_FILE)"
else
    echo "❌ ERROR: Found $JS_COUNT JavaScript files! This could cause crashes."
    find public/static/js -name "main.*.js"
    exit 1
fi

# Step 6: Clean up build directory to save space
echo "🧹 Cleaning up temporary build directory..."
rm -rf build/

echo "🎉 Clean build completed successfully!"
echo "🚀 Ready for Docker build or local testing"