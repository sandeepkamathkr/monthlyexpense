package com.expense.monthly.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "custom_categories",
       uniqueConstraints = @UniqueConstraint(name = "uq_custom_categories_name", columnNames = {"name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
