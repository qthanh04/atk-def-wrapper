package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.request.LoginRequest;
import com.tool.atkdefbackend.model.request.TeamSignUpRequest;
import com.tool.atkdefbackend.model.response.TeamInfoResponse;
import com.tool.atkdefbackend.service.auth.AuthService;
import com.tool.atkdefbackend.service.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles team authentication operations including:
 * - Team login/signin
 * - Team registration/signup
 * - Current user information retrieval
 *
 * All endpoints are public (no JWT required) except /me
 * Rate limiting is applied to prevent brute-force attacks
 *
 * @author AnD.wrapper Team
 * @version 1.0
 * @since 2026-01-10
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "🔐 Authentication - Login, Register, Get current user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate team and get JWT token
     *
     * Alternative endpoint for login (same as /signin)
     * Validates credentials and returns JWT token on success
     *
     * @param loginRequest Login credentials (username/password)
     * @return ResponseEntity with JWT token and team info
     */
    @Operation(summary = "Team Login (Sign In)",
               description = "Authenticate team credentials and receive JWT access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateTeam(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Team login attempt: {}", loginRequest.getUsername());
        return authService.signIn(loginRequest);
    }

    /**
     * Register new team
     *
     * Creates a new team account with the provided information
     * Automatically generates username from team name
     * Returns JWT token on successful registration
     *
     * @param signUpRequest Team registration data
     * @return ResponseEntity with JWT token and team info
     */
    @Operation(summary = "Team Registration (Sign Up)",
               description = "Register a new team and receive JWT access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Validation error or team already exists"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerTeam(@Valid @RequestBody TeamSignUpRequest signUpRequest) {
        log.info("Team registration attempt: {}", signUpRequest.getTeamName());
        return authService.signUp(signUpRequest);
    }

    /**
     * Get current authenticated team information
     *
     * Returns information about the currently logged-in team
     * Requires valid JWT token in Authorization header
     *
     * @return ResponseEntity with current team info
     */
    @Operation(summary = "Get Current Team Info",
               description = "Retrieve information about the currently authenticated team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team info retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated or invalid token")
    })
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
