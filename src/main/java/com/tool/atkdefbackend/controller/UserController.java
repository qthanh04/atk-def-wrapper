package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.request.CreateUserRequest;
import com.tool.atkdefbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users - Create user (Teacher only)
     * Request: { "username": "sv1", "role": "STUDENT", "team_id": 10 }
     * Response: { "id": 2, "username": "sv1" }
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    /**
     * GET /api/users - List all users with pagination
     * Query params: page=1, size=20
     * Response: [{ "id": 1, "username": "sv1", "team": "Team1" }]
     */
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.getAllUsers(page, size);
    }

    /**
     * DELETE /api/users/{id} - Delete user
     * Response: { "message": "User deleted" }
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id);
    }
}
