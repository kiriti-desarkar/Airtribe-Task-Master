package com.tasktracker.service;

import com.tasktracker.dto.request.CommentRequest;
import com.tasktracker.dto.response.CommentResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.entity.Comment;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.User;
import com.tasktracker.enums.NotificationType;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.exception.UnauthorizedException;
import com.tasktracker.repository.CommentRepository;
import com.tasktracker.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing comments on tasks.
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Adds a comment to a task.
     */
    @Transactional
    public CommentResponse addComment(Long taskId, UserDetails userDetails, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .user(currentUser)
                .build();

        comment = commentRepository.save(comment);

        // Notify task creator and assignee about the new comment
        if (!task.getCreatedBy().getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                    task.getCreatedBy(),
                    currentUser.getUsername() + " commented on task: " + task.getTitle(),
                    NotificationType.COMMENT_ADDED,
                    task
            );
        }
        if (task.getAssignee() != null &&
                !task.getAssignee().getId().equals(currentUser.getId()) &&
                !task.getAssignee().getId().equals(task.getCreatedBy().getId())) {
            notificationService.createNotification(
                    task.getAssignee(),
                    currentUser.getUsername() + " commented on task: " + task.getTitle(),
                    NotificationType.COMMENT_ADDED,
                    task
            );
        }

        return mapToCommentResponse(comment);
    }

    /**
     * Retrieves comments for a task with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getTaskComments(Long taskId, int page, int size) {
        // Verify task exists
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }

        Page<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(
                taskId, PageRequest.of(page, size));

        return PagedResponse.<CommentResponse>builder()
                .content(comments.getContent().stream()
                        .map(this::mapToCommentResponse).toList())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .last(comments.isLast())
                .build();
    }

    /**
     * Deletes a comment. Only the comment author can delete.
     */
    @Transactional
    public void deleteComment(Long taskId, Long commentId, UserDetails userDetails) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userService.mapToUserResponse(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
