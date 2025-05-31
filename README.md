# Monthly Expense Tracker

A web application for tracking and analyzing monthly expenses. This application allows users to upload CSV files containing transaction data, view transaction details, and analyze spending patterns.

## Features

- **CSV Upload**: Upload transaction data from CSV files
- **Transaction Management**: View and manage transaction records
- **Expense Analysis**: Visualize spending patterns with charts and summaries
- **Monthly Summaries**: View spending totals by month
- **Category Analysis**: See spending breakdown by category
- **Data Persistence**: All data is stored in an H2 database
- **Reset Functionality**: Option to reset all data

## Technology Stack

### Backend
- Java 11
- Spring Boot 2.7.0
- Spring Data JPA
- H2 Database
- OpenCSV for CSV processing
- Lombok

### Frontend
- React (using CDN, no build required)
- Bootstrap 5
- Chart.js for data visualization
- Axios for API calls

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven
- Docker (optional)

### Running the Application

#### Using Maven
1. Clone the repository
2. Navigate to the project directory
3. Run the application using Maven:
   ```
   mvn spring-boot:run
   ```
   or
   ```
   npm start
   ```
4. Access the application at http://localhost:8081

#### Using Docker
1. Clone the repository
2. Navigate to the project directory
3. Build the Docker image:
   ```
   docker build -t monthly-expense-tracker .
   ```
4. Run the Docker container:
   ```
   docker run -p 8081:8081 monthly-expense-tracker
   ```
5. Access the application at http://localhost:8081

#### Using Serve (React Only)
If you're having trouble starting the Spring Boot application, you can run just the React frontend:

1. Install dependencies:
   ```
   npm install
   ```
2. Start the React app:
   ```
   npm run start-react
   ```
3. Access the application at the URL shown in the terminal (typically http://localhost:5000)

Note: When using this method, the backend API will not be available, so data loading and saving features won't work.

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
- `GET /api/transactions/category-totals`: Get totals by category
- `DELETE /api/transactions/reset`: Reset all data

## Database

The application uses an H2 in-memory database by default. The database console is available at http://localhost:8081/h2-console with the following default credentials:
- JDBC URL: `jdbc:h2:file:./data/expense_db`
- Username: `sa`
- Password: `password`

## Troubleshooting

### DLL Errors
If you encounter DLL errors when running the application in Docker, it's likely due to missing native libraries in the slim JRE image. The application uses H2 database which requires certain native libraries to function properly.

Solution:
- The Dockerfile has been updated to use the full JRE image (`openjdk:11-jre`) instead of the slim version (`openjdk:11-jre-slim`).
- The full JRE image includes all necessary native libraries that H2 database needs.
- If you still encounter issues, try running the application outside of Docker using Maven.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
