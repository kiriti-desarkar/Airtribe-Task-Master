package com.tasktracker.repository;

import com.tasktracker.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Team entity operations.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /** Find all teams that a user is a member of. */
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.user.id = :userId")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);
}
