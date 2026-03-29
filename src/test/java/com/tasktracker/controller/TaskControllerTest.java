package com.tasktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.dto.request.LoginRequest;
import com.tasktracker.dto.request.RegisterRequest;
import com.tasktracker.dto.request.TaskRequest;
import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login to get auth token
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser", "test@example.com", "password123", "Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(responseBody).get("data").get("accessToken").asText();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/tasks - Should create a new task")
    void testCreateTask() throws Exception {
        TaskRequest request = new TaskRequest(
                "Test Task", "Task description", null, TaskPriority.HIGH,
                LocalDate.now().plusDays(7), null, null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Task"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/tasks - Should list tasks")
    void testGetTasks() throws Exception {
        // Create a task first
        TaskRequest request = new TaskRequest(
                "Test Task", "Description", null, null,
                LocalDate.now().plusDays(7), null, null);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // List tasks
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/tasks - Should filter tasks by status")
    void testFilterTasksByStatus() throws Exception {
        // Create tasks with different statuses
        TaskRequest openTask = new TaskRequest(
                "Open Task", "Description", TaskStatus.OPEN, null, null, null, null);
        TaskRequest completedTask = new TaskRequest(
                "Completed Task", "Description", TaskStatus.COMPLETED, null, null, null, null);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openTask)));

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completedTask)));

        // Filter by OPEN status
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Open Task"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/tasks - Should search tasks by title")
    void testSearchTasks() throws Exception {
        TaskRequest task1 = new TaskRequest(
                "Fix login bug", "Description", null, null, null, null, null);
        TaskRequest task2 = new TaskRequest(
                "Add dashboard", "Description", null, null, null, null, null);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1)));

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task2)));

        // Search for "login"
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("search", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Fix login bug"));
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/tasks/{id} - Should update a task")
    void testUpdateTask() throws Exception {
        // Create task
        TaskRequest createRequest = new TaskRequest(
                "Original Title", "Original Description", null, null, null, null, null);

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Update task
        TaskRequest updateRequest = new TaskRequest(
                "Updated Title", "Updated Description", TaskStatus.IN_PROGRESS,
                TaskPriority.CRITICAL, LocalDate.now().plusDays(3), null, null);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.priority").value("CRITICAL"));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/tasks/{id} - Should delete a task")
    void testDeleteTask() throws Exception {
        // Create task
        TaskRequest createRequest = new TaskRequest(
                "To Delete", "Description", null, null, null, null, null);

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Delete task
        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify it's gone
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/tasks - Should require authentication")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }
}
