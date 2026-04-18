# Monthly Expense Tracker

A full-stack web application for tracking and analyzing monthly expenses. This application allows users to upload CSV files containing transaction data, view transaction details, and analyze spending patterns with advanced filtering capabilities.

## Features

- **Automated CSV Import**: Scheduler auto-imports CSV files from a year/month folder structure every 5 minutes
- **Transaction Management**: View and manage transaction records
- **Expense Analysis**: Visualize spending patterns with charts and summaries
- **Monthly Summaries**: View spending totals by month
- **Category Analysis**: See spending breakdown by category
- **🆕 Month Filtering**: Filter category totals by specific month/year or view all-time data
- **Data Persistence**: PostgreSQL database for production, H2 for development
- **Reset Functionality**: Option to reset all data
- **Containerized Deployment**: Docker and Kubernetes ready

## Technology Stack

### Backend (v1.3.4)
- Java 11
- Spring Boot 2.7.14
- Spring Data JPA
- PostgreSQL (production) / H2 Database (development)
- OpenCSV for CSV processing
- ShedLock for distributed scheduler locking
- Lombok

### Frontend (v1.2.3)
- React 18 with modern build system
- Bootstrap 5
- Chart.js for data visualization
- Axios for API calls

### Infrastructure
- **Multi-Architecture Docker Images**: Support for AMD64 (Intel/Windows) and ARM64 (Mac M1/M2)
- **Automated Build Scripts**: Streamlined local development and production release workflows
- **Registry-Based Deployment**: Automatic platform detection from Docker Hub
- **Docker & Docker Compose**: Full containerization with PostgreSQL
- **Kubernetes with Helm charts**: Production-ready orchestration
- **nginx**: Optimized frontend serving with security headers
- **nginx ingress controller**: External access and routing

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Node.js 18+ and npm (for frontend development)
- **Docker Desktop** or Docker Engine with buildx (for multi-architecture builds)
- **Docker Compose** v2.0+
- **Kubernetes cluster** (Kind, minikube, or cloud provider)
- **Helm** 3.x (for Kubernetes deployment)

### Running the Application

## 🚀 Quick Start Options

### Option 1: 🔥 **Local Development** (Fastest)
For daily development with automatic rebuilds:

```bash
# 1. Clone and navigate
git clone <repository-url>
cd MonthlyExpense

# 2. Build images for your platform
./docker-image-scripts/local/build-local.sh

# 3. Run all services
docker-compose up

# 4. Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8081
# Database: PostgreSQL on localhost:5432
```

### Option 2: 🌍 **Multi-Architecture Production** 
For team sharing and cross-platform compatibility:

```bash
# 1. Build and push multi-architecture images
./docker-image-scripts/production/push-multiarch-release.sh \
  --backend-version 1.3.1 \
  --frontend-version 1.2.1 \
  --registry YOUR_DOCKERHUB_USERNAME

# 2. Use registry images (automatic platform detection)
docker-compose -f docker-compose-multiarch.yaml up

# 3. Images automatically work on:
#    • Mac M1/M2 (ARM64)  
#    • Windows (AMD64)
#    • Linux (AMD64)
```

#### Development: Local Services
For backend development:
```bash
cd backend
mvn spring-boot:run
# Access at http://localhost:8081
```

For frontend development:
```bash
cd frontend  
npm start
# Access at http://localhost:3000 (if configured)
```

### Option 3: ☸️ **Kubernetes with Helm** 
For production deployment with automatic scaling and management:

```bash
# 1. Ensure Kubernetes cluster is running (Kind, minikube, or cloud)
kubectl cluster-info

# 2. Deploy using Helm with NodePort access
cd monthly-expense-app
helm dependency update
helm install monthly-expense . --namespace monthly-expense --create-namespace

# 3. Check deployment status
kubectl get pods,svc -n monthly-expense

# 4. Access application (NodePort - works with Kind)
# Frontend: http://localhost:30080
# Backend API: http://localhost:30081/api/transactions

# Alternative: Port-forward access
kubectl port-forward -n monthly-expense svc/frontend-service 8080:80 &
kubectl port-forward -n monthly-expense svc/backend-service 8081:8081 &
# Frontend: http://localhost:8080
# Backend API: http://localhost:8081
```

