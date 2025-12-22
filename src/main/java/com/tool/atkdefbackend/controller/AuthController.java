package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.request.LoginRequest;
import com.tool.atkdefbackend.model.request.SignUpRequest;
import com.tool.atkdefbackend.model.response.UserInfoResponse;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.signIn(loginRequest);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.signIn(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        // Check if principal is UserDetailsImpl
        if (principal instanceof UserDetailsImpl userDetails) {
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("UNKNOWN");

            UserInfoResponse response = new UserInfoResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    role.replace("ROLE_", "") // Return TEACHER instead of ROLE_TEACHER
            );
            return ResponseEntity.ok(response);
        }

        // If principal is a String (username), return basic info
        if (principal instanceof String username) {
            return ResponseEntity.ok(new UserInfoResponse(
                    null,
                    username,
                    "UNKNOWN"));
        }

        return ResponseEntity.status(401).body("Unauthorized");
    }
}
