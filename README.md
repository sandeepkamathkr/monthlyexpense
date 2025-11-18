# Monthly Expense Tracker

A full-stack web application for tracking and analyzing monthly expenses. This application allows users to upload CSV files containing transaction data, view transaction details, and analyze spending patterns with advanced filtering capabilities.

## Features

- **CSV Upload**: Upload transaction data from CSV files
- **Transaction Management**: View and manage transaction records
- **Expense Analysis**: Visualize spending patterns with charts and summaries
- **Monthly Summaries**: View spending totals by month
- **Category Analysis**: See spending breakdown by category
- **🆕 Month Filtering**: Filter category totals by specific month/year or view all-time data
- **Data Persistence**: PostgreSQL database for production, H2 for development
- **Reset Functionality**: Option to reset all data
- **Containerized Deployment**: Docker and Kubernetes ready

## Technology Stack

### Backend (v1.2.0)
- Java 11
- Spring Boot 2.7.14
- Spring Data JPA
- PostgreSQL (production) / H2 Database (development)
- OpenCSV for CSV processing
- Lombok

### Frontend (v1.1.0)
- React 18 with modern build system
- Bootstrap 5
- Chart.js for data visualization
- Axios for API calls

### Infrastructure
- Docker & Docker Compose
- Kubernetes with Helm charts
- nginx for frontend serving
- nginx ingress controller

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Node.js 18+ and npm (for frontend development)
- Docker and Docker Compose
- Kubernetes cluster (for production deployment)

### Running the Application

#### 🔥 Recommended: Docker Compose (Production-like)
This is the recommended way to run the full application:

```bash
# 1. Clone the repository
git clone <repository-url>
cd MonthlyExpense

# 2. Build frontend
cd frontend
rm -rf build && npx react-scripts build
rm -rf public/static && cp -r build/* public/

# 3. Build and run all services
cd ..
docker-compose up --build

# 4. Access the application
# Frontend: http://localhost
# Backend API: http://localhost:8081
# Database: PostgreSQL on localhost:5432
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

#### Kubernetes Deployment
For production deployment on Kubernetes:

```bash
# 1. Ensure Kubernetes cluster is running
kubectl cluster-info

# 2. Deploy using Helm
cd monthly-expense-app
helm dependency update
helm install monthly-expense .

# 3. Check deployment status
kubectl get pods,svc,ingress

# 4. Access via port-forward (for testing)
kubectl port-forward service/frontend-service 8080:80
# Frontend: http://localhost:8080

kubectl port-forward service/backend-service 8081:8081  
# Backend API: http://localhost:8081
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

- `POST /api/transactions/upload`: Upload a CSV file with transactions
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

### v1.2.0 (Latest) - November 2025
**New Features:**
- 🆕 **Month Filtering for Category Totals**: Users can now filter category spending analysis by specific month/year
- 🆕 **MonthYearSelector Component**: Interactive frontend component for easy date range selection
- 🆕 **Enhanced API**: Category totals endpoint now supports optional month/year parameters

**Infrastructure Improvements:**
- ✅ Updated Docker base image to Amazon Corretto for better compatibility
- ✅ Enhanced Helm charts with version 0.2.0
- ✅ Kubernetes deployment fully tested and documented
- ✅ Comprehensive Docker workflow documentation in CLAUDE.md
- ✅ DNS-ready ingress configuration

**Backend Changes (v1.2.0):**
- Enhanced TransactionController with month/year parameter support
- Improved TransactionService with conditional filtering logic
- Maintained full backward compatibility for existing API consumers

**Frontend Changes (v1.1.0):**
- New MonthYearSelector component with intuitive month/year dropdowns
- Updated state management for filter persistence
- Enhanced user experience with "All Months" option for comprehensive view

### Previous Versions
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