## 🛠️ Build Scripts Reference

The project includes organized build scripts for different scenarios:

```
docker-image-scripts/
├── local/build-local.sh           # Fast local development builds
├── production/push-multiarch-release.sh  # Multi-arch registry builds  
└── utilities/                     # Troubleshooting tools
    ├── build-multiarch.sh         # Alternative multi-arch build
    └── verify-build.sh            # Image verification
```

### Quick Commands

```bash
# Daily development
./build.sh && docker-compose up

# Release to team  
./release.sh --backend-version 1.3.1 --frontend-version 1.2.1 --registry YOUR_USERNAME
```

## CSV File Format

The application expects CSV files with the following columns:
- **Date**: Transaction date in yyyy-MM-dd format
- **Description**: Description of the transaction
- **Amount**: Transaction amount (numeric)
- **Category**: Category of the transaction

Example:
```
Date,Description,Amount,Category
2023-01-15,Grocery shopping,125.50,Groceries
2023-01-20,Monthly rent,1200.00,Housing
2023-01-25,Internet bill,65.00,Utilities
```

## API Endpoints

The application provides the following REST API endpoints:

- `GET /api/transactions`: Get all transactions
- `GET /api/transactions/month?month={month}&year={year}`: Get transactions for a specific month and year
- `GET /api/transactions/category/{category}`: Get transactions by category
- `GET /api/transactions/total`: Get total amount of all transactions
- `GET /api/transactions/monthly-totals?year={year}`: Get monthly totals for a specific year
- `🆕 GET /api/transactions/category-totals?month={month}&year={year}`: Get category totals with optional month/year filtering
- `DELETE /api/transactions/reset`: Reset all data

### Month Filtering Feature

The new month filtering functionality allows users to:
- View category totals for all time periods (default behavior)
- Filter category totals by specific month and year
- Use the frontend MonthYearSelector component for easy filtering

**Examples:**
```bash
# Get all-time category totals
GET /api/transactions/category-totals

# Get category totals for November 2025
GET /api/transactions/category-totals?month=11&year=2025

# Get category totals for the entire year 2025
GET /api/transactions/category-totals?year=2025
```

## Database Configuration

### Production (Docker/Kubernetes)
The application uses PostgreSQL in production environments:
- **Host**: PostgreSQL container/service
- **Database**: `monthly_expense_db`
- **Username**: `postgres` (Kubernetes) / `admin` (Docker Compose)
- **Password**: Managed via environment variables/secrets

