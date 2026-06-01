package com.expense.monthly.repository;

import com.expense.monthly.model.CustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {

    Optional<CustomCategory> findByNameIgnoreCase(String name);
}
