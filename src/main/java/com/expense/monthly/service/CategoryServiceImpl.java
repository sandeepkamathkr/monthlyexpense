package com.expense.monthly.service;

import com.expense.monthly.dto.CategoryDTO;
import com.expense.monthly.model.Category;
import com.expense.monthly.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the CategoryService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> getAllCategories() {
        log.info("Retrieving all categories");
        return categoryRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> getCategoryById(Long id) {
        log.info("Retrieving category with ID: {}", id);
        return categoryRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> getCategoryByName(String name) {
        log.info("Retrieving category with name: {}", name);
        return categoryRepository.findByNameIgnoreCase(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Category saveCategory(CategoryDTO categoryDTO) {
        log.info("Saving category: {}", categoryDTO);
        return categoryRepository.save(categoryDTO.toEntity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryRepository.deleteById(id);
    }
}