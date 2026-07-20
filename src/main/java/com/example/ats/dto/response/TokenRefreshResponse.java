package com.example.ats.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) returned upon successful token refresh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "TokenRefreshResponse",
    description = "Response payload returned after successfully refreshing an access token."
)
public class TokenRefreshResponse {

    @Schema(
        description = "Newly generated JWT access token",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.new_access_token"
    )
    private String accessToken;

    @Schema(
        description = "Newly rotated refresh token (the old token is invalidated)",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.new_refresh_token"
    )
    private String refreshToken;

    @Schema(description = "Token type (always 'Bearer')", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Expiration time of the new access token in seconds", example = "3600")
    private long expiresIn;
}
