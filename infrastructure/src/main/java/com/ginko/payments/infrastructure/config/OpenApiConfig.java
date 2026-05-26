package com.ginko.payments.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ginko Payments API")
                        .description("Reactive API for managing payments to providers. " +
                                "Hexagonal architecture with ports and adapters, " +
                                "Reactive WebFlux, R2DBC, Resilience4j.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ginko Financial Solutions")
                                .email("contact@ginko.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
