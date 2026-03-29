package com.tasktracker.repository;

import com.tasktracker.entity.Task;
import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for Task entity operations with filtering, searching, and sorting support.
 * Uses JpaSpecificationExecutor for dynamic, type-safe query building.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /** Find all tasks assigned to a specific user. */
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    /** Find all tasks created by a specific user. */
    Page<Task> findByCreatedById(Long createdById, Pageable pageable);

    /** Find all tasks for a specific team. */
    Page<Task> findByTeamId(Long teamId, Pageable pageable);

    /** Find tasks by status. */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /** Find tasks by priority. */
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
}
