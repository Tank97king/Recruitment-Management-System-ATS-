package com.example.ats.util;

import com.example.ats.dto.response.AuthResponse;
import com.example.ats.dto.response.LoginResponse;
import com.example.ats.dto.response.UserResponse;
import com.example.ats.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link User} entity to response DTOs.
 *
 * <p>Registered as a Spring Component Model mapper, so it can be injected.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map a User entity to a UserResponse DTO.
     * Concat first and last name for fullName, extract role name, and convert enum status.
     *
     * @param user the User entity
     * @return the mapped UserResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + (user.getLastName() == null || user.getLastName().isEmpty() ? \"\" : \" \" + user.getLastName()))")
    @Mapping(target = "role", expression = "java(mapRole(user))")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserResponse toResponse(User user);

    /**
     * Map a User entity to an AuthResponse DTO.
     *
     * @param user the User entity
     * @return the mapped AuthResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + (user.getLastName() == null || user.getLastName().isEmpty() ? \"\" : \" \" + user.getLastName()))")
    @Mapping(target = "role", expression = "java(mapRole(user))")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    AuthResponse toAuthResponse(User user);

    /**
     * Map a User entity and token details to a LoginResponse DTO.
     *
     * @param user the User entity
     * @param accessToken the generated access token
     * @param refreshToken the generated refresh token
     * @param expiresIn access token expiry in seconds
     * @return the mapped LoginResponse DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + (user.getLastName() == null || user.getLastName().isEmpty() ? \"\" : \" \" + user.getLastName()))")
    @Mapping(target = "role", expression = "java(mapRole(user))")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "expiresIn", source = "expiresIn")
    @Mapping(target = "tokenType", constant = "Bearer")
    LoginResponse toLoginResponse(User user, String accessToken, String refreshToken, long expiresIn);

    /**
     * Helper method to map roles set to a single string role name.
     */
    default String mapRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "RECRUITER";
        }
        return user.getRoles().iterator().next().getName().name();
    }
}
