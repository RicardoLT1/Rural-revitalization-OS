package com.xiangyun.operation;

import java.util.List;

public record ResourceBatchResponse(
        String action,
        int requested,
        int succeeded,
        int failed,
        List<ResourceBatchItemResult> items
) {
}
