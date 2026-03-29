package com.tasktracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for team data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse createdBy;
    private int memberCount;
    private List<TeamMemberResponse> members;
    private LocalDateTime createdAt;

    /**
     * Nested DTO for team member info.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMemberResponse {
        private Long userId;
        private String username;
        private String fullName;
        private String role;
        private LocalDateTime joinedAt;
    }
}
