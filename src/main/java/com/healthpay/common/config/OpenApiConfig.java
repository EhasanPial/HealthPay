package com.healthpay.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI healthPayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("HealthPay API")
                .version("0.1.0")
                .description("Healthcare claims, billing and payment platform. "
                        + "Educational portfolio project that uses synthetic data only."));
    }
}
