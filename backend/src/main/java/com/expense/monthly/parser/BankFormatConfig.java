package com.expense.monthly.parser;

import lombok.Data;

/**
 * Configuration POJO for a single bank CSV format.
 * Loaded from application.yml under bank.formats[].
 * Add a new entry there to support a new bank — no Java changes needed.
 */
@Data
public class BankFormatConfig {

    /** Human-readable name, e.g. "commbank", "hsbc" */
    private String name;

    /** Java date pattern used in this bank's date column, e.g. "dd/MM/yyyy" or "d MMM yyyy" */
    private String datePattern;

    /** Whether the CSV has a header row that should be skipped */
    private boolean hasHeader;

    /**
     * Whether amounts are signed (negative = expense, positive = income).
     * true  → CommBank style: debit rows have negative amounts; skip positive rows
     * false → HSBC style: all amounts are positive debits
     */
    private boolean amountSigned;

    /** Zero-based column indices for each field */
    private ColumnConfig columns;

    @Data
    public static class ColumnConfig {
        private int date;
        private int amount;
        private int description;
    }
}
