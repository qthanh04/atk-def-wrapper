package com.tool.atkdefbackend.controller.proxy;

import com.tool.atkdefbackend.service.PythonProxyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Vulnbox Proxy Controller - Quản lý Vulnerable Boxes
 * 
 * Vulnbox = Docker image chứa services có lỗ hổng
 * Mỗi team sẽ được deploy một vulnbox riêng
 * 
 * Base URL: /api/proxy/vulnboxes
 * Target: Python Server /vulnboxes/*
 */
@RestController
@RequestMapping("/api/proxy/vulnboxes")
@Tag(name = "Vulnbox Proxy", description = "📦 VulnBox Management - Vulnerable service containers")
public class VulnboxProxyController {

    private final PythonProxyService pythonProxyService;

    public VulnboxProxyController(PythonProxyService pythonProxyService) {
        this.pythonProxyService = pythonProxyService;
    }

    /**
     * GET /api/proxy/vulnboxes - List vulnboxes
     * Roles: ADMIN, TEACHER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> listVulnboxes(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "50") int limit) {
        String endpoint = String.format("/vulnboxes?skip=%d&limit=%d", skip, limit);
        Object result = pythonProxyService.proxyGet(endpoint, Object.class);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/proxy/vulnboxes/{vulnboxId} - Chi tiết vulnbox
     * Roles: ADMIN, TEACHER
     */
    @GetMapping("/{vulnboxId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> getVulnbox(@PathVariable String vulnboxId) {
        Object result = pythonProxyService.proxyGet("/vulnboxes/" + vulnboxId, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * PATCH /api/proxy/vulnboxes/{vulnboxId} - Cập nhật vulnbox info
     * Roles: ADMIN only
     */
    @PatchMapping("/{vulnboxId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVulnbox(
            @PathVariable String vulnboxId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = pythonProxyService.proxyPatch("/vulnboxes/" + vulnboxId, request, Map.class);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/proxy/vulnboxes/{vulnboxId} - Xóa vulnbox
     * Roles: ADMIN only
     */
    @DeleteMapping("/{vulnboxId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteVulnbox(@PathVariable String vulnboxId) {
        Object result = pythonProxyService.proxyDelete("/vulnboxes/" + vulnboxId, Map.class);
        return ResponseEntity.ok(result);
    }
}
