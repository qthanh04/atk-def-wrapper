package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // --- PHẦN THÔNG TIN ĐĂNG NHẬP (Mới thêm) ---
    @Column(name = "username", nullable = false, unique = true)
    private String username; // Tên đăng nhập của cả đội

    @Column(name = "password", nullable = false)
    private String password; // Mật khẩu dùng chung

    @Column(name = "role")
    private String role; // Mặc định là "TEAM", admin là "ADMIN"

    // --- PHẦN THÔNG TIN ĐỘI ---
    @Column(name = "name", nullable = false) // Tên hiển thị (Display Name)
    private String name;

    @Column(name = "affiliation")
    private String affiliation;

    @Column(name = "country")
    private String country;

    // IP Address để định danh trong mạng (như đã bàn)
    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- CÁC QUAN HỆ (Bỏ List<UserEntity>) ---

    // Một đội vẫn sở hữu nhiều Services, Scores, Submissions
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ServiceEntity> services;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ScoreEntity> scores;
}