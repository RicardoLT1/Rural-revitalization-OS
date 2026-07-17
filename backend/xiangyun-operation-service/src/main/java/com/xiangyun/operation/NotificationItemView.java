package com.xiangyun.operation;

import java.time.LocalDateTime;

public record NotificationItemView(
        String id,
        String type,
        String title,
        String content,
        String targetPath,
        boolean read,
        LocalDateTime createdAt) {
}
