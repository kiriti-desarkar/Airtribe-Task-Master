package com.tasktracker.repository;

import com.tasktracker.entity.Task;
import com.tasktracker.enums.TaskPriority;
import com.tasktracker.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query specifications for Task filtering and searching.
 * Uses JPA Criteria API for type-safe, database-independent query building.
 */
public final class TaskSpecification {

    private TaskSpecification() {
        // Utility class
    }

    /**
     * Builds a composite Specification from optional filter parameters.
     *
     * @param status     filter by task status (nullable)
     * @param priority   filter by task priority (nullable)
     * @param assigneeId filter by assignee user ID (nullable)
     * @param teamId     filter by team ID (nullable)
     * @param search     search text in title and description (nullable)
     * @return a Specification combining all provided filters with AND logic
     */
    public static Specification<Task> withFilters(TaskStatus status, TaskPriority priority,
                                                   Long assigneeId, Long teamId, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }

            if (teamId != null) {
                predicates.add(cb.equal(root.get("team").get("id"), teamId));
            }

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
