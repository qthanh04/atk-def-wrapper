package com.tool.atkdefbackend.service;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.model.request.CreateTeamRequest;
import com.tool.atkdefbackend.model.request.UpdateTeamRequest;
import com.tool.atkdefbackend.model.response.TeamResponse;
import com.tool.atkdefbackend.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public TeamService(TeamRepository teamRepository, PasswordEncoder passwordEncoder) {
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a single team
     */
    public ResponseEntity<?> createTeam(CreateTeamRequest request) {
        // Check if team name already exists
        if (teamRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Team name already exists!"));
        }

        // Generate username from team name (lowercase, no spaces)
        String username = request.getName().toLowerCase().replaceAll("\\s+", "_");

        // Generate default password
        String defaultPassword = username + "123";

        TeamEntity team = TeamEntity.builder()
                .name(request.getName())
                .username(username)
                .password(passwordEncoder.encode(defaultPassword))
                .role("TEAM")
                .country(request.getCountry())
                .affiliation(request.getAffiliation())
                .ipAddress(request.getIpAddress())
                .build();

        teamRepository.save(team);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "id", team.getId(),
                "name", team.getName(),
                "username", username,
                "defaultPassword", defaultPassword));
    }

    /**
     * Import teams from CSV file
     * Expected CSV format: name,country,affiliation,ip_address
     */
    public ResponseEntity<?> importTeamsFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        List<TeamEntity> importedTeams = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length < 1) {
                    errors.add("Line " + lineNumber + ": Invalid format");
                    continue;
                }

                String name = columns[0].trim();
                String country = columns.length > 1 ? columns[1].trim() : "";
                String affiliation = columns.length > 2 ? columns[2].trim() : "";
                String ipAddress = columns.length > 3 ? columns[3].trim() : "";

                if (name.isEmpty()) {
                    errors.add("Line " + lineNumber + ": Team name is required");
                    continue;
                }

                // Skip if team name already exists
                if (teamRepository.existsByName(name)) {
                    errors.add("Line " + lineNumber + ": Team '" + name + "' already exists");
                    continue;
                }

                String username = name.toLowerCase().replaceAll("\\s+", "_");
                String defaultPassword = username + "123";

                TeamEntity team = TeamEntity.builder()
                        .name(name)
                        .username(username)
                        .password(passwordEncoder.encode(defaultPassword))
                        .role("TEAM")
                        .country(country)
                        .affiliation(affiliation)
                        .ipAddress(ipAddress)
                        .build();

                importedTeams.add(team);
            }

            // Save all valid teams
            teamRepository.saveAll(importedTeams);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to parse CSV file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "imported_count", importedTeams.size(),
                "errors", errors));
    }

    /**
     * Get all teams
     */
    public ResponseEntity<?> getAllTeams() {
        List<TeamResponse> teams = teamRepository.findAll().stream()
                .map(team -> TeamResponse.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .country(team.getCountry())
                        .affiliation(team.getAffiliation())
                        .ipAddress(team.getIpAddress())
                        .build())
                .toList();

        return ResponseEntity.ok(teams);
    }

    /**
     * Update team by ID
     */
    public ResponseEntity<?> updateTeam(Integer id, UpdateTeamRequest request) {
        return teamRepository.findById(id)
                .map(team -> {
                    if (request.getName() != null) {
                        team.setName(request.getName());
                    }
                    if (request.getCountry() != null) {
                        team.setCountry(request.getCountry());
                    }
                    if (request.getAffiliation() != null) {
                        team.setAffiliation(request.getAffiliation());
                    }
                    if (request.getIpAddress() != null) {
                        team.setIpAddress(request.getIpAddress());
                    }

                    teamRepository.save(team);

                    return ResponseEntity.ok(Map.of(
                            "id", team.getId(),
                            "updated", true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete team by ID
     */
    public ResponseEntity<?> deleteTeam(Integer id) {
        if (!teamRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        teamRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
