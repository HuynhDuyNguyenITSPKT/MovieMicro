package com.movie.micro.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record GoogleLoginRequest(
        @NotBlank(message = "Google id token is required")
        String idToken,
        String fullName,
        String phone,
        LocalDate dateOfBirth
) {
}
