package com.expense.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a tool execution.
 * MCP tools return content blocks (text, images, resources, etc.)
 *
 * Example tool result:
 * {
 *   "content": [
 *     {
 *       "type": "text",
 *       "text": "Found 15 transactions totaling $487.32"
 *     }
 *   ],
 *   "isError": false
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolResult {

    /**
     * List of content blocks returned by the tool
     */
    @JsonProperty("content")
    @Builder.Default
    private List<ContentBlock> content = new ArrayList<>();

    /**
     * Whether this result represents an error
     */
    @JsonProperty("isError")
    @Builder.Default
    private boolean isError = false;

    /**
     * Represents a content block (text, image, resource reference, etc.)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentBlock {

        /**
         * Content type (text, image, resource)
         */
        @JsonProperty("type")
        private String type;

        /**
         * Text content (for type="text")
         */
        @JsonProperty("text")
        private String text;

        /**
         * Image data (for type="image")
         */
        @JsonProperty("data")
        private String data;

        /**
         * MIME type (for images)
         */
        @JsonProperty("mimeType")
        private String mimeType;

        /**
         * Resource URI (for type="resource")
         */
        @JsonProperty("resource")
        private String resource;
    }

    /**
     * Create a text result
     *
     * @param text Text content
     * @return McpToolResult with text content
     */
    public static McpToolResult text(String text) {
        ContentBlock block = ContentBlock.builder()
                .type("text")
                .text(text)
                .build();

        return McpToolResult.builder()
                .content(List.of(block))
                .isError(false)
                .build();
    }

    /**
     * Create an error result
     *
     * @param errorMessage Error message
     * @return McpToolResult with error flag
     */
    public static McpToolResult error(String errorMessage) {
        ContentBlock block = ContentBlock.builder()
                .type("text")
                .text("Error: " + errorMessage)
                .build();

        return McpToolResult.builder()
                .content(List.of(block))
                .isError(true)
                .build();
    }

    /**
     * Create a structured JSON result as text
     *
     * @param json JSON string
     * @return McpToolResult with JSON text
     */
    public static McpToolResult json(String json) {
        return text(json);
    }
}
