package com.expense.monthly.dto;

import com.expense.monthly.model.Transaction;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Transaction.
 * Used for parsing CSV files and transferring data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    /**
     * DB id — not part of the CSV format; ignored by OpenCSV.
     * Exposed in REST responses so the frontend can call PATCH /{id}/category.
     */
    private Long id;

    /**
     * Date of the transaction.
     * Format: "dd/MM/yyyy"
     */
    @CsvBindByName(column = "Date", required = true)
    @CsvDate(value = "dd/MM/yyyy")
    private LocalDate date;

    /**
     * Description of the transaction.
     */
    @CsvBindByName(column = "Description", required = true)
    private String description;

    /**
     * Amount of the transaction.
     */
    @CsvBindByName(column = "Amount", required = true)
    private BigDecimal amount;

    /**
     * Currency code for the transaction (e.g., USD, EUR, GBP).
     * If not provided in CSV, defaults to USD.
     */
    @CsvBindByName(column = "Currency", required = false)
    private String currency;

    /**
     * Category of the transaction.
     */
    @CsvBindByName(column = "Category", required = true)
    private String category;

    /**
     * True if a user-saved category override was applied during import.
     * Not annotated with @CsvBind* — OpenCSV silently ignores unannotated fields,
     * so scheduler CSV parsing is unaffected.
     */
    private Boolean hasOverride;

    private boolean excluded;

    /**
     * Convert DTO to Entity.
     *
     * @return Transaction entity
     */
    public Transaction toEntity() {
        return Transaction.builder()
                .date(this.date)
                .description(this.description)
                .amount(this.amount)
                .currency(this.currency != null ? this.currency.toUpperCase() : "AUD")
                .category(this.category)
                .hasOverride(this.hasOverride)
                .build();
    }

    /**
     * Create DTO from Entity.
     *
     * @param transaction Transaction entity
     * @return TransactionDTO
     */
    public static TransactionDTO fromEntity(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getCategory(),
                transaction.getHasOverride(),
                transaction.isExcluded()
        );
    }
}