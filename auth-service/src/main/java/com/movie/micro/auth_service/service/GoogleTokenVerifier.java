package com.movie.micro.auth_service.service;

import com.movie.micro.auth_service.config.AppIntegrationProperties;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class GoogleTokenVerifier {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestClient restClient;
    private final AppIntegrationProperties appIntegrationProperties;

    public GoogleTokenVerifier(RestClient.Builder restClientBuilder, AppIntegrationProperties appIntegrationProperties) {
        this.restClient = restClientBuilder.baseUrl(GOOGLE_TOKEN_INFO_URL).build();
        this.appIntegrationProperties = appIntegrationProperties;
    }

    @SuppressWarnings("unchecked")
    public GoogleProfile verify(String idToken) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("id_token", idToken).build())
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalArgumentException("Cannot verify google token");
        }

        String email = stringValue(response.get("email"));
        String emailVerified = stringValue(response.get("email_verified"));
        String audience = stringValue(response.get("aud"));
        String name = stringValue(response.get("name"));
        String sub = stringValue(response.get("sub"));

        if (!StringUtils.hasText(email) || !"true".equalsIgnoreCase(emailVerified)) {
            throw new IllegalArgumentException("Google account email is not verified");
        }

        String configuredClientId = appIntegrationProperties.google() == null ? null : appIntegrationProperties.google().clientId();
        if (StringUtils.hasText(configuredClientId) && !configuredClientId.equals(audience)) {
            throw new IllegalArgumentException("Google token audience mismatch");
        }

        return new GoogleProfile(sub, email, name);
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    public record GoogleProfile(String googleId, String email, String name) {
    }
}
