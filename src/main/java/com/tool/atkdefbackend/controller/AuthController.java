package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.request.LoginRequest;
import com.tool.atkdefbackend.model.request.TeamSignUpRequest;
import com.tool.atkdefbackend.model.response.TeamInfoResponse;
import com.tool.atkdefbackend.service.auth.AuthService;
import com.tool.atkdefbackend.service.auth.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Alias for login
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateTeam(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.signIn(loginRequest);
    }

    /**
     * Register new team
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerTeam(@Valid @RequestBody TeamSignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    /**
     * Get current logged-in team info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentTeam() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("UNKNOWN");

            TeamInfoResponse response = new TeamInfoResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getTeamName(),
                    role.replace("ROLE_", ""));
            return ResponseEntity.ok(response);
        }

        if (principal instanceof String username) {
            return ResponseEntity.ok(new TeamInfoResponse(null, username, null, "UNKNOWN"));
        }

        return ResponseEntity.status(401).body("Unauthorized");
    }
}
