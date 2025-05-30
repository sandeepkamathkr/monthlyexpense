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
     * Date of the transaction.
     * Format: yyyy-MM-dd
     */
    @CsvBindByName(column = "Date", required = true)
    @CsvDate(value = "yyyy-MM-dd")
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
     * Category of the transaction.
     */
    @CsvBindByName(column = "Category", required = true)
    private String category;

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
                .category(this.category)
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
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory()
        );
    }
}