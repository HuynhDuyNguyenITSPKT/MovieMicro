package com.movie.micro.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppIntegrationProperties(
        Google google,
        UserService userService,
        String internalApiKey
) {
    public record Google(String clientId) {
    }

    public record UserService(String baseUrl) {
    }
}
