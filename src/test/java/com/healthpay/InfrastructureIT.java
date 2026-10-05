package com.healthpay;

import com.healthpay.support.IntegrationTestBase;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the application is really connected to PostgreSQL (with Flyway applied), Redis and Kafka.
 * The beans below are created by Spring Boot auto-configuration and injected by Spring.
 */
class InfrastructureIT extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Test
    void flywayAppliedTheBaselineMigration() {
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success",
                Integer.class);

        assertThat(applied).isEqualTo(1);
    }

    @Test
    void redisCanStoreAndReadAValue() {
        String key = "healthpay:test:" + UUID.randomUUID();
        try {
            redisTemplate.opsForValue().set(key, "ok", Duration.ofMinutes(1));

            assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("ok");
        } finally {
            redisTemplate.delete(key);
        }
    }

    @Test
    void kafkaBrokerIsReachable() throws Exception {
        try (Admin admin = Admin.create(kafkaAdmin.getConfigurationProperties())) {
            Collection<Node> nodes = admin.describeCluster().nodes().get(30, TimeUnit.SECONDS);

            assertThat(nodes).isNotEmpty();
        }
    }
}
