package com.expense.monthly.repository;

import com.expense.monthly.model.ImportFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for ImportFailure records.
 * Spring Data JPA auto-implements all methods from their names.
 */
@Repository
public interface ImportFailureRepository extends JpaRepository<ImportFailure, Long> {

    /**
     * Returns all failures with the given status (e.g. "FAILED").
     */
    List<ImportFailure> findByStatus(String status);

    /**
     * Deletes all ImportFailure records whose filePath starts with the given prefix.
     * Used during re-import cleanup to remove stale failure records for a month:
     *   prefix = "2026/Jan/"
     */
    @Modifying
    @Transactional
    void deleteByFilePathStartingWith(String prefix);
}
