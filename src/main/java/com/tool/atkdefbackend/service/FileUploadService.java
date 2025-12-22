package com.tool.atkdefbackend.service;

import ch.qos.logback.core.util.StringUtil;
import com.tool.atkdefbackend.model.response.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${upload.checker-path:/data/checkers}")
    private String checkerPath;

    @Value("${upload.vulnbox-path:/data/vulnbox}")
    private String vulnboxPath;

    /**
     * Upload checker script (.py file) for a challenge
     * 
     * @param file        Python checker script
     * @param challengeId Challenge ID
     * @return Map containing upload result
     */
    public UploadResponse uploadChecker(MultipartFile file, Integer challengeId) throws IOException {
        // Validate file extension
        validateFileExtension(file, ".py");

        String safeFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String finalFileName = "checker_" + challengeId + "_" + safeFileName;

        Path uploadDir = Paths.get(checkerPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path targetPath = uploadDir.resolve(finalFileName);
        if (Files.exists(targetPath)) {
            throw new FileAlreadyExistsException("Checker file already exists: " + finalFileName);
        }

        Files.copy(file.getInputStream(), targetPath);

        return UploadResponse.builder()
                .success(true)
                .message("Checker uploaded successfully")
                .fileName(finalFileName)
                .filePath(targetPath.toAbsolutePath().toString())
                .build();
    }

    /**
     * Upload VulnBox source (.zip file) for a challenge
     * Triggers Docker build job
     * 
     * @param file        VulnBox zip file
     * @param challengeId Challenge ID
     * @return Map containing job info
     */
    public UploadResponse uploadVulnBox(MultipartFile file, Integer challengeId) throws IOException {
        // Validate file extension
        validateFileExtension(file, ".zip");

        String safeOriginalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String finalFileName = "vulnbox_" + challengeId + "_" + safeOriginalName;

        //xac dinh duong dan
        Path uploadDir = Paths.get(vulnboxPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path targetPath = uploadDir.resolve(finalFileName);

        if (Files.exists(targetPath)) {
            throw new FileAlreadyExistsException("VulnBox file already exists: " + finalFileName);
        }

        Files.copy(file.getInputStream(), targetPath);

        return UploadResponse.builder()
                .success(true)
                .message("VulnBox uploaded successfully. Build job triggered.")
                .fileName(finalFileName)
                .filePath(targetPath.toString())
                .build();
    }

    private void validateFileExtension(MultipartFile file, String extension) {
        String originalName = file.getOriginalFilename();
        if (!originalName.endsWith(extension)) {
            throw new IllegalArgumentException("Invalid file type. Expected a " + extension + " file.");
        }
    }

    private Path saveFile(MultipartFile file, String directoryPath, String fileName) throws IOException {
        Path uploadDir = Paths.get(directoryPath);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path targetPath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath;
    }
}
