package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Team entity - combines login credentials and team info
 * Each team has ONE account to login
 * 
 * Relationships:
 * - Team → GameTeam (one-to-many): A team can participate in multiple games
 */
@Entity
@Table(name = "teams", schema = "adg_core", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // --- LOGIN CREDENTIALS ---
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username; // Login username

    @Column(name = "password", nullable = false)
    private String password; // Hashed password

    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = "TEAM"; // ADMIN or TEAM

    // --- TEAM INFO ---
    @Column(name = "name", nullable = false, length = 100)
    private String name; // Team display name

    @Column(name = "affiliation", length = 200)
    private String affiliation;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- RELATIONSHIPS ---

    /**
     * Game participations - this team's entries in various games
     * Each GameTeam record contains game-specific info (container, ssh, token)
     */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<GameTeamEntity> gameTeams;
}