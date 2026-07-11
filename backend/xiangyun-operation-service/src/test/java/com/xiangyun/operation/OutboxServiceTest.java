package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.WorkflowChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxServiceTest {
    @Test
    void enqueuePersistsEventAndPublishesWakeup() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        OutboxService service = new OutboxService(jdbcTemplate, new ObjectMapper(), publisher);
        WorkflowChangedEvent event = new WorkflowChangedEvent(
                "evt-1", "workflow.status.changed", "workflow", "201", "APPROVED",
                "2026-07-11T10:00:00Z", "trace-1", Map.of("title", "合作申请"));

        service.enqueue(event);

        verify(jdbcTemplate).update(anyString(), any(Object[].class));
        verify(publisher).publishEvent(new OutboxCreatedEvent("evt-1"));
    }
}
