package com.tasktracker.dto.response;

import com.tasktracker.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Long taskId;
    private LocalDateTime createdAt;
}
