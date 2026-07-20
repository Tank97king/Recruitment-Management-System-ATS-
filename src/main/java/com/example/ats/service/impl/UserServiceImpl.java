package com.example.ats.service.impl;

import com.example.ats.dto.request.UpdateUserRequest;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.dto.response.UserResponse;
import com.example.ats.entity.User;
import com.example.ats.enums.UserStatus;
import com.example.ats.exception.BusinessRuleViolationException;
import com.example.ats.exception.DuplicateResourceException;
import com.example.ats.exception.ResourceNotFoundException;
import com.example.ats.repository.UserRepository;
import com.example.ats.service.UserService;
import com.example.ats.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link UserService} for managing users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public PageResponse<UserResponse> getUsers(String search, String statusStr, Pageable pageable) {
        log.info("Fetching users with search: '{}', status: '{}', pageable: {}", search, statusStr, pageable);
        
        UserStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = UserStatus.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleViolationException("Invalid user status filter: " + statusStr);
            }
        }

        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<User> usersPage = userRepository.findAllActiveWithFilters(status, searchKeyword, pageable);
        Page<UserResponse> dtoPage = usersPage.map(userMapper::toResponse);
        return PageResponse.from(dtoPage);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if email is changed
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Validate and set status
        UserStatus status;
        try {
            status = UserStatus.valueOf(request.getStatus().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid user status: " + request.getStatus());
        }
        user.setStatus(status);

        // Split fullName using NameUtils utility
        com.example.ats.util.NameUtils.ParsedName parsedName = com.example.ats.util.NameUtils.parseFullName(request.getFullName());
        user.setFirstName(parsedName.getFirstName());
        user.setLastName(parsedName.getLastName());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully. ID: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("User with ID: {} soft-deleted successfully", id);
    }

    @Override
    public UserResponse getProfile(String email) {
        log.info("Fetching profile for user: {}", email);
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, com.example.ats.dto.request.UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", email);
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        com.example.ats.util.NameUtils.ParsedName parsedName = com.example.ats.util.NameUtils.parseFullName(request.getFullName());
        user.setFirstName(parsedName.getFirstName());
        user.setLastName(parsedName.getLastName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully. Email: {}", email);
        return userMapper.toResponse(updatedUser);
    }
}
