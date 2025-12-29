package com.tool.atkdefbackend.entity;

import com.tool.atkdefbackend.enums.TickStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Tick entity matching AnD.platform ticks table
 * Represents a time period in the game
 */
@Entity
@Table(name = "ticks", schema = "adg_core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TickEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "tick_number", nullable = false)
    private Integer tickNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TickStatus status = TickStatus.PENDING;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "flags_placed")
    private Integer flagsPlaced = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "tick", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FlagEntity> flags;

    @OneToMany(mappedBy = "tick", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ServiceStatusEntity> serviceStatuses;
}
