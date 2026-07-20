package com.example.ats.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Basic application metadata properties mapped from spring.application.* and info.*.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

    @NotBlank(message = "Spring application name must not be blank")
    private String name;
}
