package com.achal.jobportal.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// The app.jwt.* settings from application.properties.
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
