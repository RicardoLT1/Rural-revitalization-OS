package com.xiangyun.analysis;

import java.util.Map;

public record DashboardResult(Map<String, Object> data, String cacheStatus, boolean stale) {

    public DashboardResult(Map<String, Object> data, String cacheStatus) {
        this(data, cacheStatus, false);
    }
}
