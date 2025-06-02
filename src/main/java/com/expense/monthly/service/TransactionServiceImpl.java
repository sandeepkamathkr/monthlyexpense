package com.expense.monthly.service;

import com.expense.monthly.dto.TransactionDTO;
import com.expense.monthly.model.Transaction;
import com.expense.monthly.repository.TransactionRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the TransactionService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Transaction saveTransaction(TransactionDTO transactionDTO) {
        log.info("Saving transaction: {}", transactionDTO);
        return transactionRepository.save(transactionDTO.toEntity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<Transaction> saveTransactions(List<TransactionDTO> transactions) {
        log.info("Saving {} transactions", transactions.size());
        List<Transaction> entities = transactions.stream()
                .map(TransactionDTO::toEntity)
                .collect(Collectors.toList());
        return transactionRepository.saveAll(entities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<Transaction> processCSVFile(MultipartFile file) throws IOException {
        log.info("Processing CSV file: {}", file.getOriginalFilename());
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<TransactionDTO> csvToBean = new CsvToBeanBuilder<TransactionDTO>(reader)
                    .withType(TransactionDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<TransactionDTO> transactions = csvToBean.parse();
            log.info("Parsed {} transactions from CSV", transactions.size());
            return saveTransactions(transactions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getAllTransactions() {
        log.info("Retrieving all transactions");
        return transactionRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getTransactionsByMonth(int month, int year) {
        log.info("Retrieving transactions for month: {}, year: {}", month, year);
        return transactionRepository.findByMonthAndYear(month, year);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getTransactionsByCategory(String category) {
        log.info("Retrieving transactions for category: {}", category);
        return transactionRepository.findByCategoryIgnoreCase(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getTransactionsByDescription(String description) {
        log.info("Retrieving transactions containing description: {}", description);
        return transactionRepository.findByDescriptionContainingIgnoreCase(description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateTotalAmount() {
        log.info("Calculating total amount");
        BigDecimal total = transactionRepository.calculateTotalAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, BigDecimal> calculateMonthlyTotals(int year) {
        log.info("Calculating monthly totals for year: {}", year);
        Map<Integer, BigDecimal> monthlyTotals = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            BigDecimal total = transactionRepository.calculateMonthlyTotal(month, year);
            monthlyTotals.put(month, total != null ? total : BigDecimal.ZERO);
        }

        return monthlyTotals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, BigDecimal> calculateTotalsByCategory() {
        log.info("Calculating totals by category");
        Map<String, BigDecimal> categoryTotals = new HashMap<>();

        // Get all transactions and group by category
        List<Transaction> transactions = getAllTransactions();
        Map<String, List<Transaction>> transactionsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getCategory().toLowerCase()));

        // Calculate total for each category
        transactionsByCategory.forEach((category, categoryTransactions) -> {
            BigDecimal total = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            categoryTotals.put(category, total);
        });

        return categoryTotals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAllTransactions() {
        log.info("Deleting all transactions");
        transactionRepository.deleteAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void executeH2ResetScript() {
        log.info("Executing H2 specific reset script: DROP ALL OBJECTS");
        jdbcTemplate.execute("DROP ALL OBJECTS");
        log.info("H2 database has been cleared. Schema recreation depends on JPA/Hibernate configuration (e.g., ddl-auto) and Spring Boot's datasource initialization.");
    }

}