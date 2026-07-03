package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;

public enum WorkflowStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED;

    public boolean canTransitionTo(WorkflowStatus target) {
        return switch (this) {
            case DRAFT -> target == PENDING;
            case PENDING -> target == APPROVED || target == REJECTED;
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
