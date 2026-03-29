package com.tasktracker.controller;

import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.NotificationResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for notification management.
 * Real-time notifications are delivered via WebSocket at /ws (see WebSocketConfig).
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints (REST + WebSocket)")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves notifications for the current user.
     */
    @GetMapping
    @Operation(summary = "List my notifications")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<NotificationResponse> notifications =
                notificationService.getUserNotifications(userDetails, page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Returns the count of unread notifications.
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    /**
     * Marks a notification as read.
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationResponse notification = notificationService.markAsRead(id, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * Marks all notifications for the current user as read.
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
