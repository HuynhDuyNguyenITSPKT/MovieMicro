package com.movie.micro.auth_service.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        Long accountId,
        String email,
        String username,
        String role
) {
}
