package com.example.ats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for requesting a new access token using a refresh token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "TokenRefreshRequest",
    description = "Request payload to exchange an existing refresh token for a new access token."
)
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(
        description = "A valid refresh token obtained from the login or previous refresh response",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk0MDAwMDAwfQ...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}
