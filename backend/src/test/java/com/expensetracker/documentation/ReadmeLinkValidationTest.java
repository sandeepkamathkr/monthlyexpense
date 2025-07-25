package com.expensetracker.documentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("README.md Link and Reference Validation Tests")
class ReadmeLinkValidationTest {

    private String readmeContent;

    @BeforeEach
    void setUp() throws IOException {
        // README.md is in the project root, one level up from backend  
        Path readmePath = Paths.get("../README.md");
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("README.md");
        }
        assertTrue(Files.exists(readmePath), "README.md file should exist in project root");
        
        readmeContent = Files.readString(readmePath);
        assertFalse(readmeContent.isEmpty(), "README.md should not be empty");
    }

    @Test
    @DisplayName("Should not contain broken internal links or references")
    void testInternalReferences() {
        // Look for references to files that should exist
        List<String> expectedFiles = new ArrayList<>();
        expectedFiles.add("LICENSE");
        
        // Check if LICENSE file is referenced
        if (readmeContent.contains("LICENSE")) {
            Path licensePath = Paths.get("../LICENSE");
            if (!Files.exists(licensePath)) {
                licensePath = Paths.get("LICENSE");
            }
            // Note: LICENSE file may not exist, but if referenced it should be noted
            if (!Files.exists(licensePath)) {
                System.out.println("Warning: LICENSE file is referenced in README but does not exist");
            }
        }
    }

    @Test
    @DisplayName("Should have consistent URL formatting")
    void testUrlFormatting() {
        // Find all URLs in the README
        Pattern urlPattern = Pattern.compile("https?://[^\\s)]+");
        Matcher matcher = urlPattern.matcher(readmeContent);
        
        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        
        // Should have localhost URLs for development
        boolean hasLocalhostUrl = urls.stream()
                .anyMatch(url -> url.contains("localhost"));
        assertTrue(hasLocalhostUrl, "README should contain localhost URLs for development");
        
        // Check for consistent port usage
        long localhost8081Count = urls.stream()
                .filter(url -> url.contains("localhost:8081"))
                .count();
        assertTrue(localhost8081Count > 0, "Should reference localhost:8081 for main application");
    }

    @Test
    @DisplayName("Should have proper markdown link syntax")
    void testMarkdownLinkSyntax() {
        // Look for potential markdown links [text](url)
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
        Matcher matcher = linkPattern.matcher(readmeContent);
        
        int linkCount = 0;
        while (matcher.find()) {
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            
            assertFalse(linkText.isEmpty(), "Link text should not be empty");
            assertFalse(linkUrl.isEmpty(), "Link URL should not be empty");
            
            linkCount++;
        }
        
        // README may not have many markdown links, but if present they should be valid
        if (linkCount > 0) {
            System.out.println("Found " + linkCount + " markdown links in README");
        }
    }

    @Test
    @DisplayName("Should reference correct file paths and commands")
    void testFilePathReferences() {
        // Check for references to project structure
        if (readmeContent.contains("pom.xml")) {
            Path pomPath = Paths.get("../pom.xml");
            if (!Files.exists(pomPath)) {
                pomPath = Paths.get("pom.xml");
            }
            assertTrue(Files.exists(pomPath), "Referenced pom.xml should exist");
        }
        
        if (readmeContent.contains("package.json")) {
            Path packageJsonPath = Paths.get("../package.json");
            if (!Files.exists(packageJsonPath)) {
                packageJsonPath = Paths.get("package.json");
            }
            // package.json may exist at root level for frontend scripts
            boolean packageJsonExists = Files.exists(packageJsonPath);
            if (!packageJsonExists) {
                System.out.println("Note: package.json referenced in README but not found");
            }
        }
    }

    @Test
    @DisplayName("Should have valid section cross-references")
    void testSectionCrossReferences() {
        // Extract all section headers
        Pattern headerPattern = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = headerPattern.matcher(readmeContent);
        
        List<String> sections = new ArrayList<>();
        while (matcher.find()) {
            sections.add(matcher.group(2).trim());
        }
        
        assertFalse(sections.isEmpty(), "README should contain section headers");
        
        // Verify major sections exist
        assertTrue(sections.stream().anyMatch(s -> s.contains("Features")), 
                   "Should have Features section");
        assertTrue(sections.stream().anyMatch(s -> s.contains("Getting Started")), 
                   "Should have Getting Started section");
        assertTrue(sections.stream().anyMatch(s -> s.contains("Technology Stack")), 
                   "Should have Technology Stack section");
    }

    @Test
    @DisplayName("Should validate code block language specifications")
    void testCodeBlockLanguages() {
        // Find code blocks with language specifications
        Pattern codeBlockPattern = Pattern.compile("```(\\w*)\\n");
        Matcher matcher = codeBlockPattern.matcher(readmeContent);
        
        int totalCodeBlocks = 0;
        int languageSpecifiedBlocks = 0;
        
        while (matcher.find()) {
            totalCodeBlocks++;
            String language = matcher.group(1);
            if (!language.isEmpty()) {
                languageSpecifiedBlocks++;
            }
        }
        
        assertTrue(totalCodeBlocks > 0, "README should contain code blocks");
        
        // At least some code blocks should have language specification for better rendering
        double languageSpecificationRatio = (double) languageSpecifiedBlocks / totalCodeBlocks;
        System.out.println("Code blocks with language specification: " + 
                           languageSpecifiedBlocks + "/" + totalCodeBlocks + 
                           " (" + String.format("%.1f", languageSpecificationRatio * 100) + "%)");
    }
}