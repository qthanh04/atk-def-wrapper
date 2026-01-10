package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import com.tool.atkdefbackend.service.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Submission Proxy Controller - Proxy Flag Submission APIs
 * 
 * THIS IS THE MOST IMPORTANT API FOR GAMEPLAY!
 * Teams will use this API to submit captured flags
 *
 * SECURITY: Team isolation enforced - teams can only submit for themselves
 * Based on NewTech.md Section 4: Security Enforcement for Team-Based Operations
 * 
 * Base URL: /api/proxy/submissions
 * Target: Python Server /submissions/*
 */
@RestController
@RequestMapping("/api/proxy/submissions")
@Tag(name = "Submission Proxy", description = "🚩 Flag Submission - CORE GAMEPLAY API for capturing flags")
public class SubmissionProxyController {

    private static final Logger log = LoggerFactory.getLogger(SubmissionProxyController.class);
    private final PythonProxyService pythonProxyService;

    public SubmissionProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * POST /api/proxy/submissions - Submit flag (CORE GAMEPLAY)
     * 
     * SECURITY: Enforces team isolation
     * - TEAM/STUDENT roles: team_id is forced from authentication token
     * - ADMIN/TEACHER roles: can submit for any team
     *
     * Request Body:
     * {
     *   "game_id": "uuid",
     *   "team_id": "team_identifier",  // Overridden for TEAM/STUDENT
     *   "flag": "FLAG{...}"
     * }
     * 
     * Response:
     * {
     *   "status": "ACCEPTED|REJECTED|DUPLICATE|OWN_FLAG",
     *   "points": 100,
     *   "message": "Flag accepted!"
     * }
     *
     * Roles: ADMIN, TEACHER, TEAM, STUDENT
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM', 'STUDENT')")
    public ResponseEntity<?> submitFlag(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // SECURITY: Force team_id from authentication for TEAM/STUDENT users
        boolean isTeamUser = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEAM") ||
                              a.getAuthority().equals("ROLE_STUDENT"));

        if (isTeamUser) {
            String authenticatedTeamId = userDetails.getTeamId();

            if (authenticatedTeamId == null || authenticatedTeamId.equals("0")) {
                log.warn("SECURITY: User {} is not assigned to any team", userDetails.getUsername());
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "You are not assigned to any team",
                    "status", 403
                ));
            }

            // Check if user is trying to submit with different team_id
            if (request.containsKey("team_id") &&
                !authenticatedTeamId.equals(request.get("team_id").toString())) {
                log.warn("SECURITY: User {} attempted to submit flag as different team (auth: {}, request: {})",
                         userDetails.getUsername(), authenticatedTeamId, request.get("team_id"));
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "Cannot submit flags for other teams",
                    "status", 403
                ));
            }

            // Always use authenticated team ID for security
            request.put("team_id", authenticatedTeamId);
            log.info("Flag submission from team {} (user: {})", authenticatedTeamId, userDetails.getUsername());
        }

        Map<String, Object> result = pythonProxyService.proxyPost("/submissions", request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/submissions - List submissions
     * Query params: game_id, team_id, status, skip, limit
     * 
     * SECURITY: Enforces team isolation
     * - ADMIN/TEACHER: Can view all submissions
     * - TEAM/STUDENT: Can only view their own submissions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM', 'STUDENT')")
    public ResponseEntity<?> listSubmissions(
            @RequestParam(required = false) String gameId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        StringBuilder endpoint = new StringBuilder("/submissions?");
        endpoint.append(String.format("skip=%d&limit=%d", skip, limit));

        if (gameId != null) {
            endpoint.append("&game_id=").append(gameId);
        }

        // SECURITY: If user is TEAM/STUDENT, only show their submissions
        boolean isTeamUser = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEAM") ||
                              a.getAuthority().equals("ROLE_STUDENT"));

        if (isTeamUser) {
            // Override teamId with authenticated team ID
            String authenticatedTeamId = userDetails.getTeamId();
            if (authenticatedTeamId != null && !authenticatedTeamId.equals("0")) {
                endpoint.append("&team_id=").append(authenticatedTeamId);
                log.debug("Filtered submissions for team: {}", authenticatedTeamId);
            }
        } else if (teamId != null) {
            // ADMIN/TEACHER can filter by any team
            endpoint.append("&team_id=").append(teamId);
        }

        if (status != null) {
            endpoint.append("&status=").append(status);
        }

        Object result = pythonProxyService.proxyGet(endpoint.toString(), Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/submissions/{submissionId} - Chi tiết submission
     *
     * SECURITY: TEAM/STUDENT can only view their own submissions
     * (Note: This would require checking submission ownership at Python Core level)
     *
     * Roles: ADMIN, TEACHER, TEAM, STUDENT
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM', 'STUDENT')")
    public ResponseEntity<?> getSubmission(
            @PathVariable String submissionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // TODO: Add ownership check at Python Core level
        Object result = pythonProxyService.proxyGet("/submissions/" + submissionId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/submissions/{submissionId} - Xóa submission
     * Roles: ADMIN only
     */
    @DeleteMapping("/{submissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubmission(@PathVariable String submissionId) {
        Object result = pythonProxyService.proxyDelete("/submissions/" + submissionId, Map.class);
        return ResponseEntity.ok(result);
    }
}
