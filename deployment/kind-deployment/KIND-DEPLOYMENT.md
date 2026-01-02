# Kind Deployment Instructions

## Multi-Architecture Docker Images for Kind Kubernetes

This document provides complete instructions for deploying the Monthly Expense Tracker application to Kind (Kubernetes in Docker) using the multi-architecture Docker images we've built.

## Prerequisites

### Required Software
- **Docker**: Docker Desktop (includes buildx) or Docker Engine + buildx plugin
- **Kind**: [Installation Guide](https://kind.sigs.k8s.io/docs/user/quick-start/#installation)
- **kubectl**: [Installation Guide](https://kubernetes.io/docs/tasks/tools/)
- **Helm**: [Installation Guide](https://helm.sh/docs/intro/install/)

### Verify Prerequisites
```bash
# Check Docker and buildx
docker --version
docker buildx version

# Check Kind
kind version

# Check kubectl
kubectl version --client

# Check Helm
helm version
```

## Platform-Specific Instructions

### macOS (Intel and Apple Silicon)

#### 1. Build Images
```bash
# For local development (current platform)
./docker-image-scripts/local/build-local.sh

# For multi-architecture (all platforms)
./docker-image-scripts/production/push-multiarch-release.sh

# Verify build
./docker-image-scripts/utilities/verify-build.sh --tag local
```

#### 2. Create Kind Cluster
```bash
# Create cluster with custom config
kind create cluster --name monthly-expense --config kind-config.yaml

# Verify cluster
kubectl cluster-info --context kind-monthly-expense
```

#### 3. Load Images into Kind
```bash
# Load both images
kind load docker-image monthly-expense-backend:local --name monthly-expense
kind load docker-image monthly-expense-frontend:local --name monthly-expense

# Verify images loaded
kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | xargs -I {} docker exec {} crictl images | grep monthly-expense
```

#### 4. Deploy with Helm
```bash
cd monthly-expense-app

# Add PostgreSQL chart repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Update dependencies
helm dependency update .

# Deploy application
helm install monthly-expense .

# Verify deployment
kubectl get pods
kubectl get services
```

#### 5. Access Application
```bash
# Port forward to access frontend
kubectl port-forward svc/frontend-service 8080:80 &

# Port forward to access backend API
kubectl port-forward svc/backend-service 8081:8081 &

# Open application
open http://localhost:8080
```

### Windows

#### 1. Build Images (PowerShell)
```powershell
# Build for local development
.\build-local.ps1

# Or build multi-architecture
.\build-multiarch.ps1

# Verify build
.\verify-build.ps1 -Tag local
```

#### 2. Create Kind Cluster
```powershell
# Create cluster
kind create cluster --name monthly-expense --config kind-config.yaml

# Verify cluster
kubectl cluster-info --context kind-monthly-expense
```

#### 3. Load Images
```powershell
# Load images into Kind
kind load docker-image monthly-expense-backend:local --name monthly-expense
kind load docker-image monthly-expense-frontend:local --name monthly-expense
```

#### 4. Deploy Application
```powershell
cd monthly-expense-app

# Add Helm repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Update dependencies and deploy
helm dependency update .
helm install monthly-expense .
```

#### 5. Access Application
```powershell
# Port forward (run in separate terminals)
kubectl port-forward svc/frontend-service 8080:80
kubectl port-forward svc/backend-service 8081:8081

# Open browser
start http://localhost:8080
```

### Linux

#### 1. Build Images
```bash
# Make scripts executable
chmod +x build-local.sh build-multiarch.sh verify-build.sh

# Build images
./docker-image-scripts/local/build-local.sh

# Verify
./docker-image-scripts/utilities/verify-build.sh --tag local
```

#### 2. Create and Deploy
```bash
# Create Kind cluster
kind create cluster --name monthly-expense --config kind-config.yaml

# Load images
kind load docker-image monthly-expense-backend:local --name monthly-expense
kind load docker-image monthly-expense-frontend:local --name monthly-expense

# Deploy with Helm
cd monthly-expense-app
helm repo add bitnami https://charts.bitnami.com/bitnami
helm dependency update .
helm install monthly-expense .

# Access application
kubectl port-forward svc/frontend-service 8080:80 &
kubectl port-forward svc/backend-service 8081:8081 &

# Open browser
xdg-open http://localhost:8080  # or firefox http://localhost:8080
```

## Architecture Verification

### Check Image Architectures
```bash
# Inspect local images
docker inspect monthly-expense-backend:local --format "{{.Architecture}}"
docker inspect monthly-expense-frontend:local --format "{{.Architecture}}"

# Check images in Kind cluster
NODE_NAME=$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')
docker exec $NODE_NAME crictl images | grep monthly-expense
```

### Verify Multi-Architecture Registry Images
```bash
# If you pushed to registry with --push
docker buildx imagetools inspect monthly-expense-backend:multi-arch
docker buildx imagetools inspect monthly-expense-frontend:multi-arch
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Images Not Found in Kind
**Problem**: Pods show `ErrImageNeverPull` or `ImagePullBackOff`

**Solution**:
```bash
# Verify images are loaded
kind load docker-image monthly-expense-backend:local --name monthly-expense
kind load docker-image monthly-expense-frontend:local --name monthly-expense

# Check if images exist in cluster
kubectl get nodes -o jsonpath='{.items[0].metadata.name}' | xargs -I {} docker exec {} crictl images | grep monthly
```

#### 2. Architecture Mismatch
**Problem**: Images don't work on different platforms

**Solution**:
```bash
# Build for specific platform
docker buildx build --platform linux/amd64 -t monthly-expense-backend:amd64 ./backend
docker buildx build --platform linux/arm64 -t monthly-expense-backend:arm64 ./backend

# Or use multi-arch script
./docker-image-scripts/production/push-multiarch-release.sh --push --registry YOUR_USERNAME
```

#### 3. PostgreSQL Connection Errors
**Problem**: Backend can't connect to database

**Solution**:
```bash
# Check PostgreSQL pod status
kubectl get pods | grep postgresql

# Check backend logs
kubectl logs deployment/backend

# Verify database configuration
kubectl describe configmap
kubectl describe secret
```

#### 4. Chrome Crashes on Frontend
**Problem**: Frontend causes browser crashes

**Solution**:
```bash
# Check for multiple main JS files
kubectl exec deployment/frontend -- find /usr/share/nginx/html/static/js -name "main.*.js" | wc -l

# Should return 1. If not, rebuild:
cd frontend
rm -rf build public/static
npm run build-safe
docker build -t monthly-expense-frontend:local .
```

#### 5. Health Check Failures
**Problem**: Pods fail health checks

**Solution**:
```bash
# Check pod status and events
kubectl describe pod <pod-name>

# Check application logs
kubectl logs <pod-name>

# Test health endpoint manually
kubectl port-forward <pod-name> 8081:8081
curl http://localhost:8081/actuator/health
```

### Useful Debugging Commands

```bash
# Check all resources
kubectl get all

# Describe deployment issues
kubectl describe deployment backend
kubectl describe deployment frontend

# Check events
kubectl get events --sort-by=.metadata.creationTimestamp

# Get detailed pod information
kubectl describe pod <pod-name>

# Access pod shell
kubectl exec -it <pod-name> -- /bin/sh

# Check image details
docker inspect monthly-expense-backend:local

# View image layers
docker history monthly-expense-backend:local
```

## Development Workflow

### Daily Development
1. **Make code changes**
2. **Rebuild images**: `./docker-image-scripts/local/build-local.sh`
3. **Update Kind**: `kind load docker-image monthly-expense-*:local --name monthly-expense`
4. **Restart pods**: `kubectl rollout restart deployment/backend deployment/frontend`
5. **Test changes**: Access via port-forward

### Testing Multi-Architecture
1. **Build multi-arch**: `./docker-image-scripts/production/push-multiarch-release.sh`
2. **Push to registry**: `./docker-image-scripts/production/push-multiarch-release.sh --push --registry YOUR_USERNAME`
3. **Update Helm values** to use registry images
4. **Deploy to different platforms**

### Production Deployment
1. **Build and push**: `./docker-image-scripts/production/push-multiarch-release.sh --push --registry YOUR_REGISTRY`
2. **Update Helm charts** with registry image references
3. **Deploy to production Kubernetes**
4. **Monitor and verify**

## Performance Considerations

### Resource Requirements
- **Backend**: Minimum 256Mi memory, 100m CPU
- **Frontend**: Minimum 64Mi memory, 50m CPU  
- **PostgreSQL**: Minimum 512Mi memory, 200m CPU

### Scaling
```bash
# Scale backend horizontally
kubectl scale deployment backend --replicas=3

# Monitor resource usage
kubectl top pods
kubectl top nodes
```

## Security Notes

- All images run as non-root users (uid 1001)
- Health checks verify application functionality
- PostgreSQL driver verification prevents runtime failures
- Nginx configuration handles React Router properly
- CORS configured for development and production

## Next Steps

1. **Set up CI/CD** with GitHub Actions for automated builds
2. **Deploy to cloud Kubernetes** (EKS, GKE, AKS)
3. **Add monitoring** with Prometheus/Grafana
4. **Configure ingress** for external access
5. **Set up logging** with ELK stack or similar