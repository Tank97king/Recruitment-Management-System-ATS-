package com.example.ats.service.impl;

import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import com.example.ats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service loading user-specific data from the database for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading UserDetails for email: {}", email);

        User user = userRepository.findByEmailAndIsDeletedFalse(email.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Authentication failed — email not found: {}", email);
                    return new UsernameNotFoundException("Invalid email or password");
                });

        // Map UserStatus.ACTIVE directly to Spring Security's enabled flag
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;

        // Map domain roles to SimpleGrantedAuthority with ROLE_ prefix
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());

        log.debug("UserDetails loaded successfully for: {}. Status: {}, Roles: {}", 
                email, user.getStatus(), authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                enabled,              // enabled
                true,                 // accountNonExpired
                true,                 // credentialsNonExpired
                true,                 // accountNonLocked
                authorities
        );
    }
}
