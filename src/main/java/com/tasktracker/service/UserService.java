package com.tasktracker.service;

import com.tasktracker.dto.request.UpdateProfileRequest;
import com.tasktracker.dto.response.UserResponse;
import com.tasktracker.entity.User;
import com.tasktracker.exception.BadRequestException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile management operations.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the current authenticated user's profile.
     */
    public UserResponse getCurrentUser(UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        return mapToUserResponse(user);
    }

    /**
     * Retrieves a user by their ID.
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToUserResponse(user);
    }

    /**
     * Updates the current user's profile.
     */
    @Transactional
    public UserResponse updateProfile(UserDetails userDetails, UpdateProfileRequest request) {
        User user = getUserByUsername(userDetails.getUsername());

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            // Check if the new email is already taken by another user
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    /**
     * Helper method to get a User entity by username.
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Helper method to get a User entity by ID.
     */
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponse mapToUserResponse(User user) {
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
