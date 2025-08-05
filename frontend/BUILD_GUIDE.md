# Frontend Build Guide - Preventing Chrome Crashes

## 🚨 **CRITICAL: Chrome Crash Prevention**

This project previously suffered from Chrome "Aw, Snap! Error code 5" crashes due to multiple JavaScript files being loaded simultaneously. This guide ensures you never encounter this issue again.

## ✅ **Safe Build Commands**

### **Always Use These Commands:**

```bash
# Option 1: Use the safe npm script
npm run build-safe

# Option 2: Use the dedicated build script  
npm run build-clean

# Option 3: Manual safe build (if scripts fail)
npx rimraf public/* && cp src/index.html public/ && npm run build-react && cp -r build/* public/
```

### **Full Deployment:**

```bash
# Complete safe deployment
npm run deploy
```

## ❌ **Never Use These Commands:**

```bash
# DANGEROUS - Can cause file accumulation
npm run build-react && cp -r build/* public/

# DANGEROUS - No cleanup
cp -r build/* public/

# DANGEROUS - Appends instead of replacing
docker-compose build frontend  # without cleaning first
```

## 🔍 **How to Verify Your Build is Safe**

### **Check JavaScript File Count:**
```bash
npm run verify-build
# Should output: 1
```

### **Manual Verification:**
```bash
find public/static/js -name "main.*.js" | wc -l
# Should output: 1

# List the single JS file:
find public/static/js -name "main.*.js"
# Should show only one file like: public/static/js/main.XXXXXX.js
```

## 🛡️ **Prevention Mechanisms Built-In**

1. **Build Script Verification** - `build-clean.sh` automatically verifies only one JS file exists
2. **Docker Build Verification** - Dockerfile will fail if multiple JS files are detected
3. **npm Scripts** - All safe build commands include proper cleanup
4. **Package.json Commands** - New safe commands added to prevent manual errors

## 🚨 **Warning Signs**

If you see these, **STOP** and run a clean build:

- Chrome shows "Aw, Snap! Error code 5"
- Multiple `main.*.js` files in `public/static/js/`
- App works in Safari but crashes in Chrome
- Frontend container builds but shows blank pages

## 🔧 **Troubleshooting**

### **If Chrome Crashes Return:**

1. **Immediate Fix:**
   ```bash
   npm run build-safe
   docker-compose build frontend
   docker-compose up -d frontend
   ```

2. **Clear Browser Cache:**
   - Press `Cmd+Shift+R` (Mac) or `Ctrl+Shift+R` (Windows)
   - Or clear all browser data for localhost

3. **Verify Fix:**
   ```bash
   npm run verify-build
   # Should show: 1
   ```

### **If Build Scripts Fail:**

```bash
# Manual emergency cleanup
npx rimraf public/*
npx rimraf build/
cp src/index.html public/
npm run build-react
cp -r build/* public/
npm run verify-build
```

## 📋 **Development Workflow**

### **Safe Development Process:**

1. **Make Changes** to React components in `src/`
2. **Build Safely:** `npm run build-safe`  
3. **Test Locally:** `npm run start-react`
4. **Deploy:** `npm run deploy`
5. **Verify:** Check that only one JS file exists

### **Before Every Commit:**

```bash
npm run verify-build
# Must output: 1
```

## 🎯 **Key Principle**

**Always clean before building!** The root cause was file accumulation, so every build must start with a clean slate.

> **Remember:** One JavaScript file = Happy Chrome ✅  
> **Remember:** Multiple JavaScript files = Chrome crashes ❌