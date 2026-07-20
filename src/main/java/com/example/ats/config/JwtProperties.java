package com.example.ats.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration properties mapped from app.jwt.*.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    @Positive(message = "Access token expiration must be positive")
    private long accessTokenExpirationMs;

    @Positive(message = "Refresh token expiration must be positive")
    private long refreshTokenExpirationMs;
}
