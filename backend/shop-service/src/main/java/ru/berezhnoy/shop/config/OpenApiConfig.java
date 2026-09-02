package ru.berezhnoy.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI shopOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Online Shop — shop-service")
                .version("1.0")
                .description("""
                        Catalog, cart, orders, payment orchestration and sales analytics.
                        No authentication: the app always runs as demo user id=1
                        (ivan.petrov@example.com).
                        """));
    }
}
