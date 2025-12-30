package com.tool.atkdefbackend.service;

import com.tool.atkdefbackend.model.response.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class FileUploadService {

    @Value("${upload.checker-path:/data/checkers}")
    private String checkerPath;

    @Value("${upload.vulnbox-path:/data/vulnbox}")
    private String vulnboxPath;

    public UploadResponse uploadChecker(MultipartFile file, Integer challengeId) throws IOException {
        return processUpload(file, challengeId, checkerPath, ".py", "checker", "Checker uploaded successfully");
    }

    public UploadResponse uploadVulnBox(MultipartFile file, Integer challengeId) throws IOException {
        return processUpload(file, challengeId, vulnboxPath, ".zip", "vulnbox", "VulnBox uploaded successfully. Build job triggered.");
    }

    /**
     * Logic chung để xử lý upload file
     */
    private UploadResponse processUpload(MultipartFile file, Integer challengeId, String uploadPathStr,
                                         String extension, String prefix, String successMessage) throws IOException {
        // 1. Validate cơ bản
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        validateFileExtension(file, extension);

        // 2. Tạo tên file an toàn
        String safeOriginalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String finalFileName = String.format("%s_%d_%s", prefix, challengeId, safeOriginalName);

        // 3. Chuẩn bị đường dẫn
        Path uploadDir = Paths.get(uploadPathStr);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path targetPath = uploadDir.resolve(finalFileName);

        // 4. Kiểm tra tồn tại (Có thể thay đổi logic này nếu muốn cho phép ghi đè)
        if (Files.exists(targetPath)) {
            throw new FileAlreadyExistsException("File already exists: " + finalFileName);
        }

        // 5. Lưu file
        Files.copy(file.getInputStream(), targetPath);

        // 6. Trả về kết quả
        return UploadResponse.builder()
                .success(true)
                .message(successMessage)
                .fileName(finalFileName)
                .filePath(targetPath.toAbsolutePath().toString()) // Thống nhất dùng AbsolutePath
                .build();
    }

    private void validateFileExtension(MultipartFile file, String extension) {
        String originalName = Objects.requireNonNull(file.getOriginalFilename());
        if (!originalName.toLowerCase().endsWith(extension)) { // Thêm toLowerCase để tránh lỗi với .ZIP hay .PY
            throw new IllegalArgumentException("Invalid file type. Expected a " + extension + " file.");
        }
    }
}