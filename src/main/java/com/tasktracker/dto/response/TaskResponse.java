package com.tasktracker.dto.response;

import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for task data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private UserResponse createdBy;
    private UserResponse assignee;
    private TeamResponse team;
    private int commentCount;
    private int attachmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
