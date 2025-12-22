package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Team nào nộp?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    // Nộp trúng Flag ID nào?
    // Để nullable = true vì có thể nộp chuỗi linh tinh không khớp flag nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id")
    private FlagEntity flag;

    @Column(name = "submitted_value")
    private String submittedValue;

    @Column(name = "is_valid")
    private Boolean isValid;

    // Nộp tại hiệp số mấy
    @Column(name = "round")
    private Integer round;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}