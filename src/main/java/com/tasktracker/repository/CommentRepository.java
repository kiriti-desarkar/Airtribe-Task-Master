package com.tasktracker.repository;

import com.tasktracker.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);
}
