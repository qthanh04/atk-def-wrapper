package com.tool.atkdefbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // Cờ này thuộc service nào (của team nào)?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    // Giá trị cờ: FLAG{...}
    @Column(name = "value", nullable = false)
    private String value;

    // Hiệp số mấy (Tick number).
    // Lưu ý: Trong hình DB là cột 'round' (int), không phải 'round_id' (FK)
    @Column(name = "round")
    private Integer round;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}