package com.xiangyun.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        int totalPages
) {
    public static int normalizePage(Integer page) {
        return page == null ? 1 : Math.max(1, page);
    }

    public static int normalizePageSize(Integer pageSize) {
        return pageSize == null ? 20 : Math.max(1, Math.min(100, pageSize));
    }

    public static <T> PageResponse<T> of(List<T> items, Integer page, Integer pageSize, long total) {
        int actualPage = normalizePage(page);
        int actualPageSize = normalizePageSize(pageSize);
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / actualPageSize);
        return new PageResponse<>(items == null ? List.of() : items, actualPage, actualPageSize, total, pages);
    }
}
