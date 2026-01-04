package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Game Proxy Controller - Proxy tất cả Game APIs từ Python Core Engine
 * Full CRUD cho Games + Game Teams + Game Control
 * 
 * Base URL: /api/proxy/games
 * Target: Python Server /games/*
 */
@RestController
@RequestMapping("/api/proxy/games")
@Tag(name = "Game Proxy", description = "🎮 Game Management - Create, Control, Manage Games & Teams")
public class GameProxyController {

    private final PythonProxyService pythonProxyService;

    public GameProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    // ======================== GAME CRUD ========================

    /**
     * POST /api/proxy/games - Tạo game mới
     * Roles: ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createGame(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = pythonProxyService.proxyPost("/games", request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/games - List tất cả games
     * Roles: ADMIN, TEACHER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listGames(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        String endpoint = String.format("/games?skip=%d&limit=%d", skip, limit);
        Object result = pythonProxyService.proxyGet(endpoint, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/games/{gameId} - Chi tiết game
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/{gameId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getGame(@PathVariable String gameId) {
        Object result = pythonProxyService.proxyGet("/games/" + gameId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * PATCH /api/proxy/games/{gameId} - Cập nhật game
     * Roles: ADMIN only
     */
    @PatchMapping("/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateGame(
            @PathVariable String gameId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = pythonProxyService.proxyPatch("/games/" + gameId, request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/games/{gameId} - Xóa game
     * Roles: ADMIN only
     */
    @DeleteMapping("/{gameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGame(@PathVariable String gameId) {
        Object result = pythonProxyService.proxyDelete("/games/" + gameId, Map.class);
        return ResponseEntity.ok(result);
    }

    // ======================== GAME CONTROL ========================

    /**
     * POST /api/proxy/games/{gameId}/start - Bắt đầu game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> startGame(@PathVariable String gameId) {
        Map<String, Object> result = pythonProxyService.proxyPost("/games/" + gameId + "/start", null, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/proxy/games/{gameId}/pause - Tạm dừng game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pauseGame(@PathVariable String gameId) {
        Map<String, Object> result = pythonProxyService.proxyPost("/games/" + gameId + "/pause", null, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/proxy/games/{gameId}/stop - Dừng game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/stop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> stopGame(@PathVariable String gameId) {
        Map<String, Object> result = pythonProxyService.proxyPost("/games/" + gameId + "/stop", null, Map.class);
        return ResponseEntity.ok(result);
    }

    // ======================== GAME TEAMS ========================

    /**
     * POST /api/proxy/games/{gameId}/teams - Thêm team vào game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addTeamToGame(
            @PathVariable String gameId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = pythonProxyService.proxyPost("/games/" + gameId + "/teams", request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/games/{gameId}/teams - List teams trong game
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/{gameId}/teams")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getGameTeams(@PathVariable String gameId) {
        Object result = pythonProxyService.proxyGet("/games/" + gameId + "/teams", Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/games/{gameId}/teams/{teamId} - Chi tiết team trong game
     * Roles: ADMIN, TEACHER, hoặc TEAM xem chính mình
     */
    @GetMapping("/{gameId}/teams/{teamId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'TEAM')")
    public ResponseEntity<?> getGameTeam(
            @PathVariable String gameId,
            @PathVariable String teamId) {
        Object result = pythonProxyService.proxyGet("/games/" + gameId + "/teams/" + teamId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/games/{gameId}/teams/{teamId} - Xóa team khỏi game
     * Roles: ADMIN only
     */
    @DeleteMapping("/{gameId}/teams/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeTeamFromGame(
            @PathVariable String gameId,
            @PathVariable String teamId) {
        Object result = pythonProxyService.proxyDelete("/games/" + gameId + "/teams/" + teamId, Map.class);
        return ResponseEntity.ok(result);
    }

    // ======================== ASSIGN VULNBOX & CHECKER ========================

    /**
     * POST /api/proxy/games/{gameId}/assign-vulnbox - Gán vulnbox cho game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/assign-vulnbox")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignVulnbox(
            @PathVariable String gameId,
            @RequestParam String vulnboxId) {
        String endpoint = String.format("/games/%s/assign-vulnbox?vulnbox_id=%s", gameId, vulnboxId);
        Map<String, Object> result = pythonProxyService.proxyPost(endpoint, null, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/proxy/games/{gameId}/assign-checker - Gán checker cho game
     * Roles: ADMIN only
     */
    @PostMapping("/{gameId}/assign-checker")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignChecker(
            @PathVariable String gameId,
            @RequestParam String checkerId) {
        String endpoint = String.format("/games/%s/assign-checker?checker_id=%s", gameId, checkerId);
        Map<String, Object> result = pythonProxyService.proxyPost(endpoint, null, Map.class);
        return ResponseEntity.ok(result);
    }
}
