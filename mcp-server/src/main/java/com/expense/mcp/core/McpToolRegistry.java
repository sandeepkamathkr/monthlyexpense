package com.expense.mcp.core;

import com.expense.mcp.annotations.McpParam;
import com.expense.mcp.annotations.McpResource;
import com.expense.mcp.annotations.McpTool;
import com.expense.mcp.protocol.McpToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for MCP tools. Scans for @McpResource classes and @McpTool methods
 * at startup and maintains a registry of available tools.
 *
 * This class is a Spring component and will be automatically instantiated.
 */
@Component
@Slf4j
public class McpToolRegistry {

    private final ApplicationContext applicationContext;
    private final Map<String, RegisteredTool> tools = new ConcurrentHashMap<>();

    public McpToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Scan for @McpResource beans and register their @McpTool methods
     * This runs automatically after Spring initialization
     */
    @PostConstruct
    public void scanAndRegisterTools() {
        log.info("Scanning for MCP tools...");

        // Find all beans annotated with @McpResource
        Map<String, Object> resourceBeans = applicationContext.getBeansWithAnnotation(McpResource.class);

        for (Map.Entry<String, Object> entry : resourceBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            registerResourceBean(beanName, bean);
        }

        log.info("Registered {} MCP tools", tools.size());
    }

    /**
     * Register all @McpTool methods from a resource bean
     */
    private void registerResourceBean(String beanName, Object bean) {
        Class<?> clazz = bean.getClass();
        McpResource resourceAnnotation = clazz.getAnnotation(McpResource.class);

        log.debug("Scanning resource bean: {} ({})", beanName, clazz.getName());

        // Find all methods annotated with @McpTool
        for (Method method : clazz.getDeclaredMethods()) {
            McpTool toolAnnotation = method.getAnnotation(McpTool.class);
            if (toolAnnotation != null) {
                registerTool(bean, method, toolAnnotation);
            }
        }
    }

    /**
     * Register a single tool method
     */
    private void registerTool(Object instance, Method method, McpTool annotation) {
        String toolName = annotation.name();

        if (tools.containsKey(toolName)) {
            log.warn("Tool '{}' is already registered, skipping duplicate", toolName);
            return;
        }

        try {
            // Build parameter metadata
            ParameterMetadata[] paramMetadata = extractParameterMetadata(method);

            // Build tool definition
            McpToolDefinition definition = buildToolDefinition(annotation, paramMetadata);

            // Create registered tool
            RegisteredTool registeredTool = RegisteredTool.builder()
                    .definition(definition)
                    .instance(instance)
                    .method(method)
                    .parameters(paramMetadata)
                    .category(annotation.category())
                    .requiresAuth(annotation.requiresAuth())
                    .build();

            tools.put(toolName, registeredTool);
            log.info("Registered tool: {} - {}", toolName, annotation.description());

        } catch (Exception e) {
            log.error("Failed to register tool: " + toolName, e);
        }
    }

    /**
     * Extract parameter metadata from method parameters
     */
    private ParameterMetadata[] extractParameterMetadata(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterMetadata[] metadata = new ParameterMetadata[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            McpParam paramAnnotation = param.getAnnotation(McpParam.class);

            String paramName = paramAnnotation != null && !paramAnnotation.name().isEmpty()
                    ? paramAnnotation.name()
                    : param.getName();

            metadata[i] = ParameterMetadata.builder()
                    .name(paramName)
                    .description(paramAnnotation != null ? paramAnnotation.description() : "")
                    .type(param.getType())
                    .required(paramAnnotation != null && paramAnnotation.required())
                    .defaultValue(paramAnnotation != null ? paramAnnotation.defaultValue() : "")
                    .pattern(paramAnnotation != null ? paramAnnotation.pattern() : "")
                    .min(paramAnnotation != null ? paramAnnotation.min() : "")
                    .max(paramAnnotation != null ? paramAnnotation.max() : "")
                    .allowedValues(paramAnnotation != null ? paramAnnotation.allowedValues() : new String[0])
                    .format(paramAnnotation != null ? paramAnnotation.format() : "")
                    .index(i)
                    .build();
        }

        return metadata;
    }

