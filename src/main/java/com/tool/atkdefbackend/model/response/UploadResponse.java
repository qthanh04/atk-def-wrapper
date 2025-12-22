package com.tool.atkdefbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UploadResponse {
    private boolean success;
    private String message;
    private String fileName;
    private String filePath;
}
