package com.tool.atkdefbackend.entity;

import com.tool.atkdefbackend.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Game entity matching AnD.platform games table
 */
@Entity
@Table(name = "games", schema = "adg_core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "vulnbox_path", length = 500)
    private String vulnboxPath;

    @Column(name = "checker_module", length = 200)
    private String checkerModule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status = GameStatus.DRAFT;

    @Column(name = "tick_duration_seconds")
    private Integer tickDurationSeconds = 60;

    @Column(name = "current_tick")
    private Integer currentTick = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GameTeamEntity> gameTeams;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TickEntity> ticks;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FlagEntity> flags;
}
