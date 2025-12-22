package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Quan hệ N-1: Service này thuộc về Challenge nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ChallengeEntity challenge;

    // Quan hệ N-1: Service này của Team nào sở hữu?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    // ID của Docker Container (để lệnh stop/start)
    @Column(name = "container_id")
    private String containerId;

    // Trạng thái: RUNNING, STOPPED, ERROR
    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Quan hệ ngược (Optional) ---
    // Service chứa nhiều Flag và Healthcheck
    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FlagEntity> flags;
}
