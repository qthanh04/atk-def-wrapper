package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Checker Proxy Controller - Quản lý Checker Scripts & Service Statuses
 * 
 * Checker = Python script kiểm tra service có hoạt động đúng không
 * Service Status = Kết quả check mỗi tick
 * 
 * Base URL: /api/proxy/checkers và /api/proxy/checker
 * Target: Python Server /checkers/* và /checker/*
 */
@RestController
@RequestMapping("/api/proxy")
@Tag(name = "Checker Proxy", description = "🔍 Checker Management - Service health checks")
public class CheckerProxyController {

    private final PythonProxyService pythonProxyService;

    public CheckerProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    // ======================== CHECKER CRUD (/checkers) ========================

    /**
     * GET /api/proxy/checkers - List checkers
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/checkers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listCheckers(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        String endpoint = String.format("/checkers?skip=%d&limit=%d", skip, limit);
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/checkers/{checkerId} - Chi tiết checker
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/checkers/{checkerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getChecker(@PathVariable String checkerId) {
        Object result = pythonProxyService.proxyGet("/checkers/" + checkerId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * PATCH /api/proxy/checkers/{checkerId} - Cập nhật checker info
     * Roles: ADMIN only
     */
    @PatchMapping("/checkers/{checkerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateChecker(
            @PathVariable String checkerId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = pythonProxyService.proxyPatch("/checkers/" + checkerId, request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/checkers/{checkerId} - Xóa checker
     * Roles: ADMIN only
     */
    @DeleteMapping("/checkers/{checkerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteChecker(@PathVariable String checkerId) {
        Object result = pythonProxyService.proxyDelete("/checkers/" + checkerId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/proxy/checkers/{checkerId}/validate - Validate checker syntax
     * Roles: ADMIN only
     */
    @PostMapping("/checkers/{checkerId}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateChecker(@PathVariable String checkerId) {
        Map<String, Object> result = pythonProxyService.proxyPost(
                "/checkers/" + checkerId + "/validate", null, Map.class);
        return ResponseEntity.ok(result);
    }

    // ======================== SERVICE STATUS (/checker) ========================

    /**
     * GET /api/proxy/checker/statuses - List service check statuses
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/checker/statuses")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listCheckerStatuses(
            @RequestParam(required = false) String gameId,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String tickId,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {

        StringBuilder endpoint = new StringBuilder("/checker/statuses?");
        endpoint.append(String.format("skip=%d&limit=%d", skip, limit));

        if (gameId != null) {
            endpoint.append("&game_id=").append(gameId);
        }
        if (teamId != null) {
            endpoint.append("&team_id=").append(teamId);
        }
        if (tickId != null) {
            endpoint.append("&tick_id=").append(tickId);
        }

        Object result = pythonProxyService.proxyGet(endpoint.toString(), Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/checker/statuses/{statusId} - Chi tiết status
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/checker/statuses/{statusId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getCheckerStatus(@PathVariable String statusId) {
        Object result = pythonProxyService.proxyGet("/checker/statuses/" + statusId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/checker/statuses/{statusId} - Xóa status record
     * Roles: ADMIN only
     */
    @DeleteMapping("/checker/statuses/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCheckerStatus(@PathVariable String statusId) {
        Object result = pythonProxyService.proxyDelete("/checker/statuses/" + statusId, Map.class);
        return ResponseEntity.ok(result);
    }
}
