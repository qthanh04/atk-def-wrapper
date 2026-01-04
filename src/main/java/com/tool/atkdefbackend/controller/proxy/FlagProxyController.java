package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Flag Proxy Controller - Quản lý Flags (Admin/Debug purpose)
 * 
 * Flags là READ-ONLY cho ADMIN/TEACHER để theo dõi game
 * Teams KHÔNG được xem flags (chống gian lận)
 * 
 * Base URL: /api/proxy/flags
 * Target: Python Server /flags/*
 */
@RestController
@RequestMapping("/api/proxy/flags")
@Tag(name = "Flag Proxy", description = "🏴 Flag Management - Admin monitoring (anti-cheat)")
public class FlagProxyController {

    private final PythonProxyService pythonProxyService;

    public FlagProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * GET /api/proxy/flags - List flags
     * Query params: game_id (required), team_id, tick_id, is_stolen, skip, limit
     * Roles: ADMIN, TEACHER only
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listFlags(
            @RequestParam String gameId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String tickId,
            @RequestParam(required = false) Boolean isStolen,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {

        StringBuilder endpoint = new StringBuilder("/flags?");
        endpoint.append(String.format("game_id=%s&skip=%d&limit=%d", gameId, skip, limit));

        if (teamId != null) {
            endpoint.append("&team_id=").append(teamId);
        }
        if (tickId != null) {
            endpoint.append("&tick_id=").append(tickId);
        }
        if (isStolen != null) {
            endpoint.append("&is_stolen=").append(isStolen);
        }

        Object result = pythonProxyService.proxyGet(endpoint.toString(), Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/flags/stats - Thống kê flags
     * Roles: ADMIN, TEACHER only
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getFlagStats(
            @RequestParam String gameId,
            @RequestParam(required = false) String teamId) {

        StringBuilder endpoint = new StringBuilder("/flags/stats?game_id=").append(gameId);
        if (teamId != null) {
            endpoint.append("&team_id=").append(teamId);
        }

        Object result = pythonProxyService.proxyGet(endpoint.toString(), Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/flags/{flagId} - Chi tiết flag
     * Roles: ADMIN, TEACHER only
     */
    @GetMapping("/{flagId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getFlag(@PathVariable String flagId) {
        Object result = pythonProxyService.proxyGet("/flags/" + flagId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/flags/by-value/{flagValue} - Tìm flag theo giá trị
     * Roles: ADMIN, TEACHER only
     * 
     * Dùng để debug khi team báo cáo flag không valid
     */
    @GetMapping("/by-value/{flagValue}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getFlagByValue(@PathVariable String flagValue) {
        Object result = pythonProxyService.proxyGet("/flags/by-value/" + flagValue, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/flags/tick/{tickId} - Flags của một tick
     * Roles: ADMIN, TEACHER only
     */
    @GetMapping("/tick/{tickId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getTickFlags(
            @PathVariable String tickId,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        String endpoint = String.format("/flags/tick/%s?skip=%d&limit=%d", tickId, skip, limit);
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/flags/team/{teamId}/tick/{tickId} - Flags của team trong tick
     * Roles: ADMIN, TEACHER only
     */
    @GetMapping("/team/{teamId}/tick/{tickId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getTeamTickFlags(
            @PathVariable String teamId,
            @PathVariable String tickId,
            @RequestParam String gameId) {
        String endpoint = String.format("/flags/team/%s/tick/%s?game_id=%s", teamId, tickId, gameId);
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }
}
