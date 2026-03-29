package com.tasktracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication (login/register) containing the JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String username;

    public AuthResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
    }
}
