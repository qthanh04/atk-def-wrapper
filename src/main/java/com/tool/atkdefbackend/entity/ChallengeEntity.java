package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Quan hệ N-1: Nhiều Challenge thuộc về 1 Lab
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private LabEntity lab;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Cổng dịch vụ (Ví dụ: 80, 8080, 22)
    @Column(name = "service_port")
    private Integer servicePort;

    // Tên Docker Image (Ví dụ: ctf/web-sqli:latest)
    @Column(name = "image")
    private String image;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}