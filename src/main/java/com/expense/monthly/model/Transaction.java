package com.expense.monthly.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class representing a financial transaction.
 * This class maps to the 'transactions' table in the database.
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Unique identifier for the transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Date when the transaction occurred.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Description of the transaction.
     */
    @Column(nullable = false)
    private String description;

    /**
     * Amount of the transaction.
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * Category of the transaction.
     */
    @Column(nullable = false)
    private String category;

    /**
     * Month of the transaction (1-12).
     * This is derived from the date field but stored for easier querying.
     */
    @Column(nullable = false)
    private int month;

    /**
     * Year of the transaction.
     * This is derived from the date field but stored for easier querying.
     */
    @Column(nullable = false)
    private int year;

    /**
     * Pre-persist hook to set month and year fields based on the date.
     */
    @PrePersist
    @PreUpdate
    public void setMonthAndYear() {
        if (date != null) {
            this.month = date.getMonthValue();
            this.year = date.getYear();
        }
    }
}