package com.expense.monthly.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * Entity class representing a transaction category.
 * This class maps to the 'categories' table in the database.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * Unique identifier for the category.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the category.
     */
    @Column(nullable = false, unique = true)
    private String name;
    
    /**
     * Optional description of the category.
     */
    @Column
    private String description;
    
    /**
     * Transactions associated with this category.
     */
    @OneToMany(mappedBy = "category")
    private List<Transaction> transactions;
}