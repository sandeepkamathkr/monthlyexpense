package com.expensetracker.documentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("README.md Documentation Validation Tests")
class ReadmeValidationTest {

    private String readmeContent;
    private List<String> readmeLines;
    
    @BeforeEach
    void setUp() throws IOException {
        // README.md is in the project root, one level up from backend
        Path readmePath = Paths.get("../README.md");
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("README.md");
        }
        assertTrue(Files.exists(readmePath), "README.md file should exist in project root");
        
        readmeContent = Files.readString(readmePath);
        readmeLines = Files.readAllLines(readmePath);
        
        assertFalse(readmeContent.isEmpty(), "README.md should not be empty");
    }

    @Test
    @DisplayName("Should have proper project title and structure")
    void testProjectTitleAndStructure() {
        // Verify title exists and is properly formatted
        assertTrue(readmeContent.contains("# Monthly Expense Tracker"), 
                   "README should contain main project title");
        
        // Verify essential sections exist
        String[] requiredSections = {
            "## Features",
            "## Technology Stack", 
            "## Getting Started",
            "## API Endpoints",
            "## Database",
            "## Troubleshooting"
        };
        
        for (String section : requiredSections) {
            assertTrue(readmeContent.contains(section), 
                       "README should contain section: " + section);
        }
    }

    @Test
    @DisplayName("Should contain all documented features")
    void testFeatureDocumentation() {
        String[] expectedFeatures = {
            "CSV Upload",
            "Transaction Management", 
            "Expense Analysis",
            "Monthly Summaries",
            "Category Analysis",
            "Data Persistence",
            "Reset Functionality"
        };
        
        for (String feature : expectedFeatures) {
            assertTrue(readmeContent.contains(feature), 
                       "README should document feature: " + feature);
        }
    }

    @Test
    @DisplayName("Should document correct technology stack")
    void testTechnologyStackDocumentation() {
        // Backend technologies
        String[] backendTech = {
            "Java 11",
            "Spring Boot 2.7.0",
            "Spring Data JPA",
            "H2 Database",
            "OpenCSV",
            "Lombok"
        };
        
        // Frontend technologies  
        String[] frontendTech = {
            "React",
            "Bootstrap 5",
            "Chart.js",
            "Axios"
        };
        
        for (String tech : backendTech) {
            assertTrue(readmeContent.contains(tech), 
                       "README should document backend technology: " + tech);
        }
        
        for (String tech : frontendTech) {
            assertTrue(readmeContent.contains(tech), 
                       "README should document frontend technology: " + tech);
        }
    }

    @Test
    @DisplayName("Should have valid code blocks with proper syntax")
    void testCodeBlockSyntax() {
        // Find all code blocks
        Pattern codeBlockPattern = Pattern.compile("```[\\s\\S]*?```");
        long codeBlockCount = codeBlockPattern.matcher(readmeContent).results().count();
        
        assertTrue(codeBlockCount >= 5, "README should contain multiple code examples");
        
        // Verify Maven command is documented
        assertTrue(readmeContent.contains("mvn spring-boot:run"), 
                   "README should document Maven startup command");
        
        // Verify Docker commands are documented
        assertTrue(readmeContent.contains("docker build"), 
                   "README should document Docker build command");
        assertTrue(readmeContent.contains("docker run"), 
                   "README should document Docker run command");
    }

    @Test
    @DisplayName("Should document all API endpoints with correct HTTP methods")
    void testApiEndpointDocumentation() {
        String[] expectedEndpoints = {
            "POST /api/transactions/upload",
            "GET /api/transactions",
            "GET /api/transactions/month",
            "GET /api/transactions/category",
            "GET /api/transactions/total",
            "GET /api/transactions/monthly-totals",
            "GET /api/transactions/category-totals",
            "DELETE /api/transactions/reset"
        };
        
        for (String endpoint : expectedEndpoints) {
            assertTrue(readmeContent.contains(endpoint), 
                       "README should document API endpoint: " + endpoint);
        }
    }

    @Test
    @DisplayName("Should document CSV file format requirements")
    void testCsvFormatDocumentation() {
        // Verify CSV format section exists
        assertTrue(readmeContent.contains("## CSV File Format"), 
                   "README should contain CSV format section");
        
        // Verify required columns are documented
        String[] requiredColumns = {"Date", "Description", "Amount", "Category"};
        
        for (String column : requiredColumns) {
            assertTrue(readmeContent.contains("**" + column + "**"), 
                       "README should document CSV column: " + column);
        }
        
        // Verify date format is specified
        assertTrue(readmeContent.contains("yyyy-MM-dd"), 
                   "README should specify date format");
        
        // Verify example CSV is provided
        assertTrue(readmeContent.contains("2023-01-15,Grocery shopping"), 
                   "README should contain CSV example");
    }

    @Test
    @DisplayName("Should document database configuration")
    void testDatabaseDocumentation() {
        // Verify database section exists
        assertTrue(readmeContent.contains("## Database"), 
                   "README should contain database section");
        
        // Verify H2 configuration details
        assertTrue(readmeContent.contains("jdbc:h2:file:./data/expense_db"), 
                   "README should document H2 JDBC URL");
        assertTrue(readmeContent.contains("Username: `sa`"), 
                   "README should document database username");
        assertTrue(readmeContent.contains("Password: `password`"), 
                   "README should document database password");
        assertTrue(readmeContent.contains("http://localhost:8081/h2-console"), 
                   "README should document H2 console URL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:8081", "http://localhost:5000"})
    @DisplayName("Should document correct application URLs")
    void testApplicationUrls(String url) {
        assertTrue(readmeContent.contains(url), 
                   "README should document application URL: " + url);
    }

    @Test
    @DisplayName("Should have troubleshooting section with DLL error solutions")
    void testTroubleshootingSection() {
        assertTrue(readmeContent.contains("## Troubleshooting"), 
                   "README should contain troubleshooting section");
        
        // Verify DLL error troubleshooting
        assertTrue(readmeContent.contains("DLL Errors"), 
                   "README should address DLL errors");
        assertTrue(readmeContent.contains("openjdk:11-jre"), 
                   "README should mention JRE image solution");
        assertTrue(readmeContent.contains("native libraries"), 
                   "README should explain native library requirements");
    }

    @Test
    @DisplayName("Should have consistent markdown formatting")
    void testMarkdownFormatting() {
        // Check for consistent header formatting
        List<String> headers = readmeLines.stream()
                .filter(line -> line.startsWith("#"))
                .collect(Collectors.toList());
        
        assertFalse(headers.isEmpty(), "README should contain headers");
        
        // Verify main title uses single #
        assertTrue(headers.get(0).startsWith("# "), 
                   "Main title should use single # with space");
        
        // Check for proper list formatting
        long bulletListCount = readmeLines.stream()
                .filter(line -> line.trim().startsWith("- "))
                .count();
        
        assertTrue(bulletListCount > 10, 
                   "README should contain multiple bullet points");
    }

    @Test
    @DisplayName("Should document multiple startup methods")
    void testStartupMethodDocumentation() {
        // Verify Maven startup method
        assertTrue(readmeContent.contains("#### Using Maven"), 
                   "README should document Maven startup method");
        
        // Verify Docker startup method
        assertTrue(readmeContent.contains("#### Using Docker"), 
                   "README should document Docker startup method");
        
        // Verify React-only startup method
        assertTrue(readmeContent.contains("#### Using Serve (React Only)"), 
                   "README should document React-only startup method");
        
        // Verify npm commands are documented
        assertTrue(readmeContent.contains("npm start"), 
                   "README should document npm start command");
        assertTrue(readmeContent.contains("npm run start-react"), 
                   "README should document npm run start-react command");
    }

    @Test
    @DisplayName("Should contain license information")
    void testLicenseDocumentation() {
        assertTrue(readmeContent.contains("## License"), 
                   "README should contain license section");
        assertTrue(readmeContent.contains("MIT License"), 
                   "README should specify MIT license");
    }

    @Test
    @DisplayName("Should have proper prerequisites documentation")  
    void testPrerequisitesDocumentation() {
        assertTrue(readmeContent.contains("### Prerequisites"), 
                   "README should document prerequisites");
        
        String[] prerequisites = {"Java 11", "Maven", "Docker"};
        
        for (String prereq : prerequisites) {
            assertTrue(readmeContent.contains(prereq), 
                       "README should list prerequisite: " + prereq);
        }
    }

    @Test
    @DisplayName("Should validate README content completeness")
    void testContentCompleteness() {
        // Minimum content length check
        assertTrue(readmeContent.length() > 3000, 
                   "README should contain substantial documentation");
        
        // Minimum line count check
        assertTrue(readmeLines.size() > 100, 
                   "README should contain comprehensive information");
        
        // Check for balanced content sections
        int featureLines = countLinesInSection("## Features");
        int techStackLines = countLinesInSection("## Technology Stack");
        int gettingStartedLines = countLinesInSection("## Getting Started");
        
        assertTrue(featureLines > 5, "Features section should be detailed");
        assertTrue(techStackLines > 10, "Technology stack section should be comprehensive");
        assertTrue(gettingStartedLines > 15, "Getting started section should be thorough");
    }

    @Test
    @DisplayName("Should validate command examples are properly formatted")
    void testCommandExamples() {
        // Check that command examples are in proper code blocks
        assertTrue(readmeContent.contains("```\n   mvn spring-boot:run") || 
                   readmeContent.contains("mvn spring-boot:run\n   ```"), 
                   "Maven command should be in code block");
        assertTrue(readmeContent.contains("npm start"), 
                   "npm start command should be documented");
        assertTrue(readmeContent.contains("docker build"), 
                   "Docker build command should be documented");
        assertTrue(readmeContent.contains("docker run"), 
                   "Docker run command should be documented");
    }

    @Test
    @DisplayName("Should document CSV example with proper formatting")
    void testCsvExampleFormatting() {
        // Verify CSV example is properly formatted
        assertTrue(readmeContent.contains("Date,Description,Amount,Category"), 
                   "CSV header should be documented");
        assertTrue(readmeContent.contains("2023-01-15,Grocery shopping,125.50,Groceries"), 
                   "CSV example should contain grocery entry");
        assertTrue(readmeContent.contains("2023-01-20,Monthly rent,1200.00,Housing"), 
                   "CSV example should contain rent entry");
        assertTrue(readmeContent.contains("2023-01-25,Internet bill,65.00,Utilities"), 
                   "CSV example should contain utilities entry");
    }

    @Test
    @DisplayName("Should validate port consistency throughout documentation")
    void testPortConsistency() {
        // Main application port should be 8081
        long port8081Count = Pattern.compile("8081").matcher(readmeContent).results().count();
        assertTrue(port8081Count >= 3, "Port 8081 should be mentioned multiple times consistently");
        
        // React serve port should be 5000  
        assertTrue(readmeContent.contains("5000"), 
                   "React serve port 5000 should be documented");
    }

    private int countLinesInSection(String sectionHeader) {
        boolean inSection = false;
        int lineCount = 0;
        
        for (String line : readmeLines) {
            if (line.equals(sectionHeader)) {
                inSection = true;
                continue;
            }
            if (inSection && line.startsWith("## ")) {
                break;
            }
            if (inSection && !line.trim().isEmpty()) {
                lineCount++;
            }
        }
        
        return lineCount;
    }
}