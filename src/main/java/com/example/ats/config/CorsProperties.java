package com.example.ats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * CORS configurations mapped from app.cors.*.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private String allowedOrigins;
    private long maxAgeSeconds;
}
