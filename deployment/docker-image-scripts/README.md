# 🐳 Docker Build Scripts

Simple scripts for building Monthly Expense Tracker Docker images.

## 📁 Directory Structure

```
docker-image-scripts/
├── local/              # Daily development
│   └── build-local.sh  
├── production/         # Production releases
│   └── push-multiarch-release.sh
└── utilities/          # Optional tools (rarely needed)
    ├── build-multiarch.sh
    └── verify-build.sh
```

## 🚀 Quick Start

### 1️⃣ **Local Development** (Use this 90% of the time)

```bash
# Build images for your machine
./docker-image-scripts/local/build-local.sh

# Run application
docker-compose up
```

**What it does:**
- Builds images for YOUR computer's architecture only
- Tags images as `:local`
- Fast build for testing

### 2️⃣ **Production Release** (For sharing with team)

```bash
# Build and push to Docker Hub
./docker-image-scripts/production/push-multiarch-release.sh \
  --backend-version 1.3.0 \
  --frontend-version 1.2.0 \
  --registry YOUR_DOCKERHUB_USERNAME
```

**What it does:**
- Builds for ALL architectures (Mac M1/M2, Windows, Linux)
- Pushes to Docker Hub
- Anyone can pull and get correct architecture automatically

## 📊 When to Use Which Script?

| Scenario | Script to Use | Command |
|----------|--------------|---------|
| Testing code changes | `local/build-local.sh` | `./docker-image-scripts/local/build-local.sh` |
| Debugging locally | `local/build-local.sh` | `./docker-image-scripts/local/build-local.sh` |
| Releasing new version | `production/push-multiarch-release.sh` | See production example above |
| Sharing with team | `production/push-multiarch-release.sh` | See production example above |

## 💻 Platform Support

After pushing to Docker Hub with the production script:

- **Mac M1/M2** → Automatically pulls ARM64 version
- **Windows** → Automatically pulls AMD64 version  
- **Linux** → Automatically pulls AMD64 version

No manual architecture selection needed!

## 🎯 Example Workflow

### Daily Development
```bash
# 1. Make code changes
# 2. Build images
./docker-image-scripts/local/build-local.sh

# 3. Test
docker-compose up

# 4. Repeat...
```

### Release Process
```bash
# 1. Test thoroughly with local build
./docker-image-scripts/local/build-local.sh
docker-compose up

# 2. Push to registry for team
./docker-image-scripts/production/push-multiarch-release.sh \
  --backend-version 1.3.0 \
  --frontend-version 1.2.0 \
  --registry mycompany

# 3. Team members can now use:
docker pull mycompany/monthly-expense-backend:1.3.0
# Gets correct architecture automatically!
```

## ❓ Troubleshooting

If you have issues, check the `utilities/` folder:
- `verify-build.sh` - Validates your Docker images
- `build-multiarch.sh` - Alternative multi-arch build (without push)

## 📝 Notes

- Always run scripts from the project root directory
- Backend version is in `backend/pom.xml`
- Frontend version is in `frontend/package.json`
- Use semantic versioning (e.g., 1.2.3)