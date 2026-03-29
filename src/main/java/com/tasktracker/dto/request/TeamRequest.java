package com.tasktracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a team.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
