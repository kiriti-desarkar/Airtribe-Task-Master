package com.tasktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.dto.request.LoginRequest;
import com.tasktracker.dto.request.RegisterRequest;
import com.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register - Should register a new user successfully")
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "password123", "Test User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register - Should fail with duplicate username")
    void testRegister_DuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@example.com", "password123", "Test User");

        // First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Duplicate registration
        RegisterRequest duplicate = new RegisterRequest(
                "testuser", "other@example.com", "password123", "Other User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register - Should fail with invalid email")
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testuser", "invalid-email", "password123", "Test User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login - Should login successfully")
    void testLogin_Success() throws Exception {
        // Register first
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser", "test@example.com", "password123", "Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login - Should fail with wrong password")
    void testLogin_WrongPassword() throws Exception {
        // Register first
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser", "test@example.com", "password123", "Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login with wrong password
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
