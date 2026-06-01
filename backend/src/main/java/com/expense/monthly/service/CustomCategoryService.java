package com.expense.monthly.service;

import com.expense.monthly.model.Category;
import com.expense.monthly.model.CustomCategory;
import com.expense.monthly.repository.CustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomCategoryService {

    private final CustomCategoryRepository customCategoryRepository;

    /**
     * Returns the canonical category list merged with any user-added custom categories.
     * Order: canonical list first, then custom categories sorted alphabetically.
     */
    public List<String> getAllCategories() {
        Set<String> merged = new LinkedHashSet<>(Category.ALL);
        customCategoryRepository.findAll().stream()
                .map(CustomCategory::getName)
                .filter(name -> !merged.contains(name))
                .sorted()
                .forEach(merged::add);
        return new ArrayList<>(merged);
    }

    /**
     * Persists new category names. Ignores names that already exist (canonical or custom).
     */
    @Transactional
    public void saveCategories(List<String> names) {
        if (names == null || names.isEmpty()) return;
        Set<String> canonical = new LinkedHashSet<>(Category.ALL);
        for (String name : names) {
            if (name == null || name.isBlank()) continue;
            String trimmed = name.trim();
            if (canonical.contains(trimmed)) continue;
            customCategoryRepository.findByNameIgnoreCase(trimmed).orElseGet(() -> {
                CustomCategory cc = new CustomCategory();
                cc.setName(trimmed);
                cc.setCreatedAt(LocalDateTime.now());
                log.info("Saving new custom category: '{}'", trimmed);
                return customCategoryRepository.save(cc);
            });
        }
    }
}
