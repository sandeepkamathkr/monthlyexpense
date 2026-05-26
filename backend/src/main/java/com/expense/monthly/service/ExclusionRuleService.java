package com.expense.monthly.service;

import com.expense.monthly.model.ExclusionRule;
import com.expense.monthly.repository.ExclusionRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExclusionRuleService {

    static final String DEFAULT_USER = "user@gmail.com";

    private final ExclusionRuleRepository exclusionRuleRepository;

    @Transactional
    public void saveExclusions(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) return;
        for (String desc : descriptions) {
            String key = desc.toLowerCase().trim();
            if (key.isBlank()) continue;
            exclusionRuleRepository
                    .findByUserIdAndNormalizedDescription(DEFAULT_USER, key)
                    .orElseGet(() -> {
                        ExclusionRule rule = new ExclusionRule();
                        rule.setUserId(DEFAULT_USER);
                        rule.setNormalizedDescription(key);
                        rule.setCreatedAt(LocalDateTime.now());
                        return exclusionRuleRepository.save(rule);
                    });
            log.info("Saved exclusion rule for: '{}'", key);
        }
    }

    public Set<String> getAllExclusionsAsSet() {
        return exclusionRuleRepository.findAllByUserId(DEFAULT_USER).stream()
                .map(ExclusionRule::getNormalizedDescription)
                .collect(Collectors.toSet());
    }
}
