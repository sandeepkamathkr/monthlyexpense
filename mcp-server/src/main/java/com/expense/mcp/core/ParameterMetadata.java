package com.expense.mcp.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata about a tool parameter extracted from @McpParam annotation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterMetadata {

    /**
     * Parameter name
     */
    private String name;

    /**
     * Parameter description
     */
    private String description;

    /**
     * Parameter type (Java class)
     */
    private Class<?> type;

    /**
     * Whether the parameter is required
     */
    private boolean required;

    /**
     * Default value (as string)
     */
    private String defaultValue;

    /**
     * Validation pattern (regex)
     */
    private String pattern;

    /**
     * Minimum value (for numbers)
     */
    private String min;

    /**
     * Maximum value (for numbers)
     */
    private String max;

    /**
     * Allowed enum values
     */
    private String[] allowedValues;

    /**
     * Format hint (e.g., "date", "date-time")
     */
    private String format;

    /**
     * Parameter index in method signature
     */
    private int index;

    /**
     * Check if this parameter has a default value
     */
    public boolean hasDefault() {
        return defaultValue != null && !defaultValue.isEmpty();
    }

    /**
     * Check if this parameter has validation rules
     */
    public boolean hasValidation() {
        return (pattern != null && !pattern.isEmpty()) ||
                (min != null && !min.isEmpty()) ||
                (max != null && !max.isEmpty()) ||
                (allowedValues != null && allowedValues.length > 0);
    }
}
