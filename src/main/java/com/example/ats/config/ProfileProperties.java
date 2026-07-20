package com.example.ats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring profile properties mapped from spring.profiles.*.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.profiles")
public class ProfileProperties {

    private String active = "default";
}
