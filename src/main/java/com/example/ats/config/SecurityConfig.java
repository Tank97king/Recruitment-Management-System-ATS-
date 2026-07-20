package com.example.ats.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration.
 *
 * <p>Sets up the stateless authentication filter chain utilizing JWT.
 * Integrates custom entry points and access denied handlers to output standardized JSON.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Stateless SecurityFilterChain.
     *
     * <p>Enforces authorization checks on endpoints based on Roles:
     * <ul>
     *   <li>{@code /api/auth/**}, Swagger, and Actuator are public.</li>
     *   <li>{@code /api/v1/users/**} and {@code /api/v1/roles/**} require {@code ADMIN}.</li>
     *   <li>All other business APIs require either {@code ADMIN} or {@code RECRUITER}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS & Disable CSRF since REST APIs are stateless and use JWTs
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                
                // Set session management to stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configure request-level authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Permit all OPTIONS preflight requests
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/jobs", "/api/v1/jobs/*").permitAll()
                        
                        // User profile endpoints (any authenticated user)
                        .requestMatchers("/api/v1/users/profile", "/api/v1/users/me").authenticated()
                        
                        // Admin-only management endpoints
                        .requestMatchers("/api/v1/users/**", "/api/v1/roles/**", "/api/v1/audit-logs/**").hasRole("ADMIN")
                        
                        // Business resources accessible to both Admin and Recruiter
                        .requestMatchers(
                                "/api/v1/companies/**",
                                "/api/v1/jobs/**",
                                "/api/v1/candidates/**",
                                "/api/v1/job-applications/**",
                                "/api/v1/interviews/**",
                                "/api/v1/pipeline/**",
                                "/api/v1/dashboard/**",
                                "/api/v1/search/**"
                        ).hasAnyRole("ADMIN", "RECRUITER")
                        
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                
                // Configure custom exception entry points
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                
                // Configure explicit security HTTP headers
                .headers(headers -> headers
                        .contentTypeOptions(contentTypeOptions -> {})
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                        ))
                )
                
                // Add JWT filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * BCrypt Password Encoder Bean.
     *
     * <p>BCrypt is the industry-standard adaptive hashing algorithm for passwords.
     * We use a strength factor of 12 for robust security.
     *
     * @return a {@link BCryptPasswordEncoder} with strength factor 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes Spring Security's AuthenticationManager bean.
     *
     * @param config the Spring Boot autoconfigured {@link AuthenticationConfiguration}
     * @return the configured {@link AuthenticationManager}
     * @throws Exception if manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
