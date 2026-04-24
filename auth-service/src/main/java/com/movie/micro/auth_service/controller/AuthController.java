package com.movie.micro.auth_service.controller;

import com.movie.micro.auth_service.dto.ApiMessageResponse;
import com.movie.micro.auth_service.dto.AuthResponse;
import com.movie.micro.auth_service.dto.GoogleLoginRequest;
import com.movie.micro.auth_service.dto.LoginRequest;
import com.movie.micro.auth_service.dto.RefreshTokenRequest;
import com.movie.micro.auth_service.dto.RegisterRequestOtp;
import com.movie.micro.auth_service.dto.VerifyRegisterOtpRequest;
import com.movie.micro.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/request-otp")
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageResponse requestOtp(@Valid @RequestBody RegisterRequestOtp request) {
        authService.requestRegisterOtp(request);
        return new ApiMessageResponse("OTP sent to email");
    }

    @PostMapping("/register/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyRegisterOtpRequest request) {
        return authService.verifyRegisterOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.loginWithPassword(request);
    }

    @PostMapping("/google-login")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request);
    }
}
