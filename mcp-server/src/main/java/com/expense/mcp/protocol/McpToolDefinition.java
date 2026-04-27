package com.expense.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents an MCP tool definition that describes a tool to the client (Claude).
 *
 * Example tool definition:
 * {
 *   "name": "query_expenses",
 *   "description": "Query expenses by date range, category, or merchant",
 *   "inputSchema": {
 *     "type": "object",
 *     "properties": {
 *       "startDate": {
 *         "type": "string",
 *         "description": "Start date in YYYY-MM-DD format",
 *         "format": "date"
 *       },
 *       "category": {
 *         "type": "string",
 *         "description": "Expense category (e.g., Groceries, Bills)"
 *       }
 *     }
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolDefinition {

    /**
     * Tool name (unique identifier)
     * Convention: snake_case (e.g., "query_expenses")
     */
    @JsonProperty("name")
    private String name;

    /**
     * Human-readable description of what the tool does
     * Helps Claude understand when to use this tool
     */
    @JsonProperty("description")
    private String description;

    /**
     * JSON Schema describing the tool's input parameters
     * Follows JSON Schema Draft 7 specification
     */
    @JsonProperty("inputSchema")
    private InputSchema inputSchema;

    /**
     * Represents the JSON Schema for tool input parameters
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputSchema {

        /**
         * Schema type (usually "object")
         */
        @JsonProperty("type")
        private String type;

        /**
         * Map of property name to property schema
         */
        @JsonProperty("properties")
        private Map<String, PropertySchema> properties;

        /**
         * List of required property names
         */
        @JsonProperty("required")
        private List<String> required;

        /**
         * Additional properties allowed (default: false)
         */
        @JsonProperty("additionalProperties")
        private Boolean additionalProperties;
    }

    /**
     * Represents a property schema within the input schema
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PropertySchema {

        /**
         * Property type (string, number, integer, boolean, array, object)
         */
        @JsonProperty("type")
        private String type;

        /**
         * Property description
         */
        @JsonProperty("description")
        private String description;

        /**
         * Format hint (e.g., "date", "date-time", "email")
         */
        @JsonProperty("format")
        private String format;

        /**
         * Enum values (for string types)
         */
        @JsonProperty("enum")
        private List<String> enumValues;

        /**
         * Minimum value (for numeric types)
         */
        @JsonProperty("minimum")
        private Number minimum;

        /**
         * Maximum value (for numeric types)
         */
        @JsonProperty("maximum")
        private Number maximum;

        /**
         * Pattern (regex) for string validation
         */
        @JsonProperty("pattern")
        private String pattern;

        /**
         * Items schema (for array types)
         */
        @JsonProperty("items")
        private PropertySchema items;

        /**
         * Default value
         */
        @JsonProperty("default")
        private Object defaultValue;
    }
}
