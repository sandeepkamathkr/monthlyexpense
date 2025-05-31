package com.expense.monthly.controller;

import com.expense.monthly.dto.CategoryDTO;
import com.expense.monthly.model.Category;
import com.expense.monthly.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for category operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all categories.
     *
     * @return List of all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> dtos = categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a category by its ID.
     *
     * @param id Category ID
     * @return Category if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.info("Fetching category with ID: {}", id);
        return categoryService.getCategoryById(id)
                .map(CategoryDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new category.
     *
     * @param categoryDTO Category data
     * @return Created category
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO);
        Category saved = categoryService.saveCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryDTO.fromEntity(saved));
    }

    /**
     * Update an existing category.
     *
     * @param id Category ID
     * @param categoryDTO Category data
     * @return Updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);
        
        if (!categoryService.getCategoryById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        categoryDTO.setId(id);
        Category updated = categoryService.saveCategory(categoryDTO);
        return ResponseEntity.ok(CategoryDTO.fromEntity(updated));
    }

    /**
     * Delete a category.
     *
     * @param id Category ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        
        if (!categoryService.getCategoryById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}