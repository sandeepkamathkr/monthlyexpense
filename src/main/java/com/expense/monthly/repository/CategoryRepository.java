package com.expense.monthly.repository;

import com.expense.monthly.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Category entity.
 * Provides methods for CRUD operations and custom queries.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find a category by its name (case insensitive).
     *
     * @param name Category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameIgnoreCase(String name);
}