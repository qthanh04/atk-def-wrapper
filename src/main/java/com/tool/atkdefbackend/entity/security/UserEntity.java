package com.tool.atkdefbackend.entity.security;

import com.tool.atkdefbackend.entity.TeamEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
})
public class UserEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @NotBlank
        @Size(min = 3, max = 20)
        private String username;

        @Email
        private String email;

        @Size(min = 6, max = 120)
        private String password;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<RoleEntity> roles = new HashSet<>();

        // Relationship with Team - One user belongs to one team
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "team_id")
        private TeamEntity team;

        public UserEntity(String username, String email, String password) {
                this.username = username;
                this.email = email;
                this.password = password;
        }
}
