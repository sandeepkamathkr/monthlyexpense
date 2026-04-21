package com.expense.monthly.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_overrides",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_category_overrides_user_desc",
               columnNames = {"user_id", "normalized_description"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "normalized_description", nullable = false, length = 500)
    private String normalizedDescription;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}