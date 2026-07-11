package com.xiangyun.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.WorkflowChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public OutboxService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    public void enqueue(WorkflowChangedEvent event) {
        try {
            jdbcTemplate.update("""
                    insert into msg_outbox(event_id,event_type,aggregate_type,aggregate_id,payload_json,status)
                    values(?,?,?,?,?,'PENDING')
                    """, event.eventId(), event.eventType(), event.aggregateType(), event.aggregateId(), objectMapper.writeValueAsString(event));
            eventPublisher.publishEvent(new OutboxCreatedEvent(event.eventId()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize workflow event", ex);
        }
    }
}
