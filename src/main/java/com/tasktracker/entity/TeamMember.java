package com.tasktracker.entity;

import com.tasktracker.enums.TeamRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user's membership in a team, including their role.
 */
@Entity
@Table(name = "team_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamRole role = TeamRole.MEMBER;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime joinedAt;
}
