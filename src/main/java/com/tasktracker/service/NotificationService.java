package com.tasktracker.service;

import com.tasktracker.dto.response.NotificationResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.entity.Notification;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.User;
import com.tasktracker.enums.NotificationType;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for notification management and real-time WebSocket delivery.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a notification and sends it in real-time via WebSocket.
     */
    @Transactional
    public void createNotification(User recipient, String message,
                                    NotificationType type, Task task) {
        Notification notification = Notification.builder()
                .user(recipient)
                .message(message)
                .type(type)
                .task(task)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationResponse response = mapToNotificationResponse(notification);
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/notifications",
                response
        );
    }

    /**
     * Notifies the assignee when a task is assigned to them.
     */
    public void notifyTaskAssigned(Task task) {
        if (task.getAssignee() != null) {
            createNotification(
                    task.getAssignee(),
                    "You have been assigned to task: " + task.getTitle(),
                    NotificationType.TASK_ASSIGNED,
                    task
            );
        }
    }

    /**
     * Notifies relevant users when a task is updated.
     */
    public void notifyTaskUpdated(Task task, User updatedBy) {
        // Notify creator if they didn't make the update
        if (!task.getCreatedBy().getId().equals(updatedBy.getId())) {
            createNotification(
                    task.getCreatedBy(),
                    updatedBy.getUsername() + " updated task: " + task.getTitle(),
                    NotificationType.TASK_UPDATED,
                    task
            );
        }
        // Notify assignee if they didn't make the update
        if (task.getAssignee() != null &&
                !task.getAssignee().getId().equals(updatedBy.getId()) &&
                !task.getAssignee().getId().equals(task.getCreatedBy().getId())) {
            createNotification(
                    task.getAssignee(),
                    updatedBy.getUsername() + " updated task: " + task.getTitle(),
                    NotificationType.TASK_UPDATED,
                    task
            );
        }
    }

    /**
     * Notifies relevant users when a task is completed.
     */
    public void notifyTaskCompleted(Task task, User completedBy) {
        if (!task.getCreatedBy().getId().equals(completedBy.getId())) {
            createNotification(
                    task.getCreatedBy(),
                    completedBy.getUsername() + " completed task: " + task.getTitle(),
                    NotificationType.TASK_COMPLETED,
                    task
            );
        }
    }

    /**
     * Retrieves notifications for the current user with pagination.
     */
    public PagedResponse<NotificationResponse> getUserNotifications(UserDetails userDetails,
                                                                     int page, int size) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(page, size));

        return PagedResponse.<NotificationResponse>builder()
                .content(notifications.getContent().stream()
                        .map(this::mapToNotificationResponse).toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .last(notifications.isLast())
                .build();
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, UserDetails userDetails) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);

        return mapToNotificationResponse(notification);
    }

    /**
     * Marks all notifications for the current user as read.
     */
    @Transactional
    public void markAllAsRead(UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        notificationRepository.markAllAsReadByUserId(currentUser.getId());
    }

    /**
     * Returns the count of unread notifications for the current user.
     */
    public long getUnreadCount(UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
