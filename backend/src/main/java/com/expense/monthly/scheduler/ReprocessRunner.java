package com.expense.monthly.scheduler;

import com.expense.monthly.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Runs once on startup when REPROCESS_ONLY=true.
 * Clears all transactions, resets .csv.done files back to .csv, then exits.
 * Intended to be used via a Kubernetes Job — not the regular Deployment.
 */
@Component
@ConditionalOnProperty(name = "reprocess.only", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ReprocessRunner implements ApplicationRunner {

    private final TransactionService transactionService;
    private final CsvFileManager csvFileManager;
    private final ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        log.info("REPROCESS_ONLY mode — clearing all transactions and resetting .done files");

        transactionService.deleteAllTransactions();

        File inputFolder = csvFileManager.getInputFolder();
        if (inputFolder.exists() && inputFolder.isDirectory()) {
            csvFileManager.resetDoneFiles(inputFolder);
        } else {
            log.warn("CSV input folder not found: {}", inputFolder.getAbsolutePath());
        }

        log.info("Reprocess complete — exiting");
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}