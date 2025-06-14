# Monthly Expense Tracker Frontend

This directory contains the frontend code for the Monthly Expense Tracker application.

## Building the Docker Image

The application can be containerized using Docker. Due to potential space constraints during the build process, we've optimized the Dockerfile and provided a build script with settings to minimize disk space usage.

### Prerequisites

- Docker installed on your system
- Sufficient disk space (at least 500MB recommended)

### Option 1: Using the Optimized Build Script

We recommend using the provided build script which includes optimizations to minimize disk space usage:

```bash
# Make the script executable if it's not already
chmod +x docker-build.sh

# Run the build script
./docker-build.sh
```

This script will:
1. Clean up your Docker environment to free up space
2. Build the image with optimized flags to minimize space usage

### Option 2: Manual Build

If you prefer to build manually, you can use the following command with optimized flags:

```bash
docker build \
  --no-cache \
  --force-rm \
  --rm=true \
  --compress \
  -t monthly-expense-frontend:latest \
  .
```

### Troubleshooting

If you encounter a "no space left on device" error:

1. Clean up Docker resources:
   ```bash
   docker system prune -af
   ```

2. Check available disk space:
   ```bash
   df -h
   ```

3. If needed, free up additional space on your system before building.

## Running the Container

Once built, you can run the container with:

```bash
docker run -d -p 80:80 monthly-expense-frontend:latest
```

The application will be available at http://localhost:80