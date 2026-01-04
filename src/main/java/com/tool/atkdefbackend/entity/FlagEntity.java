package com.tool.atkdefbackend.entity;

import com.tool.atkdefbackend.enums.FlagType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Flag entity matching AnD.platform flags table
 */
@Entity
@Table(name = "flags", uniqueConstraints = @UniqueConstraint(columnNames = { "game_id", "team_id",
        "tick_id", "flag_type" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "team_id", nullable = false, length = 100)
    private String teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tick_id", nullable = false)
    private TickEntity tick;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, columnDefinition = "flagtype")
    private FlagType flagType;

    @Column(name = "flag_value", nullable = false, unique = true, length = 128)
    private String flagValue;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_stolen", nullable = false)
    private Boolean isStolen = false;

    @Column(name = "stolen_count", nullable = false)
    private Integer stolenCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FlagSubmissionEntity> submissions;
}