### Development (Local)
For local development, H2 database is used:
- **Console**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/expense_db`
- **Username**: `sa`
- **Password**: `password`

## Version History

### v1.3.4 / v1.2.3 (Latest) - April 2026

**🔒 ShedLock — Distributed Scheduler Locking:**
- Added ShedLock to ensure only one replica runs the CSV scheduler at a time
- Eliminates duplicate transaction imports when running multiple backend replicas
- Lock state stored in PostgreSQL `shedlock` table (auto-created on startup)
- Backend safely scales to 2+ replicas

**♻️ Kubernetes Job for Reprocess:**
- Replaced per-pod `REPROCESS_ALL` flag with a dedicated `batch/v1` Kubernetes Job
- Trigger via `helm upgrade --set backend.reprocessJob.enabled=true`; disable with `=false`
- Job clears all transactions, resets `.csv.done` → `.csv`, then exits cleanly
- Eliminates race condition where multiple pods could simultaneously reimport all files

**🗑️ Upload endpoint removed:**
- Removed `POST /api/transactions/upload` REST endpoint and `FileUpload` UI card
- CSV imports are handled exclusively by the scheduler

---

### v1.3.2 / v1.2.2 - April 2026

**🤖 CSV Auto-Upload Scheduler:**
- **Automated CSV import**: Scheduler scans a year/month folder structure every 5 minutes (e.g. `2025/Jan/bank.csv`, `2026/Feb/bank.csv`)
- **Valid month folders**: `Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec`
- **Reprocess-all flag**: Set `CSV_REPROCESS_ALL=true` to clear the database and reimport all CSV files on startup
- **File lifecycle**: Processed files renamed to `.csv.done`; failed files copied to `unprocessed/` folder for retry
- **Volume mount**: Local CSV folder mounted into Docker and Kubernetes deployments via `hostPath`
- **Configurable interval**: Control scan frequency via `CSV_SCHEDULER_INTERVAL_MS` (default: 300000ms)

**🎨 UI v2 Design:**
- Flat cards with hero sparkline visualization
- Global filter strip for quick data filtering
- Drag-and-drop card layout

**☸️ Helm Chart Updates:**
- CSV scheduler env vars (`CSV_SCHEDULER_ENABLED`, `CSV_INPUT_FOLDER`, `CSV_REPROCESS_ALL`, etc.)
- `hostPath` volume mount for local CSV folder in Kubernetes
- Frontend: NodePort `30080`, Backend: NodePort `30081`

---

### v1.3.1 / v1.2.1 - January 2026
**🚀 Multi-Architecture Docker Revolution:**
- **✅ Multi-Platform Support**: Images now work seamlessly on Mac M1/M2 (ARM64), Windows/Linux (AMD64)
- **✅ Automatic Architecture Detection**: Docker automatically pulls the correct image for your platform
- **✅ Registry-Based Deployment**: Push once to Docker Hub, run anywhere with automatic platform selection
- **✅ Streamlined Build Scripts**: Organized `docker-image-scripts/` with local and production workflows

**🛠️ Enhanced Docker Infrastructure:**
- **Security Hardening**: All containers run as non-root users (uid 1001)
- **Health Checks**: Integrated Spring Boot Actuator for container health monitoring
- **PostgreSQL Verification**: Automatic verification prevents H2 fallback issues
- **Chrome Crash Prevention**: Frontend build verification ensures single main.js file
- **Optimized Images**: Multi-stage builds with minimal attack surface

**☸️ Kubernetes Production Readiness:**
- **NodePort Access**: Direct browser access via localhost:30080/30081
- **Helm Chart Updates**: Enhanced with multi-architecture registry images
- **Kind Cluster Support**: Complete development and testing workflow
- **Documentation**: Comprehensive deployment guide in KIND-DEPLOYMENT.md

**🔧 Developer Experience:**
- **Simplified Commands**: `./build.sh` for local, `./release.sh` for production
- **Automatic Rebuilds**: Fresh JAR builds prevent Docker cache issues  
- **Cross-Platform Testing**: Same images work on any developer machine
- **Team Collaboration**: Share exact same images across all platforms

**Backend Changes (v1.3.1):**
- Enhanced Dockerfiles with security best practices
- PostgreSQL driver verification during build
- Health check endpoints for Kubernetes readiness probes
- Non-root user execution for security compliance

**Frontend Changes (v1.2.1):**
- Multi-stage Docker build optimization
- Chrome crash prevention through build verification
- nginx security headers and performance tuning
- Automatic React Router integration

### Previous Versions
- **v1.2.0**: Month filtering for category totals, MonthYearSelector component  
- **v1.1.1**: Chrome crash prevention and build stability improvements
- **v1.0.0**: Initial release with core expense tracking functionality

## Troubleshooting

### DLL Errors
If you encounter DLL errors when running the application in Docker, it's likely due to missing native libraries in the slim JRE image. The application uses H2 database which requires certain native libraries to function properly.

Solution:
- The Dockerfile has been updated to use the full JRE image (`openjdk:11-jre`) instead of the slim version (`openjdk:11-jre-slim`).
- The full JRE image includes all necessary native libraries that H2 database needs.
- If you still encounter issues, try running the application outside of Docker using Maven.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
