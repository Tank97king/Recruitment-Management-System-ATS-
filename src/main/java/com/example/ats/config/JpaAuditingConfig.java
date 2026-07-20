package com.example.ats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing Configuration.
 *
 * <p>This class provides the {@link AuditorAware} bean required by Spring Data JPA
 * to populate the {@code @CreatedBy} and {@code @LastModifiedBy} fields on
 * entities that extend {@link com.example.ats.entity.base.BaseEntity}.
 *
 * <h3>How It Works</h3>
 * <ol>
 *   <li>Spring Data JPA calls {@code getCurrentAuditor()} before every INSERT and UPDATE.</li>
 *   <li>The returned value (the current user's email) is stored in the
 *       {@code created_by} / {@code updated_by} columns automatically.</li>
 *   <li>Service methods do NOT need to manually set these fields.</li>
 * </ol>
 *
 * <h3>Current State (Phase 0/1 Placeholder)</h3>
 * <p>Since Spring Security's JWT filter chain is not yet implemented, the
 * {@code SecurityContext} will be empty for most requests. In that case,
 * this bean falls back to returning {@code "system"}.
 *
 * <p>In Phase 1, once {@code JwtAuthenticationFilter} populates the
 * {@code SecurityContextHolder}, this bean will automatically return
 * the authenticated user's email without any code change.
 */
@Configuration
public class JpaAuditingConfig {

    /**
     * Provides the currently authenticated user's identifier (email address)
     * for JPA auditing fields.
     *
     * <p>The return value is stored in:
     * <ul>
     *   <li>{@code created_by} — set once at INSERT time</li>
     *   <li>{@code updated_by} — updated at every UPDATE time</li>
     * </ul>
     *
     * @return an {@link AuditorAware} that resolves the current user's email,
     *         or {@code "system"} when no authentication context is available
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Return "system" if no authentication context is available.
            // This covers: startup/migration, background jobs, unauthenticated requests.
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            // Once JwtAuthenticationFilter is implemented in Phase 1,
            // authentication.getName() returns the email from the JWT subject claim.
            return Optional.ofNullable(authentication.getName())
                           .filter(name -> !name.isBlank())
                           .or(() -> Optional.of("system"));
        };
    }
}
