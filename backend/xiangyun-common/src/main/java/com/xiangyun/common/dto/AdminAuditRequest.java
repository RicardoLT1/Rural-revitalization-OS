package com.xiangyun.common.dto;

public record AdminAuditRequest(
        String traceId,
        String actorId,
        String actorName,
        String actorRole,
        String villageId,
        String module,
        String action,
        String targetType,
        String targetId,
        String requestMethod,
        String requestPath,
        String clientIp,
        String userAgent,
        String result,
        Integer httpStatus,
        String detail,
        String beforeData,
        String afterData
) {
}
