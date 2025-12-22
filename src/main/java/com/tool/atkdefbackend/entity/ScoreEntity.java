package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Điểm của Team nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    // Điểm tại hiệp nào? (Khác với Flag/Submissions, ở đây DB dùng round_id FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    @Column(name = "offense_score")
    private Double offenseScore; // Điểm tấn công

    @Column(name = "defense_score")
    private Double defenseScore; // Điểm phòng thủ

    @Column(name = "sla_score")
    private Double slaScore; // Điểm SLA (Service uptime)

    @Column(name = "total_score")
    private Double totalScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}