package com.tasktracker.service;

import com.tasktracker.dto.request.TaskRequest;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.dto.response.TaskResponse;
import com.tasktracker.dto.response.TeamResponse;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.Team;
import com.tasktracker.entity.User;
import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.exception.UnauthorizedException;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.TaskSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for task management operations including CRUD, filtering, sorting, and searching.
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Creates a new task.
     */
    @Transactional
    public TaskResponse createTask(UserDetails userDetails, TaskRequest request) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .createdBy(currentUser)
                .build();

        // Set assignee if provided
        if (request.getAssigneeId() != null) {
            User assignee = userService.getUserEntityById(request.getAssigneeId());
            task.setAssignee(assignee);
        }

        // Set team if provided
        if (request.getTeamId() != null) {
            Team team = new Team();
            team.setId(request.getTeamId());
            task.setTeam(team);
        }

        task = taskRepository.save(task);

        // Send notification to assignee
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(currentUser.getId())) {
            notificationService.notifyTaskAssigned(task);
        }

        return mapToTaskResponse(task);
    }

    /**
     * Retrieves a task by ID.
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return mapToTaskResponse(task);
    }

    /**
     * Updates an existing task.
     */
    @Transactional
    public TaskResponse updateTask(Long id, UserDetails userDetails, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        // Only creator or assignee can update
        if (!task.getCreatedBy().getId().equals(currentUser.getId()) &&
                (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId()))) {
            throw new UnauthorizedException("You don't have permission to update this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userService.getUserEntityById(request.getAssigneeId());
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);

        // Notify about update
        notificationService.notifyTaskUpdated(task, currentUser);

        return mapToTaskResponse(task);
    }

    /**
     * Deletes a task.
     */
    @Transactional
    public void deleteTask(Long id, UserDetails userDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        if (!task.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the task creator can delete this task");
        }

        taskRepository.delete(task);
    }

    /**
     * Updates the status of a task.
     */
    @Transactional
    public TaskResponse updateTaskStatus(Long id, UserDetails userDetails, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        task.setStatus(status);
        task = taskRepository.save(task);

        if (status == TaskStatus.COMPLETED) {
            notificationService.notifyTaskCompleted(task, currentUser);
        } else {
            notificationService.notifyTaskUpdated(task, currentUser);
        }

        return mapToTaskResponse(task);
    }

    /**
     * Assigns a task to a user.
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, Long assigneeId, UserDetails userDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User assignee = userService.getUserEntityById(assigneeId);
        task.setAssignee(assignee);
        task = taskRepository.save(task);

        notificationService.notifyTaskAssigned(task);

        return mapToTaskResponse(task);
    }

    /**
     * Retrieves tasks assigned to the current user.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getMyTasks(UserDetails userDetails, int page, int size,
                                                   String sortBy, String sortDir) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<Task> tasks = taskRepository.findByAssigneeId(currentUser.getId(), pageable);
        return mapToPagedResponse(tasks);
    }

    /**
     * Retrieves all tasks with optional filters, search, sorting, and pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(TaskStatus status, TaskPriority priority,
                                                 Long assigneeId, Long teamId, String search,
                                                 int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<Task> tasks = taskRepository.findAll(
                TaskSpecification.withFilters(status, priority, assigneeId, teamId, search),
                pageable);

        return mapToPagedResponse(tasks);
    }

    /**
     * Retrieves tasks for a specific team.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTeamTasks(Long teamId, int page, int size,
                                                     String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Task> tasks = taskRepository.findByTeamId(teamId, pageable);
        return mapToPagedResponse(tasks);
    }

    // ─── Helper Methods ─────────────────────────────────────────────

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<TaskResponse> mapToPagedResponse(Page<Task> tasks) {
        return PagedResponse.<TaskResponse>builder()
                .content(tasks.getContent().stream().map(this::mapToTaskResponse).toList())
                .page(tasks.getNumber())
                .size(tasks.getSize())
                .totalElements(tasks.getTotalElements())
                .totalPages(tasks.getTotalPages())
                .last(tasks.isLast())
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .commentCount(task.getComments() != null ? task.getComments().size() : 0)
                .attachmentCount(task.getAttachments() != null ? task.getAttachments().size() : 0)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        if (task.getCreatedBy() != null) {
            builder.createdBy(userService.mapToUserResponse(task.getCreatedBy()));
        }
        if (task.getAssignee() != null) {
            builder.assignee(userService.mapToUserResponse(task.getAssignee()));
        }
        if (task.getTeam() != null) {
            builder.team(TeamResponse.builder()
                    .id(task.getTeam().getId())
                    .name(task.getTeam().getName())
                    .build());
        }

        return builder.build();
    }
}
