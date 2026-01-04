package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Scoreboard Proxy Controller - Public APIs cho bảng xếp hạng
 * 
 * Scoreboard là PUBLIC - Không cần authentication
 * Ai cũng có thể xem bảng xếp hạng real-time
 * 
 * Base URL: /api/proxy/scoreboard
 * Target: Python Server /scoreboard/*
 */
@RestController
@RequestMapping("/api/proxy/scoreboard")
@Tag(name = "Scoreboard Proxy", description = "📊 Public Scoreboard - Real-time team rankings")
public class ScoreboardProxyController {

    private final PythonProxyService pythonProxyService;

    public ScoreboardProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * GET /api/proxy/scoreboard - List tất cả scoreboards
     * PUBLIC - Không cần auth
     */
    @GetMapping
    public ResponseEntity<?> listScoreboards(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        String endpoint = String.format("/scoreboard?skip=%d&limit=%d", skip, limit);
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/scoreboard/{gameId} - Scoreboard của một game cụ thể
     * PUBLIC - Không cần auth
     * 
     * Response:
     * {
     * "game_id": "uuid",
     * "game_name": "CTF 2024",
     * "current_tick": 15,
     * "entries": [
     * {
     * "team_id": "team1",
     * "attack_points": 800,
     * "defense_points": 700,
     * "sla_points": 500,
     * "total_points": 2000,
     * "rank": 1,
     * "flags_captured": 50,
     * "flags_lost": 10
     * }
     * ],
     * "last_updated": "2024-01-01T12:00:00Z"
     * }
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGameScoreboard(@PathVariable String gameId) {
        Object result = pythonProxyService.proxyGet("/scoreboard/" + gameId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/scoreboard/{gameId}/team/{teamId} - Điểm của một team cụ thể
     * PUBLIC - Không cần auth
     */
    @GetMapping("/{gameId}/team/{teamId}")
    public ResponseEntity<?> getTeamScore(
            @PathVariable String gameId,
            @PathVariable String teamId) {
        Object result = pythonProxyService.proxyGet("/scoreboard/" + gameId + "/team/" + teamId, Map.class);
        return ResponseEntity.ok(result);
    }
}
