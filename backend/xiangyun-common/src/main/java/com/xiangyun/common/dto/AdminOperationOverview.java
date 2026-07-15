package com.xiangyun.common.dto;

import java.util.List;

public record AdminOperationOverview(
        String villageId,
        String villageName,
        int resourceCount,
        int investmentReadyCount,
        int pendingApprovalCount,
        int overdueApprovalCount,
        int resourcesUpdatedToday,
        int readyResourcesUpdatedToday,
        int pendingCreatedToday,
        int workflowsResolvedToday,
        List<ResourceCategoryCount> resourceDistribution
) {
    public record ResourceCategoryCount(String category, int count) {
    }
}
