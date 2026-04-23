package com.movie.micro.auth_service.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateSixDigits() {
        int value = 100000 + RANDOM.nextInt(900000);
        return Integer.toString(value);
    }
}
