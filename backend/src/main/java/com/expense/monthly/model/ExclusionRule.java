package com.expense.monthly.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exclusion_rules",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_exclusion_rules_user_desc",
               columnNames = {"user_id", "normalized_description"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExclusionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "normalized_description", nullable = false, length = 500)
    private String normalizedDescription;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
