package ru.berezhnoy.payment;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI paymentOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Online Shop — payment-service")
                .version("1.0")
                .description("""
                        In-memory payment provider used by shop-service.
                        PAYMENT_FAILURE_RATE (0..1) randomly returns HTTP 503.
                        Contract: api/payment-api.yaml.
                        """));
    }
}
