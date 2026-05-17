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
     * Currency code for the transaction (e.g., USD, EUR, GBP).
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Category of the transaction.
     */
    @Column(nullable = false)
    private String category;

    /**
     * Month of the transaction (1-12).
     * This is derived from the date field but stored for easier querying.
     */
    @Column(name = "transaction_month", nullable = false)
    private int month;

    /**
     * Year of the transaction.
     * This is derived from the date field but stored for easier querying.
     */
    @Column(name = "transaction_year", nullable = false)
    private int year;

    /**
     * True if the category was overridden by a user-saved rule during import.
     * Null means no override was applied.
     */
    @Column(name = "has_override")
    private Boolean hasOverride;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean excluded;

    @Column(name = "source_file")
    private String sourceFile;

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
