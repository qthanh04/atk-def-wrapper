package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import com.tool.atkdefbackend.service.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Submission Proxy Controller - Proxy Flag Submission APIs
 * 
 * ĐÂY LÀ API QUAN TRỌNG NHẤT CHO GAMEPLAY!
 * Teams sẽ dùng API này để submit flags đã capture
 * 
 * Base URL: /api/proxy/submissions
 * Target: Python Server /submissions/*
 */
@RestController
@RequestMapping("/api/proxy/submissions")
@Tag(name = "Submission Proxy", description = "🚩 Flag Submission - CORE GAMEPLAY API for capturing flags")
public class SubmissionProxyController {

    private final PythonProxyService pythonProxyService;

    public SubmissionProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * POST /api/proxy/submissions - Submit flag (CORE GAMEPLAY)
     * 
     * Request Body:
     * {
     * "game_id": "uuid",
     * "team_id": "team_identifier",
     * "flag": "FLAG{...}"
     * }
     * 
     * Response:
     * {
     * "status": "ACCEPTED|REJECTED|DUPLICATE|OWN_FLAG",
     * "points": 100,
     * "message": "Flag accepted!"
     * }
     * 
     * Roles: TEAM - Team có thể submit flag cho chính mình
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM')")
    public ResponseEntity<?> submitFlag(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // Nếu là TEAM, có thể inject team_id tự động từ authentication
        // (Tùy theo yêu cầu bảo mật, có thể override team_id)

        Map<String, Object> result = pythonProxyService.proxyPost("/submissions", request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/submissions - List submissions
     * Query params: game_id, team_id, status, skip, limit
     * 
     * Roles: ADMIN, TEACHER - Xem tất cả
     * TEAM - Chỉ xem submissions của mình
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM')")
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

        // Nếu user là TEAM, chỉ cho xem submissions của chính mình
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEAM"))) {
            // Override teamId với ID của team hiện tại (id chính là team id trong DB)
            endpoint.append("&team_id=").append(userDetails.getId().toString());
        } else if (teamId != null) {
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
     * Roles: ADMIN, TEACHER, hoặc TEAM (chỉ xem của mình)
     */
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM')")
    public ResponseEntity<?> getSubmission(@PathVariable String submissionId) {
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
