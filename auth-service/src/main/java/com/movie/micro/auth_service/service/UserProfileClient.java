package com.movie.micro.auth_service.service;

import com.movie.micro.auth_service.config.AppIntegrationProperties;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserProfileClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileClient.class);

    private final RestClient restClient;
    private final String internalApiKey;

    public UserProfileClient(RestClient.Builder builder, AppIntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.userService().baseUrl()).build();
        this.internalApiKey = properties.internalApiKey();
    }

    public void createOrUpdateProfile(Long accountId, String email, String fullName, String phone, LocalDate dateOfBirth) {
        try {
            restClient.post()
                    .uri("/api/users/internal/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(new InternalCreateUserRequest(accountId, email, fullName, phone, dateOfBirth))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            LOGGER.warn("Could not create profile in user-service for account {}: {}", accountId, ex.getMessage());
        }
    }

    private record InternalCreateUserRequest(
            Long accountId,
            String email,
            String fullName,
            String phone,
            LocalDate dateOfBirth
    ) {
    }
}
