# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚨 GLOBAL SESSION INSTRUCTIONS - MANDATORY FOR ALL SESSIONS 🚨

**CRITICAL NOTICE**: The instructions in this file are PERSISTENT and GLOBAL. They apply to:
- ✅ ALL current Claude Code sessions
- ✅ ALL future Claude Code sessions  
- ✅ ALL new conversations and session restarts
- ✅ EVERY interaction in this repository
- ✅ Both LOCAL development and PRODUCTION deployments

**ENFORCEMENT**: These instructions OVERRIDE all default Claude behavior and MUST be followed WITHOUT EXCEPTION in every session. There are NO scenarios where these rules can be bypassed or ignored.

**SESSION PERSISTENCE**: Even if a session is restarted, a new conversation begins, or Claude Code is reopened after days/weeks, these instructions remain in full effect and must be followed exactly as written.

## Claude Response Requirements

When providing suggestions or recommendations, Claude must address these three questions:
1. **Why** - Explain the reasoning behind the suggestion
2. **How** - Describe how to implement or apply the suggestion
3. **Where** - Provide documentation references or sources to verify the information

## MANDATORY: Step-by-Step Learning Mode

**CRITICAL**: Claude must ALWAYS use a step-by-step educational approach for all technical changes. This overrides all default behavior.

### Required Learning Format
Before ANY technical implementation, Claude MUST:

1. **📚 CONCEPT EXPLANATION**: Break down the technical concept being implemented
   - **What** it is in simple terms
   - **Why** it's needed for this specific project
   - **How** it fits into the overall architecture
   - **When** to use this approach vs alternatives

2. **🔍 CURRENT STATE ANALYSIS**: Examine and explain the existing code/configuration
   - What the current implementation does
   - What problems or limitations exist
   - What will change and why

3. **📋 STEP-BY-STEP BREAKDOWN**: Detail each implementation step
   - Number each step clearly (Step 1, Step 2, etc.)
   - Explain what each step accomplishes
   - Show before/after code comparisons
   - Explain any new concepts introduced in each step

4. **✅ VERIFICATION & TESTING**: Explain how to validate each step
   - How to test that each step worked correctly
   - What output/behavior to expect
   - How to troubleshoot common issues

5. **📖 LEARNING REFERENCES**: Provide learning resources
   - Official documentation links
   - Best practices explanations
   - Common patterns and why they're used

### Examples of Required Learning Format:

**Example 1: Docker Multi-Architecture Changes**
```
📚 CONCEPT: Multi-architecture Docker builds
- What: Building Docker images that run on different CPU architectures (Intel x64, ARM64)
- Why: Ensures your app runs on Apple Silicon Macs, Intel machines, and cloud servers
- How: Uses Docker buildx with platform specifications
- When: Required when deploying to diverse hardware environments

🔍 CURRENT STATE: 
- Current Dockerfile uses "FROM amazoncorretto:17-alpine" 
- This pulls the default architecture for your machine
- Problem: May not work on different architectures

📋 STEP-BY-STEP:
Step 1: Add platform specification to Dockerfile
- Before: FROM amazoncorretto:17-alpine
- After: FROM --platform=${BUILDPLATFORM:-linux/amd64} docker.io/library/amazoncorretto:17-alpine
- Why: Explicitly controls which architecture to build for
...
```

### Enforcement Rules:
- NO technical changes without educational explanation
- NO "quick implementations" without learning context
- ALWAYS explain Docker/Kubernetes concepts as they're used
- MUST teach the user about tools, patterns, and best practices
- Each step must be understandable to someone learning these technologies

This ensures continuous learning and prevents "black box" development.

## MANDATORY: Explain-First Code Changes

**CRITICAL**: Claude must ALWAYS explain code changes BEFORE implementing them. This overrides all default behavior.

### Required Explanation Format
Before ANY code modification, file creation, or technical change, Claude MUST:

1. **🔍 EXPLANATION SECTION**: Provide detailed explanation including:
   - **What** is being added/modified/deleted
   - **Why** this change is necessary 
   - **How** it integrates with existing architecture
   - **Where** it connects to other components
   - **Impact** on frontend, backend, database, or deployment

