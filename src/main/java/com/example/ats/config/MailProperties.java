package com.example.ats.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Mail (SMTP) configurations mapped from spring.mail.*.
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {

    @NotBlank(message = "Mail username must not be blank")
    private String username;

    private String host;
    private int port;
}
