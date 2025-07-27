# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Code Documentation Requirements

**IMPORTANT**: When suggesting any code changes, additions, or new files, always provide:

1. **Step-by-Step Explanation**: Detailed explanation of what each line/step does
2. **Reasoning**: Why this specific approach was chosen over alternatives
3. **Official Documentation**: Links to official documentation proving the approach is correct and follows best practices

This ensures all code suggestions are educational, well-reasoned, and validated by authoritative sources.

## Project Overview

Monthly Expense Tracker is a full-stack web application for tracking and analyzing monthly expenses. The application allows users to upload CSV files with transaction data, view transaction details, and analyze spending patterns with charts and summaries.

## Architecture

### Multi-Service Architecture
- **Backend**: Java Spring Boot REST API service (port 8081)
- **Frontend**: React application served via nginx (port 80) 
- **Database**: PostgreSQL for production, H2 for local development
- **Deployment**: Kubernetes via Helm charts, Docker Compose for local development

### Key Technologies
- **Backend**: Java 11, Spring Boot 2.7.14, Spring Data JPA, OpenCSV, Lombok
- **Frontend**: React 18, Bootstrap 5, Chart.js, Axios
- **Database**: PostgreSQL (production), H2 (development)
- **Infrastructure**: Docker, Kubernetes, Helm

## Development Commands

### Backend (Java Spring Boot)
```bash
# From root directory or backend/
mvn spring-boot:run          # Start backend server on port 8081
mvn clean package           # Build JAR file
mvn test                    # Run unit tests
mvn clean compile           # Compile sources
```

### Frontend (React)
```bash
# From frontend/ directory
npm start                   # Start full-stack (backend + frontend)
npm run build-react         # Build React application
npm run start-react         # Serve frontend only (no backend API)
npm test                    # Run frontend tests
```

### Docker Development
```bash
# Full stack with Docker Compose
docker-compose up           # Start all services (frontend, backend, PostgreSQL)
docker-compose up --build   # Rebuild and start services
docker-compose down         # Stop all services

# Individual service builds
docker build -t monthly-expense-backend ./backend
docker build -t monthly-expense-frontend ./frontend
```

### Kubernetes/Helm Deployment
```bash
# From monthly-expense-app/ directory
helm repo add bitnami https://charts.bitnami.com/bitnami
helm dependency update .
helm install my-expense-app .
helm upgrade my-expense-app .
helm uninstall my-expense-app
```

## Project Structure

### Backend (`/backend`)
- `src/main/java/com/expense/monthly/`
  - `MonthlyExpenseApplication.java` - Main Spring Boot application
  - `controller/TransactionController.java` - REST API endpoints
  - `service/TransactionService.java` - Business logic layer
  - `repository/TransactionRepository.java` - Data access layer
  - `model/Transaction.java` - JPA entity for transactions
  - `dto/TransactionDTO.java` - Data transfer objects
  - `config/` - Configuration classes
- `src/main/resources/application.properties` - Spring configuration
- `src/test/java/` - Unit tests for controller, service, and repository layers

### Frontend (`/frontend`)
- `src/App.js` - Main React component with all UI logic
- `src/index.js` - React application entry point
- `src/styles.css` - Custom CSS styles
- `src/index.html` - HTML template
- `src/components/` - Reusable React components
- `src/utils/` - Utility functions

### Helm Charts (`/monthly-expense-app`)
- Main umbrella chart for Kubernetes deployment
- `charts/backend/` - Backend service subchart
- `charts/frontend/` - Frontend service subchart
- Uses PostgreSQL subchart from Bitnami

## API Endpoints

The backend provides these REST endpoints:
- `POST /api/transactions/upload` - Upload CSV file with transactions
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/month?month={month}&year={year}` - Get transactions by month/year
- `GET /api/transactions/category/{category}` - Get transactions by category
- `GET /api/transactions/total` - Get total amount of all transactions
- `GET /api/transactions/monthly-totals?year={year}` - Get monthly totals by year
- `GET /api/transactions/category-totals` - Get spending totals by category
- `DELETE /api/transactions/reset` - Reset all transaction data

## Database Configuration

### Local Development (H2)
- In-memory database for quick development
- Console available at: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:file:./data/expense_db`
- Username: `sa`, Password: `password`

### Production (PostgreSQL)
- Configured via environment variables in Docker/Kubernetes
- Database: `monthly_expense_db`
- User: `admin` (Docker Compose) / `postgres` (Helm)

## CSV File Format

Expected CSV format for transaction uploads:
```csv
Date,Description,Amount,Category
2023-01-15,Grocery shopping,125.50,Groceries
2023-01-20,Monthly rent,1200.00,Housing
```

## Testing

### Backend Tests
- Unit tests for controllers, services, and repositories
- Uses JUnit 5, Mockito, and Spring Boot Test
- Run with: `mvn test`

### Frontend Tests
- React component tests (if configured)
- Run with: `npm test`

## Common Development Patterns

### Adding New API Endpoints
1. Define endpoint in `TransactionController.java`
2. Implement business logic in `TransactionServiceImpl.java`
3. Add repository methods if needed in `TransactionRepository.java`
4. Update frontend API calls in `App.js`

### Database Schema Changes
1. Update `Transaction.java` entity
2. Add database migration scripts if needed
3. Update corresponding DTOs and service methods

### Frontend Component Development
- Main application logic is in `App.js` with functional components
- Uses React hooks for state management
- Bootstrap 5 for styling with custom CSS overrides
- Chart.js for data visualization components