2. **📋 TECHNICAL DETAILS**: Explain:
   - File locations and their purposes
   - Code patterns being used and why
   - Dependencies or libraries being added
   - Configuration changes required
   - Testing implications

3. **✅ UNDERSTANDING CHECK**: Ask "Does this make sense? Any questions before I implement?" and wait for confirmation.

### Examples of Required Explanations:
- Adding new API endpoint: Explain controller → service → repository flow
- Frontend changes: Explain component hierarchy and state management 
- Database changes: Explain entity relationships and migration needs
- Docker/Helm changes: Explain containerization and deployment impact

### Enforcement Rules:
- NO code changes without prior explanation
- NO file creation without architectural justification  
- NO "quick fixes" without learning context
- ALWAYS explain existing code when modifying it
- MUST teach the user about patterns and conventions being used

This ensures continuous learning and prevents "black box" development.

## Git Commit Guidelines

When creating git commits, do not add any reference to Claude in commit messages or code comments. Keep all commit messages professional and focused on the actual changes made.

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

### Docker Development Workflow (REQUIRED)

**IMPORTANT**: Whenever you make changes to frontend or backend code, you MUST use the Docker workflow to test your changes. Local development commands are provided for reference only.

```bash
# From frontend/ directory - ALWAYS use this workflow after code changes
./docker-build.sh           # Rebuild both frontend and backend Docker images
docker-compose up -d         # Start all services (frontend, backend, PostgreSQL)

# Verify deployment
docker-compose ps            # Check all containers are running
curl http://localhost/       # Test frontend (port 80)
curl http://localhost:8081/api/transactions  # Test backend API (port 8081)

# Stop services when done
docker-compose down          # Stop all containers
```

### Frontend Build Requirements

Before running `docker-build.sh`, ensure the React app is properly built:

```bash
# From frontend/ directory
# 1. Clean and rebuild React application
rm -rf build && npx react-scripts build

# 2. Copy build files to public directory (required by Dockerfile)
rm -rf public/static && cp -r build/* public/

# 3. Verify only one main JS file exists (prevents Chrome crashes)
find public/static/js -name "main.*.js" | wc -l  # Should return 1

# 4. Now run Docker build
./docker-build.sh && docker-compose up -d
```

### Backend (Java Spring Boot) - Local Development Only
```bash
# From root directory or backend/
mvn spring-boot:run          # Start backend server on port 8081
mvn clean package           # Build JAR file
mvn test                    # Run unit tests
mvn clean compile           # Compile sources
```

### Frontend (React) - Local Development Only
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

### Mandatory Testing After Code Changes

**IMPORTANT**: After making any frontend or backend changes, you MUST follow this testing protocol:

#### 1. Backend API Testing
```bash
# Test basic endpoints after Docker deployment
curl -s "http://localhost:8081/api/transactions" | jq '. | length'
curl -s "http://localhost:8081/api/transactions/category-totals" | jq 'keys | length'

# Test any new endpoints with parameters
curl -s "http://localhost:8081/api/transactions/category-totals?month=11&year=2025" | jq

# Verify error handling
curl -s "http://localhost:8081/api/transactions/category-totals?month=invalid" 
```

#### 2. Frontend UI Testing
```bash
# 1. Access application
open http://localhost/  # or curl -s -o /dev/null -w "%{http_code}" http://localhost/

# 2. Manual testing checklist:
# - Upload a CSV file successfully
# - Navigate through different sections
# - Test new UI components (e.g., MonthYearSelector)
# - Verify data displays correctly
# - Check browser console for errors (F12 → Console)
# - Test responsive design on different screen sizes
```

#### 3. Integration Testing
```bash
# Test full workflow after changes
# 1. Upload sample data via frontend
# 2. Verify data appears in all relevant sections
# 3. Test filtering/sorting functionality
# 4. Verify API responses match UI display
```

#### 4. Container Health Testing
```bash
# Verify all containers are healthy
docker-compose ps
docker-compose logs backend-service | tail -20
docker-compose logs frontend | tail -20

# Test container restart resilience
docker-compose restart backend-service
curl -s "http://localhost:8081/api/transactions/total"
```

### Unit Tests

#### Backend Tests
- Unit tests for controllers, services, and repositories
- Uses JUnit 5, Mockito, and Spring Boot Test
- Run with: `mvn test`

#### Frontend Tests
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