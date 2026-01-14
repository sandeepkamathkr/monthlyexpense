package com.expense.mcp.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Converts JSON parameters to Java types for tool method invocation.
 * Handles type conversion, validation, and default values.
 */
@Component
@Slf4j
public class McpParameterConverter {

    private final ObjectMapper objectMapper;

    public McpParameterConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert a parameter value to the target type
     *
     * @param value Parameter value from JSON (can be null)
     * @param metadata Parameter metadata
     * @return Converted value
     * @throws IllegalArgumentException if conversion fails or validation fails
     */
    public Object convert(Object value, ParameterMetadata metadata) {
        // Handle null/missing values
        if (value == null) {
            if (metadata.isRequired() && !metadata.hasDefault()) {
                throw new IllegalArgumentException(
                        "Required parameter '" + metadata.getName() + "' is missing"
                );
            }
            if (metadata.hasDefault()) {
                value = metadata.getDefaultValue();
            } else {
                return null; // Optional parameter not provided
            }
        }

        // Convert to target type
        Object converted = convertToType(value, metadata.getType());

        // Validate
        validate(converted, metadata);

        return converted;
    }

    /**
     * Convert value to the specified type
     */
    private Object convertToType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // Already correct type
        if (targetType.isInstance(value)) {
            return value;
        }

        // String conversion
        String stringValue = value.toString();

        try {
            // String
            if (targetType == String.class) {
                return stringValue;
            }

            // Integer
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(stringValue);
            }

            // Long
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(stringValue);
            }

            // Double
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(stringValue);
            }

            // Float
            if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(stringValue);
            }

            // Boolean
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(stringValue);
            }

            // BigDecimal
            if (targetType == BigDecimal.class) {
                return new BigDecimal(stringValue);
            }

            // LocalDate
            if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE);
            }

            // LocalDateTime
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            // List (use Jackson for complex types)
            if (List.class.isAssignableFrom(targetType)) {
                return objectMapper.convertValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
            }

            // Map (use Jackson for complex types)
            if (Map.class.isAssignableFrom(targetType)) {
                return objectMapper.convertValue(value, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            }

            // Try Jackson as fallback
            return objectMapper.convertValue(value, targetType);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot convert parameter to " + targetType.getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    /**
     * Validate a converted value against parameter metadata
     */
    private void validate(Object value, ParameterMetadata metadata) {
        if (value == null) {
            return; // Null already handled in convert()
        }

        // Pattern validation (for strings)
        if (metadata.getPattern() != null && !metadata.getPattern().isEmpty() && value instanceof String) {
            String stringValue = (String) value;
            if (!stringValue.matches(metadata.getPattern())) {
                throw new IllegalArgumentException(
                        "Parameter '" + metadata.getName() + "' does not match pattern: " + metadata.getPattern()
                );
            }
        }

        // Enum validation (allowed values)
        if (metadata.getAllowedValues() != null && metadata.getAllowedValues().length > 0 && value instanceof String) {
            String stringValue = (String) value;
            boolean found = false;
            for (String allowed : metadata.getAllowedValues()) {
                if (allowed.equals(stringValue)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(
                        "Parameter '" + metadata.getName() + "' must be one of: " +
                                String.join(", ", metadata.getAllowedValues())
                );
            }
        }

        // Numeric range validation
        if (value instanceof Number) {
            Number numValue = (Number) value;
            double doubleValue = numValue.doubleValue();

            if (metadata.getMin() != null && !metadata.getMin().isEmpty()) {
                double min = Double.parseDouble(metadata.getMin());
                if (doubleValue < min) {
                    throw new IllegalArgumentException(
                            "Parameter '" + metadata.getName() + "' must be >= " + min
                    );
                }
            }

            if (metadata.getMax() != null && !metadata.getMax().isEmpty()) {
                double max = Double.parseDouble(metadata.getMax());
                if (doubleValue > max) {
                    throw new IllegalArgumentException(
                            "Parameter '" + metadata.getName() + "' must be <= " + max
                    );
                }
            }
        }
    }
}
