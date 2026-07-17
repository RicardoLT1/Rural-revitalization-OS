package com.xiangyun.operation;

import java.util.List;
import java.util.Map;

public record AdminSearchResponse(
        String query,
        List<AdminSearchItem> items,
        Map<String, Integer> counts,
        boolean partial
) {
}
