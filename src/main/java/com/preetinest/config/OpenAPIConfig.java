    // src/main/java/com/preetinest/config/OpenAPIConfig.java
    package com.preetinest.config;


    import io.swagger.v3.oas.models.OpenAPI;
    import io.swagger.v3.oas.models.servers.Server;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.env.Environment;

    import java.util.Arrays;
    import java.util.List;

    @Configuration
    public class OpenAPIConfig {

        private static final Logger log = LoggerFactory.getLogger(OpenAPIConfig.class);

        private final Environment env;

        public OpenAPIConfig(Environment env) {
            this.env = env;
        }

        @Bean
        public OpenAPI customOpenAPI() {
            OpenAPI openAPI = new OpenAPI();

            // Get active profiles
            String[] activeProfiles = env.getActiveProfiles();
            String activeProfilesStr = activeProfiles.length > 0
                    ? String.join(", ", activeProfiles)
                    : "(none - default profile active)";

            // Detect if "local" profile is active
            boolean isLocal = Arrays.asList(activeProfiles).contains("local");

            // LOG EVERYTHING - This will appear in console and logs
            log.info("=== Swagger Server Configuration ===");
            log.info("Active Spring profiles: [{}]", activeProfilesStr);
            log.info("'local' profile detected: {}", isLocal);
            log.info("Default profiles (always active): [{}]", String.join(", ", env.getDefaultProfiles()));


            if (isLocal) {
                log.warn("LOCAL profile is ACTIVE - Swagger will show http://localhost:8080");
                openAPI.addServersItem(new Server()
                        .url("http://localhost:8080")

                        .description("Local Development Server"));
            }

            else {
                log.warn("LOCAL profile is NOT active - Using PRODUCTION server: https://preetinest.ca");
                openAPI.addServersItem(new Server()
                        .url("https://preetinest.ca")
                        .description("Production Server"));
            }

            log.info("=====================================");

            return openAPI;
        }
    }