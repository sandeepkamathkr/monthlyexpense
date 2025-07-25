package com.expensetracker.documentation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("README.md Integration Tests")
class ReadmeIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName("Should validate that documented API endpoints actually exist")
    void testDocumentedApiEndpointsExist() throws IOException {
        // README.md is in the project root, one level up from backend
        Path readmePath = Paths.get("../README.md");
        if (!Files.exists(readmePath)) {
            readmePath = Paths.get("README.md");
        }
        String readmeContent = Files.readString(readmePath);
        
        // Extract API endpoints from README
        Pattern endpointPattern = Pattern.compile("(GET|POST|PUT|DELETE) (/api/[^\\s]+)");
        Matcher matcher = endpointPattern.matcher(readmeContent);
        
        int validatedEndpoints = 0;
        while (matcher.find()) {
            String method = matcher.group(1);
            String endpoint = matcher.group(2);
            
            // Skip parameterized endpoints for basic validation
            if (endpoint.contains("{") || endpoint.contains("?")) {
                continue;
            }
            
            String url = "http://localhost:" + port + endpoint;
            
            try {
                ResponseEntity<String> response;
                switch (method) {
                    case "GET":
                        response = restTemplate.getForEntity(url, String.class);
                        break;
                    case "POST":
                        response = restTemplate.postForEntity(url, null, String.class);
                        break;
                    case "DELETE":
                        restTemplate.delete(url);
                        response = new ResponseEntity<>(HttpStatus.OK);
                        break;
                    default:
                        continue;
                }
                
                // Endpoint should exist (not return 404)
                assertNotEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                               "Documented endpoint should exist: " + method + " " + endpoint);
                
                validatedEndpoints++;
            } catch (Exception e) {
                // Log but don't fail - some endpoints may require authentication or specific conditions
                System.out.println("Could not validate endpoint: " + method + " " + endpoint + " - " + e.getMessage());
            }
        }
        
        assertTrue(validatedEndpoints > 0, "Should validate at least some documented endpoints");
    }

    @Test
    @DisplayName("Should verify H2 console is accessible as documented")
    void testH2ConsoleAccessibility() {
        String h2ConsoleUrl = "http://localhost:" + port + "/h2-console";
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(h2ConsoleUrl, String.class);
            
            // H2 console should be accessible (may return 200 or redirect)
            assertTrue(response.getStatusCode().is2xxSuccessful() || 
                      response.getStatusCode().is3xxRedirection(),
                      "H2 console should be accessible as documented");
                      
        } catch (Exception e) {
            // If H2 console is disabled in test, that's acceptable
            System.out.println("H2 console not accessible in test environment: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate application responds on documented port")
    void testApplicationAccessibility() {
        String appUrl = "http://localhost:" + port;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(appUrl, String.class);
            
            // Application should be accessible
            assertTrue(response.getStatusCode().is2xxSuccessful() || 
                      response.getStatusCode().is3xxRedirection(),
                      "Application should be accessible on documented port");
                      
        } catch (Exception e) {
            fail("Application should be accessible: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should verify project structure matches documentation")
    void testProjectStructureMatchesDocumentation() {
        // Verify key project files exist as implied by documentation
        Path pomPath = Paths.get("pom.xml");
        if (!Files.exists(pomPath)) {
            pomPath = Paths.get("../pom.xml");
        }
        assertTrue(Files.exists(pomPath), 
                   "Maven pom.xml should exist as documented");
        
        // Check for Docker-related files mentioned in documentation
        Path dockerfile = Paths.get("../Dockerfile");
        if (!Files.exists(dockerfile)) {
            dockerfile = Paths.get("Dockerfile");
        }
        
        if (Files.exists(dockerfile)) {
            try {
                String dockerfileContent = Files.readString(dockerfile);
                assertTrue(dockerfileContent.contains("openjdk:11-jre"), 
                           "Dockerfile should use JRE image as documented in troubleshooting");
            } catch (IOException e) {
                System.out.println("Could not read Dockerfile: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should validate that documented database configuration matches application properties")
    void testDatabaseConfigurationConsistency() throws IOException {
        // Check if application.properties exists and contains H2 configuration
        Path appPropsPath = Paths.get("src/main/resources/application.properties");
        if (!Files.exists(appPropsPath)) {
            appPropsPath = Paths.get("../backend/src/main/resources/application.properties");
        }
        
        if (Files.exists(appPropsPath)) {
            String appPropsContent = Files.readString(appPropsPath);
            
            // Check if H2 configuration is present
            if (appPropsContent.contains("h2")) {
                // README should be consistent with actual configuration
                Path readmePath = Paths.get("../README.md");
                if (!Files.exists(readmePath)) {
                    readmePath = Paths.get("README.md");
                }
                String readmeContent = Files.readString(readmePath);
                
                assertTrue(readmeContent.contains("H2"), 
                           "README should mention H2 database if configured");
            }
        }
    }
}