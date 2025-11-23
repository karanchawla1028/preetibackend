// src/main/java/com/preetinest/config/OpenAPIConfig.java
package com.preetinest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    private final Environment env;

    public OpenAPIConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();

        // Detect active profile
        boolean isLocal = List.of(env.getActiveProfiles()).contains("local");

        if (isLocal) {
            openAPI.addServersItem(new Server()
                    .url("http://localhost:8080")
                    .description("Local Development Server"));
        } else {
            openAPI.addServersItem(new Server()
                    .url("https://preetinest.ca")
                    .description("Production Server"));
        }

        return openAPI;
    }
}