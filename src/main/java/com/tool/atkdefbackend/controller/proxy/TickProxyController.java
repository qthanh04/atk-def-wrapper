package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tick Proxy Controller - Quản lý Ticks (Game Turns)
 * 
 * Tick = một vòng trong game (60 giây mặc định)
 * Mỗi tick sẽ:
 * - Generate flags mới cho mỗi team
 * - Run checker để kiểm tra service availability
 * - Tính điểm
 * 
 * Base URL: /api/proxy/ticks
 * Target: Python Server /ticks/*
 */
@RestController
@RequestMapping("/api/proxy/ticks")
@Tag(name = "Tick Proxy", description = "⏱️ Tick/Round Management - Game timing and turns")
public class TickProxyController {

    private final PythonProxyService pythonProxyService;

    public TickProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * GET /api/proxy/ticks - List ticks
     * Roles: ADMIN, TEACHER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listTicks(
            @RequestParam String gameId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {

        StringBuilder endpoint = new StringBuilder("/ticks?");
        endpoint.append(String.format("game_id=%s&skip=%d&limit=%d", gameId, skip, limit));

        if (status != null) {
            endpoint.append("&status=").append(status);
        }

        Object result = pythonProxyService.proxyGet(endpoint.toString(), Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/ticks/current - Tick hiện tại
     * PUBLIC - Teams cần biết tick hiện tại để biết khi nào flag expire
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentTick(@RequestParam String gameId) {
        String endpoint = "/ticks/current?game_id=" + gameId;
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/ticks/latest - Tick mới nhất
     * PUBLIC - Hữu ích để frontend hiển thị
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestTick(@RequestParam String gameId) {
        String endpoint = "/ticks/latest?game_id=" + gameId;
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/ticks/{tickId} - Chi tiết tick
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/{tickId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getTick(@PathVariable String tickId) {
        Object result = pythonProxyService.proxyGet("/ticks/" + tickId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/ticks/number/{tickNumber} - Tick theo số thứ tự
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/number/{tickNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getTickByNumber(
            @PathVariable int tickNumber,
            @RequestParam String gameId) {
        String endpoint = String.format("/ticks/number/%d?game_id=%s", tickNumber, gameId);
        Object result = pythonProxyService.proxyGet(endpoint, Map.class);
        return ResponseEntity.ok(result);
    }
}
