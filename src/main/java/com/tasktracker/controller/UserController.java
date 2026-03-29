package com.tasktracker.controller;

import com.tasktracker.dto.request.UpdateProfileRequest;
import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.UserResponse;
import com.tasktracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile management endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves the current authenticated user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getCurrentUser(userDetails);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Updates the current user's profile.
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = userService.updateProfile(userDetails, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    /**
     * Retrieves a user by their ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
