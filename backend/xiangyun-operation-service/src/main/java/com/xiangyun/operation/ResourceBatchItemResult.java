package com.xiangyun.operation;

public record ResourceBatchItemResult(
        String id,
        boolean success,
        String message,
        String status
) {
}
