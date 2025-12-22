package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.service.PythonProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ScoreboardController {

    private final PythonProxyService pythonProxyService;

    public ScoreboardController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * Get scoreboard data
     * GET /api/scoreboard
     * Proxies to Python: /api/scoreboard
     * Public endpoint (no auth required)
     */
    @GetMapping("/scoreboard")
    public ResponseEntity<?> getScoreboard() {
        Object result = pythonProxyService.getScoreboard();
        return ResponseEntity.ok(result);
    }
}
