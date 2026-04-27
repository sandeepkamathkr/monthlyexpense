package com.expense.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Configures proper handling of Java 8 date/time types and BigDecimal.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Don't write dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // CRITICAL: Disable pretty-printing for stdio transport
        // Claude Desktop expects single-line JSON (no newlines)
        // Pretty-printed JSON causes "Unexpected token" errors when read line-by-line
        mapper.disable(SerializationFeature.INDENT_OUTPUT);

        // Don't fail on unknown properties
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Write BigDecimal as plain strings (not scientific notation)
        mapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        return mapper;
    }
}
