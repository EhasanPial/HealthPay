package com.healthpay.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Base class for integration tests: the full application on a random port,
 * backed by real containers.
 * <p>
 * Every subclass uses the same configuration, so Spring caches one application
 * context and the containers start only once per test run.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {
}
