package com.xiangyun.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.EventBusNames;
import com.xiangyun.common.event.WorkflowChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class WorkflowEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(WorkflowEventConsumer.class);
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public WorkflowEventConsumer(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = EventBusNames.WORKFLOW_QUEUE)
    @Transactional
    public void consume(WorkflowChangedEvent event) throws JsonProcessingException {
        int inserted = jdbcTemplate.update("""
                insert ignore into msg_inbox(event_id,event_type,aggregate_type,aggregate_id,payload_json,status)
                values(?,?,?,?,?,'PROCESSED')
                """, event.eventId(), event.eventType(), event.aggregateType(), event.aggregateId(), objectMapper.writeValueAsString(event));
        if (inserted == 0) {
            log.info("Duplicate workflow event ignored: {}", event.eventId());
            return;
        }
        jdbcTemplate.update("""
                insert into analysis_event_aggregate(event_type,event_count,last_event_id,last_event_at)
                values(?,1,?,now())
                on duplicate key update event_count=event_count+1,last_event_id=values(last_event_id),last_event_at=now()
                """, event.eventType(), event.eventId());
        Set<String> keys = redisTemplate.keys("analysis:dashboard:v2:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("Workflow event processed: eventId={}, aggregateId={}, status={}", event.eventId(), event.aggregateId(), event.status());
    }
}
