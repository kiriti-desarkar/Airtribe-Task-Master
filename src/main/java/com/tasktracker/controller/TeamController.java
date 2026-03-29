package com.tasktracker.controller;

import com.tasktracker.dto.request.TeamRequest;
import com.tasktracker.dto.response.ApiResponse;
import com.tasktracker.dto.response.PagedResponse;
import com.tasktracker.dto.response.TaskResponse;
import com.tasktracker.dto.response.TeamResponse;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for team/project collaboration.
 */
@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team/project management and collaboration endpoints")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TaskService taskService;

    /**
     * Creates a new team.
     */
    @PostMapping
    @Operation(summary = "Create a new team")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TeamRequest request) {
        TeamResponse team = teamService.createTeam(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully", team));
    }

    /**
     * Lists all teams the current user belongs to.
     */
    @GetMapping
    @Operation(summary = "List my teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getUserTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamResponse> teams = teamService.getUserTeams(userDetails);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    /**
     * Retrieves a team by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        TeamResponse team = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(team));
    }

    /**
     * Updates a team's details.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a team")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TeamRequest request) {
        TeamResponse team = teamService.updateTeam(id, userDetails, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", team));
    }

    /**
     * Deletes a team.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        teamService.deleteTeam(id, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }

    /**
     * Adds a member to a team.
     */
    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to team")
    public ResponseEntity<ApiResponse<TeamResponse>> addMember(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body) {
        TeamResponse team = teamService.addMember(id, body.get("userId"), userDetails);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", team));
    }

    /**
     * Removes a member from a team.
     */
    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from team")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        teamService.removeMember(id, userId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    /**
     * Lists tasks for a specific team.
     */
    @GetMapping("/{id}/tasks")
    @Operation(summary = "List team tasks")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTeamTasks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<TaskResponse> tasks = taskService.getTeamTasks(id, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
}
