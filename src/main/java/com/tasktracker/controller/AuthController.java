package com.tasktracker.controller;

import com.tasktracker.dto.request.LoginRequest;
import com.tasktracker.dto.request.RegisterRequest;
import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.AuthResponse;
import com.tasktracker.dto.response.UserResponse;
import com.tasktracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user credentials and returns a JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Logs out the current user (client-side token invalidation).
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logs out the current user (client should discard the JWT token)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless; logout is handled client-side by discarding the token
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
