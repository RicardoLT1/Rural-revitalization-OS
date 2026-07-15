package com.xiangyun.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.WorkflowChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowEventConsumerTest {
    private final WorkflowChangedEvent event = new WorkflowChangedEvent(
            "evt-1", "workflow.status.changed", "workflow", "201", "APPROVED",
            "2026-07-11T10:00:00Z", "trace-1", Map.of("title", "合作申请"));

    @Test
    void consumeAggregatesAndInvalidatesDashboardCache() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(redisTemplate.keys("analysis:dashboard:v2:*")).thenReturn(Set.of("analysis:dashboard:v2:1:7"));
        WorkflowEventConsumer consumer = new WorkflowEventConsumer(jdbcTemplate, redisTemplate, new ObjectMapper());

        consumer.consume(event);

        verify(redisTemplate).delete(Set.of("analysis:dashboard:v2:1:7"));
    }

    @Test
    void duplicateEventDoesNotAggregateAgain() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(0);
        WorkflowEventConsumer consumer = new WorkflowEventConsumer(jdbcTemplate, redisTemplate, new ObjectMapper());

        consumer.consume(event);

        verify(redisTemplate, never()).keys(anyString());
        verify(redisTemplate, never()).delete(any(Set.class));
    }
}
