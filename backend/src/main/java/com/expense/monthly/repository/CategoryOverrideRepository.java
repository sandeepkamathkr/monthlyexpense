package com.expense.monthly.repository;

import com.expense.monthly.model.CategoryOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryOverrideRepository extends JpaRepository<CategoryOverride, Long> {

    Optional<CategoryOverride> findByUserIdAndNormalizedDescription(String userId, String normalizedDescription);

    List<CategoryOverride> findAllByUserId(String userId);
}