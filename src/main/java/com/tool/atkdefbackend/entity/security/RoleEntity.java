    package com.tool.atkdefbackend.entity.security;

    import jakarta.persistence.*;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @Entity
    @Table(name = "roles")
    public class RoleEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        @Enumerated(EnumType.STRING)
        @Column(length = 20)
        private ERole name;
    }
