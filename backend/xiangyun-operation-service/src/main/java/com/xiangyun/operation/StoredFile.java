package com.xiangyun.operation;

public record StoredFile(
        String storageKey,
        String originalName,
        String contentType,
        long size,
        String sha256
) {
}
