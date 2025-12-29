package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.request.CreateTeamRequest;
import com.tool.atkdefbackend.model.request.UpdateTeamRequest;
import com.tool.atkdefbackend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return teamService.createTeam(request);
    }

    /**
     * POST /api/teams/bulk - Import multiple teams from CSV
     * Request: MultipartFile (CSV file)
     * Response: { "success": true, "imported_count": 50 }
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> importTeams(@RequestParam("file") MultipartFile file) {
        return teamService.importTeamsFromCsv(file);
    }

    /**
     * GET /api/teams - List all teams (Public for Dropdown/List)
     * Response: [{ "id": 10, "name": "Team1" }]
     */
    @GetMapping
    public ResponseEntity<?> getAllTeams() {
        return teamService.getAllTeams();
    }

    /**
     * PUT /api/teams/{id} - Update team
     * Request: { "name": "NewName", "ip_address": "..." }
     * Response: { "id": 10, "updated": true }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> updateTeam(@PathVariable Integer id,
            @RequestBody UpdateTeamRequest request) {
        return teamService.updateTeam(id, request);
    }

    /**
     * DELETE /api/teams/{id} - Delete team
     * Response: { "message": "Deleted" }
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> deleteTeam(@PathVariable Integer id) {
        return teamService.deleteTeam(id);
    }
}
