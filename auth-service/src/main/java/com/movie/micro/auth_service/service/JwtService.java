package com.movie.micro.auth_service.service;

import com.movie.micro.auth_service.config.JwtProperties;
import com.movie.micro.auth_service.entity.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Account account) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .setSubject(account.getId().toString())
                .claim(CLAIM_TOKEN_TYPE, ACCESS_TOKEN_TYPE)
                .claim("email", account.getEmail())
                .claim("username", account.getUsername())
                .claim("role", account.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Account account) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.refreshExpirationMs());

        return Jwts.builder()
                .setSubject(account.getId().toString())
                .claim(CLAIM_TOKEN_TYPE, REFRESH_TOKEN_TYPE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractAccountIdFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    public Instant getAccessExpirationFromNow() {
        return Instant.now().plusMillis(jwtProperties.expirationMs());
    }

    public Instant getRefreshExpirationFromNow() {
        return Instant.now().plusMillis(jwtProperties.refreshExpirationMs());
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
