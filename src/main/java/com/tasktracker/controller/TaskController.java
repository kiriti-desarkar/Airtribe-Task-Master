package com.tasktracker.controller;

import com.tasktracker.dto.request.TaskRequest;
import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.dto.response.TaskResponse;
import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import com.tasktracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for task management operations.
 * Supports CRUD, filtering, sorting, searching, and task assignment.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Creates a new task.
     */
    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    /**
     * Retrieves all tasks with optional filters, search, sorting, and pagination.
     */
    @GetMapping
    @Operation(summary = "List tasks with filters",
               description = "Retrieve tasks with optional filtering by status, priority, assignee, team, and search term")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasks(
            @Parameter(description = "Filter by status") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by priority") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Filter by assignee ID") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Filter by team ID") @RequestParam(required = false) Long teamId,
            @Parameter(description = "Search in title and description") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<TaskResponse> tasks = taskService.getTasks(
                status, priority, assigneeId, teamId, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    /**
     * Retrieves a task by its ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    /**
     * Updates an existing task.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.updateTask(id, userDetails, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    /**
     * Deletes a task.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    /**
     * Updates the status of a task (e.g., mark as completed).
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        TaskStatus status = TaskStatus.valueOf(body.get("status").toUpperCase());
        TaskResponse task = taskService.updateTaskStatus(id, userDetails, status);
        return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
    }

    /**
     * Assigns a task to a user.
     */
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign task to a user")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body) {
        TaskResponse task = taskService.assignTask(id, body.get("assigneeId"), userDetails);
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully", task));
    }

    /**
     * Retrieves tasks assigned to the current user.
     */
    @GetMapping("/my-tasks")
    @Operation(summary = "Get my assigned tasks")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getMyTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<TaskResponse> tasks = taskService.getMyTasks(userDetails, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
}
