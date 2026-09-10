package com.devoteeportal.backend;


import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Use H2 in-memory database for tests
        registry.add("spring.datasource.url",              () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.jpa.database-platform",       () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.datasource.driver-class-name",() -> "org.h2.Driver");
        registry.add("spring.datasource.username",         () -> "sa");
        registry.add("spring.datasource.password",         () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto",      () -> "update");
        registry.add("spring.mail.host",                   () -> "localhost");
        registry.add("spring.mail.port",                   () -> "2525");
        registry.add("jwt.secret",                         () -> "supersecretkeythatisatleast32characterslongforjwttoacceptit");
        registry.add("jwt.expiration.ms",                  () -> "3600000");
        // Stub Cloudflare R2 so S3Presigner bean is constructed without real credentials
        registry.add("app.r2.account-id",                  () -> "test");
        registry.add("app.r2.access-key",                  () -> "test");
        registry.add("app.r2.secret-key",                  () -> "test");
        registry.add("app.r2.bucket-name",                 () -> "test-bucket");
        registry.add("app.r2.endpoint-url",                () -> "http://localhost:4566");
    }
}
