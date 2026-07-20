package com.example.ats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application Context Integration Test.
 *
 * <p>{@code @SpringBootTest} loads the full Spring application context.
 * This test verifies that all Spring beans are configured correctly,
 * all required properties are present, and no circular dependencies exist.
 *
 * <p>{@code @ActiveProfiles("test")} activates the test profile so that
 * test-specific overrides (e.g., H2 in-memory database) are used instead
 * of the PostgreSQL connection.
 *
 * <p>This test MUST pass before committing any code. A failing context
 * load indicates a misconfigured bean or missing property.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Loads Successfully")
class AtsApplicationTests {

    /**
     * Verifies the entire Spring application context loads without errors.
     *
     * <p>If this test passes, it confirms:
     * <ul>
     *   <li>All {@code @Configuration} beans are valid</li>
     *   <li>All required properties are available</li>
     *   <li>All {@code @Component}, {@code @Service}, {@code @Repository} beans are discoverable</li>
     *   <li>No circular dependency issues exist</li>
     * </ul>
     */
    @Test
    @DisplayName("Spring context loads without errors")
    void contextLoads() {
        // If the context loads, this test passes implicitly.
        // No assertions needed — @SpringBootTest handles the verification.
    }
}
