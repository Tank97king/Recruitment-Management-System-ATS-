package com.example.ats.service;

import com.example.ats.dto.request.UpdateUserRequest;
import com.example.ats.dto.response.UserResponse;
import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.impl.UserServiceImpl;
import com.example.ats.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * <p>Validates CRUD operations and exception paths for user management.
 * No Spring context or database involved — all collaborators are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    // ─── Mocked Dependencies ─────────────────────────────────────────────────

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    // ─── Subject Under Test ───────────────────────────────────────────────────

    @InjectMocks
    private UserServiceImpl userService;

    // ─── Test Fixtures ────────────────────────────────────────────────────────

    private UUID userId;
    private User activeUser;
    private User deletedUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        activeUser = new User();
        activeUser.setEmail("jane.doe@example.com");
        activeUser.setFirstName("Jane");
        activeUser.setLastName("Doe");
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setIsDeleted(false);

        try {
            var idField = com.example.ats.entity.base.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activeUser, userId);
        } catch (Exception ignored) {}

        deletedUser = new User();
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setIsDeleted(true);

        userResponse = UserResponse.builder()
                .id(userId)
                .email("jane.doe@example.com")
                .fullName("Jane Doe")
                .status("ACTIVE")
                .build();
    }

    // =========================================================================
    // getUserById()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnUserWhenFoundById — existing active user")
    void shouldReturnUserWhenFoundById() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userMapper.toResponse(activeUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toResponse(activeUser);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenUserIdDoesNotExist")
    void shouldThrowResourceNotFoundExceptionWhenUserIdDoesNotExist() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act + Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("user");

        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenUserIsSoftDeleted")
    void shouldThrowResourceNotFoundExceptionWhenUserIsSoftDeleted() {
        // Arrange — repository returns the soft-deleted user
        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedUser));

        // Act + Assert — service filter(!u.getIsDeleted()) triggers the orElseThrow
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        verify(userMapper, never()).toResponse(any());
    }

    // =========================================================================
    // updateUser()
    // =========================================================================

    @Test
    @DisplayName("shouldUpdateUserSuccessfully — same email path, no duplicate check needed")
    void shouldUpdateUserSuccessfully() {
        // Arrange
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .fullName("Jane Updated")
                .email("jane.doe@example.com") // same email — no uniqueness check
                .status("ACTIVE")
                .build();

        User updatedUser = new User();
        updatedUser.setEmail("jane.doe@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Updated");
        updatedUser.setStatus(UserStatus.ACTIVE);
        updatedUser.setIsDeleted(false);

        UserResponse updatedResponse = UserResponse.builder()
                .id(userId)
                .email("jane.doe@example.com")
                .fullName("Jane Updated")
                .status("ACTIVE")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        UserResponse result = userService.updateUser(userId, updateRequest);

        // Assert
        assertThat(result.getFullName()).isEqualTo("Jane Updated");
        verify(userRepository, times(1)).save(any(User.class));
        // Email unchanged — existsByEmail uniqueness check should NOT fire
        verify(userRepository, never()).existsByEmailAndIsDeletedFalse("jane.doe@example.com");
    }

    @Test
    @DisplayName("shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail")
    void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail() {
        // Arrange — user tries to change email to one already taken
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .fullName("Jane Doe")
                .email("taken@example.com")    // different email
                .status("ACTIVE")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.existsByEmailAndIsDeletedFalse("taken@example.com")).thenReturn(true);

        // Act + Assert
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateUser(userId, updateRequest)
        );
        assertThat(ex.getMessage()).containsIgnoringCase("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentUser")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .fullName("Ghost User")
                .email("ghost@example.com")
                .status("ACTIVE")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, updateRequest));

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // deleteUser()
    // =========================================================================

    @Test
    @DisplayName("shouldSoftDeleteUserSuccessfully — sets isDeleted flag and saves")
    void shouldSoftDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        // Act
        userService.deleteUser(userId);

        // Assert — isDeleted flag set to true
        assertThat(activeUser.getIsDeleted()).isTrue();
        verify(userRepository, times(1)).save(activeUser);
    }

    @Test
    @DisplayName("shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentUser")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));

        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // getProfile()
    // =========================================================================

    @Test
    @DisplayName("shouldReturnUserProfileByEmail — active user found")
    void shouldReturnUserProfileByEmail() {
        // Arrange
        when(userRepository.findByEmailAndIsDeletedFalse("jane.doe@example.com"))
                .thenReturn(Optional.of(activeUser));
        when(userMapper.toResponse(activeUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getProfile("jane.doe@example.com");

        // Assert
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        verify(userRepository, times(1)).findByEmailAndIsDeletedFalse("jane.doe@example.com");
    }
}
