package com.expense.monthly.dto;

import com.expense.monthly.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Category.
 * Used for transferring category data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    /**
     * Unique identifier for the category.
     */
    private Long id;
    
    /**
     * Name of the category.
     */
    private String name;
    
    /**
     * Optional description of the category.
     */
    private String description;
    
    /**
     * Convert DTO to Entity.
     *
     * @return Category entity
     */
    public Category toEntity() {
        return Category.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .build();
    }
    
    /**
     * Create DTO from Entity.
     *
     * @param category Category entity
     * @return CategoryDTO
     */
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}