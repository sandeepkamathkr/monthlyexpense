package com.expense.monthly.service;

import com.expense.monthly.model.CategoryOverride;
import com.expense.monthly.repository.CategoryOverrideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryOverrideService {

    // Placeholder until authentication lands. Replace with principal from SecurityContext.
    static final String DEFAULT_USER = "user@gmail.com";

    private final CategoryOverrideRepository categoryOverrideRepository;

    @Transactional
    public void saveOverride(String description, String category) {
        saveOverride(DEFAULT_USER, description, category);
    }

    @Transactional
    public void saveOverride(String userId, String description, String category) {
        String key = description.toLowerCase().trim();
        CategoryOverride override = categoryOverrideRepository
                .findByUserIdAndNormalizedDescription(userId, key)
                .orElseGet(CategoryOverride::new);
        override.setUserId(userId);
        override.setNormalizedDescription(key);
        override.setCategory(category);
        override.setUpdatedAt(LocalDateTime.now());
        categoryOverrideRepository.save(override);
        log.info("Saved category override: '{}' -> '{}'", key, category);
    }

    public Map<String, String> getAllOverridesAsMap() {
        return getAllOverridesAsMap(DEFAULT_USER);
    }

    public Map<String, String> getAllOverridesAsMap(String userId) {
        return categoryOverrideRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(
                        CategoryOverride::getNormalizedDescription,
                        CategoryOverride::getCategory));
    }

    @Transactional
    public void saveOverrides(List<com.expense.monthly.dto.CategoryOverrideDTO> overrides) {
        if (overrides == null || overrides.isEmpty()) return;
        for (com.expense.monthly.dto.CategoryOverrideDTO req : overrides) {
            if (req.getDescription() == null || req.getDescription().isBlank()) continue;
            if (req.getCategory() == null || req.getCategory().isBlank()) continue;
            saveOverride(req.getDescription(), req.getCategory());
        }
    }
}