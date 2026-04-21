package com.expense.monthly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Monthly Expense Tracker.
 * This Spring Boot application provides REST APIs for managing expense transactions.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
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