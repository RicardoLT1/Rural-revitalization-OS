package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;

public enum WorkflowStatus {
    DRAFT,
    PENDING,
    MATERIAL_REQUIRED,
    APPROVED,
    REJECTED;

    public boolean canTransitionTo(WorkflowStatus target) {
        return switch (this) {
            case DRAFT -> target == PENDING;
            case PENDING -> target == MATERIAL_REQUIRED || target == APPROVED || target == REJECTED;
            case MATERIAL_REQUIRED -> target == PENDING || target == APPROVED || target == REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }

    public static WorkflowStatus from(Object value) {
        try {
            return WorkflowStatus.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            throw new BusinessException(40001, "流程状态不合法");
        }
    }
}
