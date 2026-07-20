package com.example.ats.service;

import com.example.ats.dto.request.UpdateUserRequest;
import com.example.ats.dto.response.PageResponse;
import com.example.ats.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract defining the operations for managing system users.
 */
public interface UserService {

    /**
     * Retrieves a paginated list of active users, optionally filtered by keyword (name/email) and status.
     * Accessible by ADMIN only.
     *
     * @param search   the search keyword (case-insensitive substring match against name/email)
     * @param status   optional status filter (ACTIVE or INACTIVE)
     * @param pageable pagination and sorting parameters
     * @return a paginated response wrapper containing user details
     */
    PageResponse<UserResponse> getUsers(String search, String status, Pageable pageable);

    /**
     * Retrieves details of a specific user by their ID.
     * Accessible by ADMIN only.
     *
     * @param id the user UUID
     * @return the UserResponse DTO containing user profile info
     * @throws com.example.ats.exception.ResourceNotFoundException if user not found or is soft-deleted
     */
    UserResponse getUserById(UUID id);

    /**
     * Updates an existing user's details.
     * Accessible by ADMIN only.
     *
     * @param id      the user UUID to update
     * @param request the updated profile details
     * @return the updated UserResponse DTO
     * @throws com.example.ats.exception.ResourceNotFoundException if user not found or is soft-deleted
     * @throws com.example.ats.exception.DuplicateResourceException if the updated email is already taken by another active user
     */
    UserResponse updateUser(UUID id, UpdateUserRequest request);

    /**
     * Performs a soft delete of a user by setting the isDeleted flag to true.
     * Accessible by ADMIN only.
     *
     * @param id the user UUID to delete
     * @throws com.example.ats.exception.ResourceNotFoundException if user not found or is already soft-deleted
     */
    void deleteUser(UUID id);

    /**
     * Retrieves the profile details of the currently authenticated user by their email address.
     * Accessible by any authenticated user.
     *
     * @param email the authenticated user's email
     * @return the UserResponse DTO containing user profile info
     */
    UserResponse getProfile(String email);

    /**
     * Updates the profile details of the currently authenticated user.
     *
     * @param email   the authenticated user's email
     * @param request the updated profile details
     * @return the updated UserResponse DTO
     */
    UserResponse updateProfile(String email, com.example.ats.dto.request.UpdateProfileRequest request);
}
