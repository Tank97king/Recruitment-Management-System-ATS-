package com.example.ats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application info properties mapped from info.app.*.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "info.app")
public class InfoProperties {

    private String version = "1.0.0";
}
