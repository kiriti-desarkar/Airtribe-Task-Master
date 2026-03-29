package com.tasktracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Email(message = "Email must be a valid email address")
    private String email;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
