package com.tasktracker.service;

import com.tasktracker.dto.request.TeamRequest;
import com.tasktracker.dto.response.TeamResponse;
import com.tasktracker.dto.response.UserResponse;
import com.tasktracker.entity.Team;
import com.tasktracker.entity.TeamMember;
import com.tasktracker.entity.User;
import com.tasktracker.enums.NotificationType;
import com.tasktracker.enums.TeamRole;
import com.tasktracker.exception.BadRequestException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.exception.UnauthorizedException;
import com.tasktracker.repository.TeamMemberRepository;
import com.tasktracker.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for team/project management operations.
 */
@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Creates a new team and adds the creator as OWNER.
     */
    @Transactional
    public TeamResponse createTeam(UserDetails userDetails, TeamRequest request) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        team = teamRepository.save(team);

        // Add creator as team owner
        TeamMember ownerMember = TeamMember.builder()
                .team(team)
                .user(currentUser)
                .role(TeamRole.OWNER)
                .build();
        teamMemberRepository.save(ownerMember);

        return mapToTeamResponse(team);
    }

    /**
     * Retrieves all teams the current user belongs to.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getUserTeams(UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<Team> teams = teamRepository.findTeamsByUserId(currentUser.getId());
        return teams.stream().map(this::mapToTeamResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves a team by ID with full details.
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return mapToTeamResponse(team);
    }

    /**
     * Updates a team's details. Only OWNER or ADMIN can update.
     */
    @Transactional
    public TeamResponse updateTeam(Long id, UserDetails userDetails, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        validateTeamAdmin(id, currentUser.getId());

        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team = teamRepository.save(team);

        return mapToTeamResponse(team);
    }

    /**
     * Deletes a team. Only the OWNER can delete.
     */
    @Transactional
    public void deleteTeam(Long id, UserDetails userDetails) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        if (!team.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the team owner can delete this team");
        }

        teamRepository.delete(team);
    }

    /**
     * Adds a user to a team.
     */
    @Transactional
    public TeamResponse addMember(Long teamId, Long userId, UserDetails userDetails) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        validateTeamAdmin(teamId, currentUser.getId());

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new BadRequestException("User is already a member of this team");
        }

        User newMember = userService.getUserEntityById(userId);

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(newMember)
                .role(TeamRole.MEMBER)
                .build();
        teamMemberRepository.save(member);

        // Notify the new member
        notificationService.createNotification(
                newMember,
                "You have been added to team: " + team.getName(),
                NotificationType.TEAM_INVITATION,
                null
        );

        return mapToTeamResponse(team);
    }

    /**
     * Removes a member from a team.
     */
    @Transactional
    public void removeMember(Long teamId, Long userId, UserDetails userDetails) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", "id", teamId);
        }

        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        validateTeamAdmin(teamId, currentUser.getId());

        // Cannot remove the owner
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMember", "userId", userId));

        if (member.getRole() == TeamRole.OWNER) {
            throw new BadRequestException("Cannot remove the team owner");
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    // ─── Helper Methods ─────────────────────────────────────────────

    private void validateTeamAdmin(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this team"));

        if (member.getRole() != TeamRole.OWNER && member.getRole() != TeamRole.ADMIN) {
            throw new UnauthorizedException("You don't have admin permissions for this team");
        }
    }

    private TeamResponse mapToTeamResponse(Team team) {
        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());

        List<TeamResponse.TeamMemberResponse> memberResponses = members.stream()
                .map(m -> TeamResponse.TeamMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .fullName(m.getUser().getFullName())
                        .role(m.getRole().name())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        UserResponse createdByResponse = null;
        if (team.getCreatedBy() != null) {
            createdByResponse = userService.mapToUserResponse(team.getCreatedBy());
        }

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdBy(createdByResponse)
                .memberCount(members.size())
                .members(memberResponses)
                .createdAt(team.getCreatedAt())
                .build();
    }
}
