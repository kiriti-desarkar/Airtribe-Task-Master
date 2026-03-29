package com.tasktracker.dto.request;

import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Long assigneeId;

    private Long teamId;
}
