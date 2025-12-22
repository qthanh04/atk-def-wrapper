package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.service.PythonProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final PythonProxyService pythonProxyService;

    public GameController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * Start the CTF game
     * POST /api/game/start
     * Proxies to Python: /internal/game/start
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> startGame() {
        Map<String, Object> result = pythonProxyService.startGame();
        return ResponseEntity.ok(result);
    }

    /**
     * Stop the CTF game
     * POST /api/game/stop
     * Proxies to Python: /internal/game/stop
     */
    @PostMapping("/stop")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> stopGame() {
        Map<String, Object> result = pythonProxyService.stopGame();
        return ResponseEntity.ok(result);
    }

    /**
     * Get current game status
     * GET /api/game/status
     * Proxies to Python: /internal/game/status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getGameStatus() {
        Object result = pythonProxyService.getGameStatus();
        return ResponseEntity.ok(result);
    }
}
