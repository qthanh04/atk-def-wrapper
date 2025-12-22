package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "config", columnDefinition = "jsonb")
    private String config;

    @Column(name = "created_at", updatable = true)//tu dong lay tg tao khi update
    private LocalDateTime createdAt;

    // Quan hệ 1-N: Một Lab có nhiều bài Challenge
    @OneToMany(mappedBy = "lab", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ChallengeEntity> challenges;

}
