---
globs: "**/src/**/*.java"
---

## Java / Spring Boot Conventions

- Use BigDecimal for all monetary amounts — never float or double
- Transaction.month and Transaction.year are derived fields; always set via @PrePersist/@PreUpdate, never manually
- Controller parameters for month (1–12) and year (1900–2100) must use @Min/@Max validation
- Service interface is TransactionService; implementation is TransactionServiceImpl — follow this pattern for new services
- CSV date parsing expects strict dd/MM/yyyy format via OpenCSV
- CORS origins are configured in CorsConfig.java — do not hardcode origins elsewhere
- ShedLock lock names must be unique across all schedulers
- Unit tests live in src/test/java2 (not standard src/test/java) — configured via Maven build-helper-maven-plugin