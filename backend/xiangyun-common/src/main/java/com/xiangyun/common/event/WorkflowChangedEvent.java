package com.xiangyun.common.event;

import java.util.Map;

public record WorkflowChangedEvent(
        String eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String status,
        String occurredAt,
        String traceId,
        Map<String, Object> payload
) {
}
