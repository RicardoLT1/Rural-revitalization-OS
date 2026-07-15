package com.xiangyun.operation;

public record AdminAuditEvent(
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
        int httpStatus,
        String detail
) {
}
