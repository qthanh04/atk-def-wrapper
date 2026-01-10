package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.entity.TeamEntity;
import com.tool.atkdefbackend.model.request.CreateTeamRequest;
import com.tool.atkdefbackend.model.request.UpdateTeamRequest;
import com.tool.atkdefbackend.model.response.TeamResponse;
import com.tool.atkdefbackend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * POST /api/teams - Create a single team
     * Request: { "name": "TeamHust", "country": "VN", "affiliation": "HUST",
     * "ip_address": "10.0.0.1" }
     * Response: { "success": true, "id": 1, "name": "TeamHust" }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamEntity team = teamService.createTeam(request);
        return ResponseEntity.status(201).body(team);
    }

    /**
     * POST /api/teams/bulk - Import multiple teams from CSV
     * Request: MultipartFile (CSV file)
     * Response: { "success": true, "imported_count": 50 }
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> importTeams(@RequestParam("file") MultipartFile file) {
        Map<String, Object> team = teamService.importTeamsFromCsv(file);
        return ResponseEntity.status(201).body(team);
    }

    /**
     * GET /api/teams - List all teams (Public for Dropdown/List)
     * Response: [{ "id": 10, "name": "Team1" }]
     */
    @GetMapping
    public ResponseEntity<?> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * PUT /api/teams/{id} - Update team
     * Request: { "name": "NewName", "ip_address": "..." }
     * Response: { "id": 10, "updated": true }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateTeam(@PathVariable Integer id,
            @Valid @RequestBody UpdateTeamRequest request) {
        TeamResponse team = teamService.updateTeam(id, request);
        return ResponseEntity.ok(team);
    }

    /**
     * DELETE /api/teams/{id} - Delete team
     * Response: { "message": "Deleted" }
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> deleteTeam(@PathVariable Integer id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(Map.of("message", "Team deleted successfully"));
    }
}
