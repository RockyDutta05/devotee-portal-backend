package com.devoteeportal.backend;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy;

/**
 * JUnit 5 extension that configures Testcontainers globally before any tests run.
 * The static block sets the required system properties.
 */
public class TestcontainersConfig implements BeforeAllCallback {

    static {
        System.setProperty("docker.host", "tcp://localhost:2375");
        System.setProperty("DOCKER_HOST", "tcp://localhost:2375");
        System.setProperty("DOCKER_TLS_VERIFY", "0");
        System.setProperty("testcontainers.dockerclient.strategy",
                EnvironmentAndSystemPropertyClientProviderStrategy.class.getName());
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // No additional actions needed; static initializer already set properties.
    }
}
