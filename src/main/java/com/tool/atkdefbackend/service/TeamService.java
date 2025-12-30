package com.tool.atkdefbackend.service;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.model.request.CreateTeamRequest;
import com.tool.atkdefbackend.model.request.UpdateTeamRequest;
import com.tool.atkdefbackend.model.response.TeamResponse;
import com.tool.atkdefbackend.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor // Tự tạo Constructor Injection
public class TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a single team
     * Returns the created entity, Controller will wrap it in ResponseEntity
     */
    public TeamEntity createTeam(CreateTeamRequest request) {
        if (teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Team name already exists!");
        }

        TeamEntity team = buildTeamEntity(
                request.getName(),
                request.getCountry(),
                request.getAffiliation(),
                request.getIpAddress());

        return teamRepository.save(team);
    }

    /**
     * Import teams from CSV
     * Uses @Transactional to ensure data integrity
     */
    @Transactional
    public Map<String, Object> importTeamsFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        List<TeamEntity> validTeams = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                } // Skip header
                if (line.trim().isEmpty())
                    continue; // Skip empty lines

                String[] columns = line.split(",", -1); // -1 to keep empty trailing strings
                if (columns.length < 1) {
                    errors.add("Line " + lineNumber + ": Invalid format");
                    continue;
                }

                String name = columns[0].trim();
                if (name.isEmpty()) {
                    errors.add("Line " + lineNumber + ": Name required");
                    continue;
                }

                if (teamRepository.existsByName(name)) {
                    errors.add("Line " + lineNumber + ": Team '" + name + "' exists");
                    continue;
                }

                // Reuse logic
                String country = columns.length > 1 ? columns[1].trim() : "";
                String affiliation = columns.length > 2 ? columns[2].trim() : "";
                String ipAddress = columns.length > 3 ? columns[3].trim() : "";

                validTeams.add(buildTeamEntity(name, country, affiliation, ipAddress));
            }

            teamRepository.saveAll(validTeams);

        } catch (Exception e) {
            log.error("CSV Import failed", e);
            throw new RuntimeException("Failed to process CSV: " + e.getMessage());
        }

        return Map.of(
                "success", true,
                "imported_count", validTeams.size(),
                "errors", errors);
    }

    /**
     * Get all teams mapped to DTO
     */
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findByRole("TEAM").stream()
                .map(this::mapToResponse)
                .toList(); // Java 16+
    }

    /**
     * Update team
     */
    @Transactional
    public TeamResponse updateTeam(Integer id, UpdateTeamRequest request) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // Validate unique name if name is changed
        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (!newName.equals(team.getName()) && !newName.isEmpty()) {
                if (teamRepository.existsByName(newName)) {
                    throw new IllegalArgumentException("Team name already exists!");
                }
                team.setName(newName);
            }
        }

        if (request.getCountry() != null)
            team.setCountry(request.getCountry().trim());
        if (request.getAffiliation() != null)
            team.setAffiliation(request.getAffiliation().trim());
        if (request.getIpAddress() != null)
            team.setIpAddress(request.getIpAddress().trim());

        TeamEntity savedTeam = teamRepository.save(team);
        return mapToResponse(savedTeam);
    }

    public void deleteTeam(Integer id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("Team not found");
        }
        teamRepository.deleteById(id);
    }

    // === Helper Methods (DRY - Don't Repeat Yourself) ===

    private TeamEntity buildTeamEntity(String name, String country, String affiliation, String ipAddress) {
        String username = name.toLowerCase().replaceAll("\\s+", "_");
        String defaultPassword = username + "123";

        return TeamEntity.builder()
                .name(name)
                .username(username)
                .password(passwordEncoder.encode(defaultPassword))
                .role("TEAM")
                .country(country)
                .affiliation(affiliation)
                .ipAddress(ipAddress)
                .build();
    }

    private TeamResponse mapToResponse(TeamEntity team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .country(team.getCountry())
                .affiliation(team.getAffiliation())
                .ipAddress(team.getIpAddress())
                .build();
    }
}