package com.expense.monthly.repository;

import com.expense.monthly.model.ExclusionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExclusionRuleRepository extends JpaRepository<ExclusionRule, Long> {

    Optional<ExclusionRule> findByUserIdAndNormalizedDescription(String userId, String normalizedDescription);

    List<ExclusionRule> findAllByUserId(String userId);
}
