package com.tool.atkdefbackend.service;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.entity.security.ERole;
import com.tool.atkdefbackend.entity.security.RoleEntity;
import com.tool.atkdefbackend.entity.security.UserEntity;
import com.tool.atkdefbackend.model.request.CreateUserRequest;
import com.tool.atkdefbackend.model.response.UserResponse;
import com.tool.atkdefbackend.repository.RoleRepository;
import com.tool.atkdefbackend.repository.TeamRepository;
import com.tool.atkdefbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
            TeamRepository teamRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is already taken!"));
        }

        // Generate default password (username + "123")
        String defaultPassword = request.getUsername() + "123";

        // Create new user
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(defaultPassword));

        // Set role
        Set<RoleEntity> roles = new HashSet<>();
        ERole roleEnum = switch (request.getRole() != null ? request.getRole().toUpperCase() : "STUDENT") {
            case "TEACHER" -> ERole.ROLE_TEACHER;
            case "TEAM" -> ERole.ROLE_TEAM;
            default -> ERole.ROLE_STUDENT;
        };

        RoleEntity role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(role);
        user.setRoles(roles);

        // Set team if provided
        if (request.getTeamId() != null) {
            TeamEntity team = teamRepository.findById(request.getTeamId())
                    .orElse(null);
            user.setTeam(team);
        }

        userRepository.save(user);

        return ResponseEntity.status(201).body(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "defaultPassword", defaultPassword));
    }

    public ResponseEntity<?> getAllUsers(int page, int size) {
        Page<UserEntity> userPage = userRepository.findAll(PageRequest.of(page, size));

        List<UserResponse> users = userPage.getContent().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRoles().stream()
                                .findFirst()
                                .map(r -> r.getName().name().replace("ROLE_", ""))
                                .orElse("UNKNOWN"))
                        .team(user.getTeam() != null ? user.getTeam().getName() : null)
                        .build())
                .toList();

        return ResponseEntity.ok(users);
    }

    public ResponseEntity<?> deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
