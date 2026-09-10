package com.devoteeportal.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Smoke test – verifies the Spring application context loads cleanly.
 * Uses the "test" profile (H2 in-memory DB) so no live DB connection is required.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",              () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.jpa.database-platform",       () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.datasource.driver-class-name",() -> "org.h2.Driver");
        registry.add("spring.datasource.username",         () -> "sa");
        registry.add("spring.datasource.password",         () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto",      () -> "create-drop");
        registry.add("spring.mail.host",                   () -> "localhost");
        registry.add("spring.mail.port",                   () -> "2525");
        registry.add("jwt.secret",                         () -> "supersecretkeythatisatleast32characterslongforjwttoacceptit");
        registry.add("jwt.expiration.ms",                  () -> "3600000");
        // Stub R2/S3 with an empty endpoint to avoid AWS SDK bean failures
        registry.add("app.r2.account-id",                  () -> "test");
        registry.add("app.r2.access-key",                  () -> "test");
        registry.add("app.r2.secret-key",                  () -> "test");
        registry.add("app.r2.bucket-name",                 () -> "test-bucket");
        registry.add("app.r2.endpoint-url",                () -> "http://localhost:4566");
    }

    @Test
    void contextLoads() {
        // If the application context starts without errors, this test passes.
    }
}
