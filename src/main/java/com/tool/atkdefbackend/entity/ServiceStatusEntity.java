package com.tool.atkdefbackend.entity;

import com.tool.atkdefbackend.enums.CheckStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ServiceStatus entity matching AnD.platform service_statuses table
 */
@Entity
@Table(name = "service_statuses", uniqueConstraints = @UniqueConstraint(columnNames = { "game_id",
        "team_id", "tick_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceStatusEntity {

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
    @Column(name = "status", nullable = false, columnDefinition = "checkstatus")
    private CheckStatus status;

    @Column(name = "sla_percentage", nullable = false)
    private Float slaPercentage = 100.0f;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "check_duration_ms")
    private Integer checkDurationMs;

    @CreationTimestamp
    @Column(name = "checked_at", updatable = false)
    private LocalDateTime checkedAt;
}
