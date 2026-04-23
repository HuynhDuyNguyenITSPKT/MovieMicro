package com.movie.micro.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record InternalUserProfileRequest(
        @NotNull(message = "accountId is required")
        Long accountId,
        @NotBlank(message = "email is required")
        @Email(message = "invalid email")
        String email,
        String fullName,
        String phone,
        LocalDate dateOfBirth
) {
}
