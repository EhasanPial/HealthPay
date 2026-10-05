package com.healthpay.common.health;

import com.healthpay.support.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the running application over real HTTP, exactly like curl would.
 */
class HealthApiIT extends IntegrationTestBase {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        HttpResponse<String> response = get("/api/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(response.body(), "$.status")).isEqualTo("UP");
    }

    @Test
    void actuatorHealthReportsDatabaseAndRedisUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health");
        String body = response.body();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("UP");
        assertThat(JsonPath.<String>read(body, "$.components.db.status")).isEqualTo("UP");
        assertThat(JsonPath.<String>read(body, "$.components.redis.status")).isEqualTo("UP");
    }

    @Test
    void openApiDocumentIsPublic() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(response.body(), "$.info.title")).isEqualTo("HealthPay API");
    }

    @Test
    void protectedEndpointsReturn401WithoutAuthentication() throws Exception {
        assertThat(get("/api/claims").statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
