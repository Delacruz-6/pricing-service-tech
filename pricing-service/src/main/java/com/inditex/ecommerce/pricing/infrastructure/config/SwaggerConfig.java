package com.inditex.ecommerce.pricing.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.name:pricing-service}")
    private String name;

    @Value("${app.description:API de servicio de precios de Inditex}")
    private String description;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${app.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("pricing-api")
                .packagesToScan("com.inditex.ecommerce.pricing.infrastructure.in.rest.controller")
                .pathsToExclude("/actuator/**")
                .build();
    }

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .addServersItem(new Server().url(serverUrl))
            .info(new Info()
                .title(name.toUpperCase())
                .version(version)
                .description(description));
    }
}