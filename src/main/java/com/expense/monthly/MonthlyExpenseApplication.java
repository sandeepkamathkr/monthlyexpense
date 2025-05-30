package com.expense.monthly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Monthly Expense Tracker.
 * This Spring Boot application provides REST APIs for managing expense transactions.
 */
@SpringBootApplication
public class MonthlyExpenseApplication {

    /**
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MonthlyExpenseApplication.class, args);
    }
}