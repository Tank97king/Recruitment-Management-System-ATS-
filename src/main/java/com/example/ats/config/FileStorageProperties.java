package com.example.ats.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * File storage configurations mapped from app.file.*.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    @NotBlank(message = "File upload directory must not be blank")
    private String uploadDir;

    private String allowedExtensions;

    @Positive(message = "Max size in bytes must be positive")
    private long maxSizeBytes;
}
