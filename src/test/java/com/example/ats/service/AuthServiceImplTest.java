package com.example.ats.service;

import com.example.ats.dto.request.LoginRequest;
import com.example.ats.dto.request.RegisterRequest;
import com.example.ats.dto.response.AuthResponse;
import com.example.ats.dto.response.LoginResponse;
import com.example.ats.entity.RefreshToken;
import com.example.ats.entity.Role;
import com.example.ats.entity.User;
import com.example.ats.enums.RoleName;
import com.example.ats.enums.UserStatus;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.repository.RoleRepository;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.impl.AuthServiceImpl;
import com.example.ats.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>All dependencies (repositories, encoder, JWT service, etc.) are Mockito mocks.
 * No Spring context or database is involved.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserMapper userMapper;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Role recruiterRole;
    private User savedUser;
    private RefreshToken mockRefreshToken;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .build();

        recruiterRole = new Role(RoleName.RECRUITER, "Default Recruiter role");

        savedUser = new User();
        savedUser.setEmail("john.doe@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setPasswordHash("$2a$10$hashedPassword");
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.addRole(recruiterRole);

        // Simulate UUID assignment (GenerationType.UUID sets it in JVM before INSERT)
        try {
            var idField = com.example.ats.entity.base.BaseEntity.class
                    .getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedUser, UUID.randomUUID());
        } catch (Exception ignored) {
            // Field injection — acceptable in test setup
        }

        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token-uuid");
        mockRefreshToken.setUser(savedUser);
    }

    // =========================================================================
    // register()
    // =========================================================================

    @Test
    @DisplayName("shouldRegisterUserSuccessfully — happy path")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmailAndIsDeletedFalse(registerRequest.getEmail()))
                .thenReturn(false);
        when(roleRepository.findByName(RoleName.RECRUITER))
                .thenReturn(Optional.of(recruiterRole));
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(userMapper.toAuthResponse(any(User.class)))
                .thenReturn(AuthResponse.builder()
                        .id(savedUser.getId())
                        .fullName("John Doe")
                        .email("john.doe@example.com")
                        .role("RECRUITER")
                        .status("ACTIVE")
                        .build());

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getRole()).isEqualTo("RECRUITER");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");

        verify(userRepository, times(1)).existsByEmailAndIsDeletedFalse(registerRequest.getEmail());
        verify(roleRepository, times(1)).findByName(RoleName.RECRUITER);
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogService, times(1)).logAction(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenEmailAlreadyExists — duplicate email registration")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmailAndIsDeletedFalse(registerRequest.getEmail()))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );
        assertThat(ex.getMessage()).contains("john.doe@example.com");

        // Verify save() was NEVER reached due to early exit
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("shouldRegisterUserWithSingleWordName — lastName becomes empty string")
    void shouldRegisterUserWithSingleWordName() {
        // Arrange
        RegisterRequest singleNameRequest = RegisterRequest.builder()
                .fullName("Madonna")
                .email("madonna@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .build();

        User singleNameUser = new User();
        singleNameUser.setEmail("madonna@example.com");
        singleNameUser.setFirstName("Madonna");
        singleNameUser.setLastName("");
        singleNameUser.setStatus(UserStatus.ACTIVE);
        singleNameUser.addRole(recruiterRole);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(singleNameUser, UUID.randomUUID());
        } catch (Exception ignored) {}

        when(userRepository.existsByEmailAndIsDeletedFalse("madonna@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.RECRUITER)).thenReturn(Optional.of(recruiterRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(singleNameUser);
        when(userMapper.toAuthResponse(any(User.class)))
                .thenReturn(AuthResponse.builder()
                        .id(singleNameUser.getId())
                        .fullName("Madonna")
                        .email("madonna@example.com")
                        .role("RECRUITER")
                        .status("ACTIVE")
                        .build());

        // Act
        AuthResponse response = authService.register(singleNameRequest);

        // Assert — fullName equals only the first name when lastName is blank
        assertThat(response.getFullName()).isEqualTo("Madonna");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // =========================================================================
    // login()
    // =========================================================================

    @Test
    @DisplayName("shouldLoginSuccessfully — valid credentials return tokens")
    void shouldLoginSuccessfully() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Authentication success — return value is unused by service
        when(userRepository.findByEmailAndIsDeletedFalse("john.doe@example.com"))
                .thenReturn(Optional.of(savedUser));
        when(jwtService.generateAccessToken(savedUser))
                .thenReturn("mock-jwt-access-token");
        when(refreshTokenService.createRefreshToken(savedUser))
                .thenReturn(mockRefreshToken);
        when(jwtService.getExpirationInSeconds())
                .thenReturn(3600L);
        when(userMapper.toLoginResponse(any(User.class), anyString(), anyString(), anyLong()))
                .thenReturn(LoginResponse.builder()
                        .accessToken("mock-jwt-access-token")
                        .refreshToken("mock-refresh-token-uuid")
                        .tokenType("Bearer")
                        .email("john.doe@example.com")
                        .role("RECRUITER")
                        .build());

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token-uuid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getRole()).isEqualTo("RECRUITER");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(savedUser);
        verify(refreshTokenService, times(1)).createRefreshToken(savedUser);
        verify(auditLogService, times(1)).logAction(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenLoginCredentialsAreInvalid — bad password")
    void shouldThrowExceptionWhenLoginCredentialsAreInvalid() {
        // Arrange — AuthenticationManager throws BadCredentialsException on wrong password
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );
        assertThat(ex.getMessage()).contains("Bad credentials");

        // Verify JWT was NEVER generated because authentication failed
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}
