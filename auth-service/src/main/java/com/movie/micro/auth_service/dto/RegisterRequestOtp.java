package com.movie.micro.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequestOtp(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 32, message = "Username length must be between 3 and 32")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password length must be between 6 and 72")
        String password,
        String fullName,
        String phone,
        LocalDate dateOfBirth
) {
}
