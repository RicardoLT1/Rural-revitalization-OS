package com.xiangyun.operation;

public record AdminSearchItem(
        String id,
        String type,
        String title,
        String subtitle,
        String status,
        String updatedAt
) {
}
