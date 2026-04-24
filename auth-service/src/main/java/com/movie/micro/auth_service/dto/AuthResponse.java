package com.movie.micro.auth_service.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant expiresAt,
        Instant refreshExpiresAt,
        Long accountId,
        String email,
        String username,
        String role
) {
}
