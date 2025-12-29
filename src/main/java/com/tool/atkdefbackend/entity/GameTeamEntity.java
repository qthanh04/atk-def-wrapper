package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GameTeam entity matching AnD.platform game_teams table
 * Represents a team's participation in a specific game
 * 
 * Relationships:
 * - Team → GameTeam (one-to-many): One team can join multiple games
 * - Game → GameTeam (one-to-many): One game has multiple team participants
 */
@Entity
@Table(name = "game_teams", schema = "adg_core", uniqueConstraints = @UniqueConstraint(columnNames = { "game_id",
        "team_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    /**
     * Reference to the Team entity (teams table)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    // --- Container/SSH Info (set when game starts) ---
    @Column(name = "container_name", length = 200)
    private String containerName;

    @Column(name = "container_ip", length = 50)
    private String containerIp;

    @Column(name = "ssh_username", length = 50)
    private String sshUsername;

    @Column(name = "ssh_password", length = 100)
    private String sshPassword;

    @Column(name = "ssh_port")
    private Integer sshPort;

    /**
     * Team token for flag submission
     * Each team gets a unique token per game
     */
    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
