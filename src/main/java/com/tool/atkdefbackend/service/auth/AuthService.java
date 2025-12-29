package com.tool.atkdefbackend.service.auth;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.model.request.LoginRequest;
import com.tool.atkdefbackend.model.request.TeamSignUpRequest;
import com.tool.atkdefbackend.model.response.JwtResponse;
import com.tool.atkdefbackend.repository.TeamRepository;
import com.tool.atkdefbackend.config.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TeamRepository teamRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager, TeamRepository teamRepository,
            PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.teamRepository = teamRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Team login - returns JWT token
     */
    public ResponseEntity<?> signIn(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getTeamName(),
                roles));
    }

    /**
     * Register new team account
     */
    public ResponseEntity<?> signUp(TeamSignUpRequest signUpRequest) {
        if (teamRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (teamRepository.existsByName(signUpRequest.getTeamName())) {
            return ResponseEntity.badRequest().body("Error: Team name is already in use!");
        }

        // Create new team account
        TeamEntity team = TeamEntity.builder()
                .username(signUpRequest.getUsername())
                .password(encoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getTeamName())
                .affiliation(signUpRequest.getAffiliation())
                .country(signUpRequest.getCountry())
                .role("TEAM") // Default role for new teams
                .build();

        teamRepository.save(team);
        return ResponseEntity.ok("Team registered successfully!");
    }

    /**
     * Register admin account (for internal use)
     */
    public ResponseEntity<?> createAdmin(String username, String password) {
        if (teamRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        TeamEntity admin = TeamEntity.builder()
                .username(username)
                .password(encoder.encode(password))
                .name("Administrator")
                .role("ADMIN")
                .build();

        teamRepository.save(admin);
        return ResponseEntity.ok("Admin created successfully!");
    }
}
