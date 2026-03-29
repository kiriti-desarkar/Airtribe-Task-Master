package com.tasktracker.controller;

import com.tasktracker.dto.request.CommentRequest;
import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.CommentResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for task comment operations.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@Tag(name = "Comments", description = "Task comment endpoints")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Adds a comment to a task.
     */
    @PostMapping
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.addComment(taskId, userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    /**
     * Lists comments for a task.
     */
    @GetMapping
    @Operation(summary = "List comments for a task")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getTaskComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<CommentResponse> comments = commentService.getTaskComments(taskId, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    /**
     * Deletes a comment.
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(taskId, commentId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
