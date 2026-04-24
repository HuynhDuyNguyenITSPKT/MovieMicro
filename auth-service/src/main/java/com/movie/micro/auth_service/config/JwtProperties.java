package com.movie.micro.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Long expirationMs, Long refreshExpirationMs) {

	private static final long DEFAULT_ACCESS_EXPIRATION_MS = 86_400_000L;
	private static final long DEFAULT_REFRESH_EXPIRATION_MS = 604_800_000L;

	public JwtProperties {
		if (expirationMs == null || expirationMs <= 0) {
			expirationMs = DEFAULT_ACCESS_EXPIRATION_MS;
		}

		if (refreshExpirationMs == null || refreshExpirationMs <= 0) {
			refreshExpirationMs = DEFAULT_REFRESH_EXPIRATION_MS;
		}
	}
}
