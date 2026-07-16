package com.xiangyun.operation;

import java.time.LocalDateTime;

public record ResourceMaterialView(
        String id,
        String resourceId,
        String category,
        String title,
        String description,
        boolean cover,
        String originalName,
        String contentType,
        long fileSize,
        String sha256,
        String uploadedBy,
        String uploadedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public boolean image() {
        return contentType != null && contentType.startsWith("image/");
    }
}
