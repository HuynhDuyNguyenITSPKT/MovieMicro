package com.movie.micro.user_service.dto;

import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        Long accountId,
        String email,
        String fullName,
        String phone,
        LocalDate dateOfBirth,
        Instant createdAt,
        Instant updatedAt
) {
}
