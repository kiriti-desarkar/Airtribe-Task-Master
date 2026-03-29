package com.tasktracker.service;

import com.tasktracker.dto.request.LoginRequest;
import com.tasktracker.dto.request.RegisterRequest;
import com.tasktracker.dto.response.AuthResponse;
import com.tasktracker.dto.response.UserResponse;
import com.tasktracker.entity.User;
import com.tasktracker.exception.BadRequestException;
import com.tasktracker.repository.UserRepository;
import com.tasktracker.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user registration and authentication.
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Registers a new user account.
     *
     * @param request the registration details
     * @return the created user profile
     * @throws BadRequestException if username or email already exists
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        // Create and save user with hashed password
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials
     * @return authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        // Get user details for the response
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BadRequestException("User not found")));

        return new AuthResponse(jwt, user.getId(), user.getUsername());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
