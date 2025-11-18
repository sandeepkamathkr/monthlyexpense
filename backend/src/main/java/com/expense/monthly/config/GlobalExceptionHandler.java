package com.expense.monthly.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Handles validation errors and other exceptions centrally.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @RequestParam validation.
     *
     * @param ex ConstraintViolationException containing validation errors
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("message", "Invalid request parameters provided");
        
        // Extract specific validation errors
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> extractFieldName(violation),
                    ConstraintViolation::getMessage
                ));
        
        response.put("fieldErrors", fieldErrors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle illegal argument exceptions from manual validation.
     *
     * @param ex IllegalArgumentException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Invalid Parameter");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle general runtime exceptions.
     *
     * @param ex RuntimeException
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // Don't handle IllegalArgumentException here as it's handled above
        if (ex instanceof IllegalArgumentException) {
            throw ex;
        }
        
        log.error("Runtime error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Extract field name from constraint violation.
     *
     * @param violation ConstraintViolation
     * @return Field name
     */
    private String extractFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        // Extract parameter name from path like "getCategoryTotals.month"
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
    }
}