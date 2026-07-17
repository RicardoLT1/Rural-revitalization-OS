package com.xiangyun.operation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SystemSettingsView(
        String villageId,
        String platformName,
        String villageName,
        BigDecimal mapCenterLat,
        BigDecimal mapCenterLng,
        int approvalTimeoutHours,
        int weeklyReportDay,
        boolean workflowNotificationEnabled,
        boolean riskNotificationEnabled,
        String contactPhone,
        String updatedBy,
        LocalDateTime updatedAt,
        String systemVersion) {
}
