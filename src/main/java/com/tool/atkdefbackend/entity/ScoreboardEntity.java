package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Scoreboard entity matching AnD.platform scoreboard table
 */
@Entity
@Table(name = "scoreboard", schema = "adg_core", uniqueConstraints = @UniqueConstraint(columnNames = { "game_id",
        "team_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreboardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "team_id", nullable = false, length = 100)
    private String teamId;

    @Column(name = "attack_points")
    private Integer attackPoints = 0;

    @Column(name = "defense_points")
    private Integer defensePoints = 0;

    @Column(name = "sla_points")
    private Integer slaPoints = 0;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "rank")
    private Integer rank = 0;

    @Column(name = "flags_captured")
    private Integer flagsCaptured = 0;

    @Column(name = "flags_lost")
    private Integer flagsLost = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
