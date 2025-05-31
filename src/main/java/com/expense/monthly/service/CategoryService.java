package com.expense.monthly.service;

import com.expense.monthly.dto.CategoryDTO;
import com.expense.monthly.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    /**
     * Get all categories.
     *
     * @return List of all categories
     */
    List<Category> getAllCategories();

    /**
     * Get a category by its ID.
     *
     * @param id Category ID
     * @return Optional containing the category if found
     */
    Optional<Category> getCategoryById(Long id);

    /**
     * Get a category by its name (case insensitive).
     *
     * @param name Category name
     * @return Optional containing the category if found
     */
    Optional<Category> getCategoryByName(String name);

    /**
     * Save a category.
     *
     * @param categoryDTO Category data
     * @return Saved category
     */
    Category saveCategory(CategoryDTO categoryDTO);

    /**
     * Delete a category by its ID.
     *
     * @param id Category ID
     */
    void deleteCategory(Long id);
}