package com.example.ats.service.impl;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.dto.request.TokenRefreshRequest;
import com.example.ats.dto.response.AuthResponse;
import com.example.ats.dto.response.LoginResponse;
import com.example.ats.dto.response.TokenRefreshResponse;
import com.example.ats.entity.Role;
import com.example.ats.entity.RefreshToken;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.AuthService;
import com.example.ats.service.AuditLogService;
import com.example.ats.service.JwtService;
import com.example.ats.service.RefreshTokenService;
import com.example.ats.util.NameUtils;
import com.example.ats.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation containing all business logic for registration and authorization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            log.warn("Registration failed — email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email address is already in use: " + request.getEmail());
        }

        // 2. Resolve default role: RECRUITER
        Role recruiterRole = roleRepository.findByName(RoleName.RECRUITER)
                .orElseGet(() -> {
                    log.info("Role RECRUITER not found in database; seeding automatically.");
                    Role newRole = new Role(RoleName.RECRUITER, "Default Recruiter role");
                    return roleRepository.save(newRole);
                });

        // 3. Create User entity and split full name into first and last name
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        
        // Hash password using BCrypt
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Status is ACTIVE by default
        user.setStatus(UserStatus.ACTIVE);
        
        // Assign default RECRUITER role
        user.addRole(recruiterRole);

        // Split fullName using NameUtils utility
        NameUtils.ParsedName parsedName = NameUtils.parseFullName(request.getFullName());
        user.setFirstName(parsedName.getFirstName());
        user.setLastName(parsedName.getLastName());

        // 4. Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully. Assigned ID: {}", savedUser.getId());

        auditLogService.logAction(savedUser.getId(), savedUser.getEmail(), "USER_REGISTER", "USER", savedUser.getId().toString(), "User registered successfully");

        // 5. Construct and return response DTO using MapStruct mapper
        return userMapper.toAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Attempting to authenticate user: {}", email);

        // 1. Authenticate credentials via standard Spring Security AuthenticationManager.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        // 2. If authentication succeeds, load the full user details to build the JWT and response metadata.
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        // 3. Generate JWT access token & Refresh Token
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User authenticated successfully: {}. Issued JWT and Refresh tokens.", email);

        auditLogService.logAction(user.getId(), user.getEmail(), "USER_LOGIN", "USER", user.getId().toString(), "User logged in successfully");

        // 4. Construct and return LoginResponse using MapStruct mapper
        return userMapper.toLoginResponse(user, accessToken, refreshToken.getToken(), jwtService.getExpirationInSeconds());
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        log.info("Attempting to refresh access token using refresh token");
        
        // Find token in database
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // Verify token expiration & revocation (deletes if expired/revoked)
        refreshTokenService.verifyExpiration(refreshToken);

        // Generate new access token
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        // Generate rotated new refresh token (token rotation)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Access token refreshed successfully for user: {}", user.getEmail());

        auditLogService.logAction(user.getId(), user.getEmail(), "REFRESH_TOKEN", "USER", user.getId().toString(), "Token refreshed successfully");

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
    }
}
