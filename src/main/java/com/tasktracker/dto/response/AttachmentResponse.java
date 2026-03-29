package com.tasktracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for attachment data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private UserResponse uploadedBy;
    private LocalDateTime uploadedAt;
}
