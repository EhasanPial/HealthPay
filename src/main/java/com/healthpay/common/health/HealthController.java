package com.healthpay.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "System", description = "Service status")
public class HealthController {

    /**
     * Liveness only: answers "is the application process running and serving HTTP?".
     * It deliberately does not touch PostgreSQL, Redis or Kafka, so it stays fast and
     * never fails because of a dependency. Dependency checks live at /actuator/health.
     */
    @GetMapping
    @Operation(
            summary = "Check that the application is running",
            description = "Lightweight liveness check. Use /actuator/health to check PostgreSQL and Redis.")
    public HealthResponse health() {
        return HealthResponse.up();
    }
}
