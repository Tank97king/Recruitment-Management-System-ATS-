package com.example.ats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Server configuration properties mapped from server.*.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerProperties {

    private int port = 8080;
}
