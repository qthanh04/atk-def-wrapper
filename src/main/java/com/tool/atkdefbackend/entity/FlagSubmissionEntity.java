package com.tool.atkdefbackend.entity;

import com.tool.atkdefbackend.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FlagSubmission entity matching AnD.platform flag_submissions table
 */
@Entity
@Table(name = "flag_submissions", schema = "adg_core")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "attacker_team_id", nullable = false, length = 100)
    private String attackerTeamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id")
    private FlagEntity flag;

    @Column(name = "submitted_flag", nullable = false, length = 128)
    private String submittedFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status;

    @Column(name = "points")
    private Integer points = 0;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}
