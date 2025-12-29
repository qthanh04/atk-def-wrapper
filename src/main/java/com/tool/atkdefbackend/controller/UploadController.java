package com.tool.atkdefbackend.controller;

import com.tool.atkdefbackend.model.response.UploadResponse;
import com.tool.atkdefbackend.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final FileUploadService fileUploadService;

    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload checker script (.py) for a challenge
     * POST /api/upload/checker
     */
    @PostMapping("/checker")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> uploadChecker(
            @RequestParam("file") MultipartFile file,
            @RequestParam("challengeId") Integer challengeId) {
        try {
            UploadResponse result = fileUploadService.uploadChecker(file, challengeId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    /**
     * Upload VulnBox source (.zip) for a challenge
     * POST /api/upload/vulnbox
     */
    @PostMapping("/vulnbox")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<?> uploadVulnBox(
            @RequestParam("file") MultipartFile file,
            @RequestParam("challengeId") Integer challengeId) {
        try {

            UploadResponse result = fileUploadService.uploadVulnBox(file, challengeId);
            return ResponseEntity.accepted().body(result); // 202 Accepted (async job)
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
}