    /**
     * Build MCP tool definition from annotation and parameters
     */
    private McpToolDefinition buildToolDefinition(McpTool annotation, ParameterMetadata[] parameters) {
        // Build input schema properties
        Map<String, McpToolDefinition.PropertySchema> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ParameterMetadata param : parameters) {
            McpToolDefinition.PropertySchema propertySchema = buildPropertySchema(param);
            properties.put(param.getName(), propertySchema);

            if (param.isRequired()) {
                required.add(param.getName());
            }
        }

        // Build input schema
        McpToolDefinition.InputSchema inputSchema = McpToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties)
                .required(required.isEmpty() ? null : required)
                .additionalProperties(false)
                .build();

        // Build tool definition
        return McpToolDefinition.builder()
                .name(annotation.name())
                .description(annotation.description())
                .inputSchema(inputSchema)
                .build();
    }

    /**
     * Build property schema for a parameter
     */
    private McpToolDefinition.PropertySchema buildPropertySchema(ParameterMetadata param) {
        String jsonType = mapJavaTypeToJsonType(param.getType());

        McpToolDefinition.PropertySchema.PropertySchemaBuilder builder = McpToolDefinition.PropertySchema.builder()
                .type(jsonType)
                .description(param.getDescription());

        // Add format hint
        if (param.getFormat() != null && !param.getFormat().isEmpty()) {
            builder.format(param.getFormat());
        }

        // Add enum values
        if (param.getAllowedValues() != null && param.getAllowedValues().length > 0) {
            builder.enumValues(Arrays.asList(param.getAllowedValues()));
        }

        // Add numeric constraints
        if (param.getMin() != null && !param.getMin().isEmpty()) {
            builder.minimum(parseNumber(param.getMin()));
        }
        if (param.getMax() != null && !param.getMax().isEmpty()) {
            builder.maximum(parseNumber(param.getMax()));
        }

        // Add pattern
        if (param.getPattern() != null && !param.getPattern().isEmpty()) {
            builder.pattern(param.getPattern());
        }

        // Add default value
        if (param.hasDefault()) {
            builder.defaultValue(param.getDefaultValue());
        }

        return builder.build();
    }

    /**
     * Map Java type to JSON Schema type
     */
    private String mapJavaTypeToJsonType(Class<?> javaType) {
        if (javaType == String.class) return "string";
        if (javaType == Integer.class || javaType == int.class) return "integer";
        if (javaType == Long.class || javaType == long.class) return "integer";
        if (javaType == Double.class || javaType == double.class) return "number";
        if (javaType == Float.class || javaType == float.class) return "number";
        if (javaType == Boolean.class || javaType == boolean.class) return "boolean";
        if (javaType == java.time.LocalDate.class) return "string"; // format: date
        if (javaType == java.time.LocalDateTime.class) return "string"; // format: date-time
        if (javaType == java.math.BigDecimal.class) return "number";
        if (javaType.isArray() || List.class.isAssignableFrom(javaType)) return "array";
        if (Map.class.isAssignableFrom(javaType)) return "object";
        return "string"; // default
    }

    /**
     * Parse a number from string
     */
    private Number parseNumber(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a registered tool by name
     */
    public Optional<RegisteredTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Get all registered tools
     */
    public List<RegisteredTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    /**
     * Get all tool definitions (for listing to clients)
     */
    public List<McpToolDefinition> getAllToolDefinitions() {
        return tools.values().stream()
                .map(RegisteredTool::getDefinition)
                .toList();
    }

    /**
     * Check if a tool exists
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * Get tool count
     */
    public int getToolCount() {
        return tools.size();
    }
}
