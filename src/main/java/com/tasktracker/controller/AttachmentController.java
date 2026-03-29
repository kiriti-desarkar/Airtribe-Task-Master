package com.tasktracker.controller;

import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.AttachmentResponse;
import com.tasktracker.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for task file attachment operations.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
@Tag(name = "Attachments", description = "Task file attachment endpoints")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    /**
     * Uploads a file attachment to a task.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an attachment to a task")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        AttachmentResponse attachment = attachmentService.uploadAttachment(taskId, file, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", attachment));
    }

    /**
     * Lists all attachments for a task.
     */
    @GetMapping
    @Operation(summary = "List attachments for a task")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getTaskAttachments(
            @PathVariable Long taskId) {
        List<AttachmentResponse> attachments = attachmentService.getTaskAttachments(taskId);
        return ResponseEntity.ok(ApiResponse.success(attachments));
    }

    /**
     * Downloads a file attachment.
     */
    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download an attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        Resource resource = attachmentService.downloadAttachment(taskId, attachmentId);
        String fileName = attachmentService.getAttachmentFileName(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * Deletes a file attachment.
     */
    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        attachmentService.deleteAttachment(taskId, attachmentId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
