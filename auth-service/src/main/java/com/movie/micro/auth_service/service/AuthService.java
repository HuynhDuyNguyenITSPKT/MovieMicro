package com.movie.micro.auth_service.service;

import com.movie.micro.auth_service.dto.AuthResponse;
import com.movie.micro.auth_service.dto.GoogleLoginRequest;
import com.movie.micro.auth_service.dto.LoginRequest;
import com.movie.micro.auth_service.dto.RegisterRequestOtp;
import com.movie.micro.auth_service.dto.VerifyRegisterOtpRequest;
import com.movie.micro.auth_service.config.OtpProperties;
import com.movie.micro.auth_service.entity.Account;
import com.movie.micro.auth_service.entity.AuthProvider;
import com.movie.micro.auth_service.entity.RegistrationOtp;
import com.movie.micro.auth_service.entity.Role;
import com.movie.micro.auth_service.repository.AccountRepository;
import com.movie.micro.auth_service.repository.RegistrationOtpRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final OtpProperties otpProperties;
    private final MailService mailService;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserProfileClient userProfileClient;

    public AuthService(
            AccountRepository accountRepository,
            RegistrationOtpRepository registrationOtpRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            OtpProperties otpProperties,
            MailService mailService,
            JwtService jwtService,
            GoogleTokenVerifier googleTokenVerifier,
            UserProfileClient userProfileClient
    ) {
        this.accountRepository = accountRepository;
        this.registrationOtpRepository = registrationOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.otpProperties = otpProperties;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userProfileClient = userProfileClient;
    }

    @Transactional
    public void requestRegisterOtp(RegisterRequestOtp request) {
        String email = normalizeEmail(request.email());
        String username = request.username().trim();

        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (accountRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        String otp = otpService.generateSixDigits();

        RegistrationOtp registrationOtp = registrationOtpRepository.findByEmailIgnoreCase(email)
                .orElseGet(RegistrationOtp::new);
        registrationOtp.setEmail(email);
        registrationOtp.setUsername(username);
        registrationOtp.setPasswordHash(passwordEncoder.encode(request.password()));
        registrationOtp.setOtpHash(passwordEncoder.encode(otp));
        registrationOtp.setFullName(request.fullName());
        registrationOtp.setPhone(request.phone());
        registrationOtp.setDateOfBirth(request.dateOfBirth());
        registrationOtp.setExpiresAt(Instant.now().plusSeconds((long) otpProperties.expirationMinutes() * 60));
        registrationOtpRepository.save(registrationOtp);

        mailService.sendOtp(email, otp);
    }

    @Transactional
    public AuthResponse verifyRegisterOtp(VerifyRegisterOtpRequest request) {
        String email = normalizeEmail(request.email());

        RegistrationOtp pending = registrationOtpRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("No registration OTP request found"));

        if (Instant.now().isAfter(pending.getExpiresAt())) {
            registrationOtpRepository.delete(pending);
            throw new IllegalArgumentException("OTP expired");
        }

        if (!passwordEncoder.matches(request.otp(), pending.getOtpHash())) {
            throw new IllegalArgumentException("OTP is invalid");
        }

        if (accountRepository.existsByEmailIgnoreCase(email)) {
            registrationOtpRepository.delete(pending);
            throw new IllegalArgumentException("Email already exists");
        }
        if (accountRepository.existsByUsernameIgnoreCase(pending.getUsername())) {
            registrationOtpRepository.delete(pending);
            throw new IllegalArgumentException("Username already exists");
        }

        Account account = new Account();
        account.setEmail(email);
        account.setUsername(pending.getUsername());
        account.setPasswordHash(pending.getPasswordHash());
        account.setActive(true);
        account.setRole(Role.USER);
        account.setProvider(AuthProvider.LOCAL);
        Account saved = accountRepository.save(account);

        registrationOtpRepository.delete(pending);
        userProfileClient.createOrUpdateProfile(saved.getId(), saved.getEmail(), pending.getFullName(), pending.getPhone(), pending.getDateOfBirth());

        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse loginWithPassword(LoginRequest request) {
        String input = request.login().trim();
        Account account = input.contains("@")
                ? accountRepository.findByEmailIgnoreCase(input).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"))
                : accountRepository.findByUsernameIgnoreCase(input).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (account.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("Please use Google login for this account");
        }
        if (!account.isActive()) {
            throw new IllegalArgumentException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return buildAuthResponse(account);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleProfile profile = googleTokenVerifier.verify(request.idToken());
        String email = normalizeEmail(profile.email());

        Account account = accountRepository.findByEmailIgnoreCase(email)
                .map(existing -> {
                    if (existing.getProvider() == AuthProvider.LOCAL) {
                        throw new IllegalArgumentException("Email already registered with password login");
                    }
                    return existing;
                })
                .orElseGet(() -> createGoogleAccount(profile));

        userProfileClient.createOrUpdateProfile(
                account.getId(),
                account.getEmail(),
                StringUtils.hasText(request.fullName()) ? request.fullName() : profile.name(),
                request.phone(),
                request.dateOfBirth()
        );

        return buildAuthResponse(account);
    }

    private Account createGoogleAccount(GoogleTokenVerifier.GoogleProfile profile) {
        Account account = new Account();
        account.setEmail(normalizeEmail(profile.email()));
        account.setUsername(generateUniqueUsername(profile.email()));
        account.setPasswordHash(null);
        account.setActive(true);
        account.setRole(Role.USER);
        account.setProvider(AuthProvider.GOOGLE);
        return accountRepository.save(account);
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._]", "");
        String normalizedBase = StringUtils.hasText(base) ? base : "user";
        String candidate = normalizedBase;

        while (accountRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = normalizedBase + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        return candidate;
    }

    private AuthResponse buildAuthResponse(Account account) {
        return new AuthResponse(
                jwtService.generateAccessToken(account),
                "Bearer",
                jwtService.getExpirationFromNow(),
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                account.getRole().name()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
