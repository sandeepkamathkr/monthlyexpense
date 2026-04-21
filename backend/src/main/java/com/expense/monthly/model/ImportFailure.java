package com.expense.monthly.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity that records a CSV file which the scheduler failed to process.
 *
 * When the scheduler hits an exception processing a CSV file it:
 *   1. Renames the file to <name>.csv.failed  (stops auto-retry)
 *   2. Persists an ImportFailure row here so the Import page can surface it
 *
 * The user can then:
 *   - Download the .csv.failed file, fix it on their computer, and re-upload
 *   - Dismiss the record once it is resolved
 */
@Entity
@Table(name = "import_failures")
@Data
@NoArgsConstructor
public class ImportFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relative path of the failed file inside the scheduler input folder.
     * Example: "2026/May/1745123456-commbank.csv.failed"
     */
    @Column(nullable = false)
    private String filePath;

    /**
     * Human-readable, actionable error message.
     * Examples:
     *   "Row 5: Date \"2026-01-15\" is not in expected format. Expected DD/MM/YYYY (e.g. 15/01/2026)"
     *   "Row 3: Amount \"$45.50\" could not be parsed. Remove currency symbols — expected a plain number (e.g. 45.50)"
     */
    @Column(nullable = false, length = 2000)
    private String errorMessage;

    /** Timestamp when the failure was recorded. */
    @Column(nullable = false)
    private LocalDateTime failedAt;

    /**
     * Lifecycle status.
     * "FAILED"    — visible in the Import page failures panel
     * "DISMISSED" — user has acknowledged and hidden from panel
     */
    @Column(nullable = false, length = 20)
    private String status;
}
