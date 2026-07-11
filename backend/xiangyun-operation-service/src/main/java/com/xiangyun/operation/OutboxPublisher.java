package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.event.EventBusNames;
import com.xiangyun.common.event.WorkflowChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(OutboxCreatedEvent event) {
        publishPending();
    }

    @Scheduled(initialDelayString = "${xiangyun.outbox.initial-delay-ms:10000}", fixedDelayString = "${xiangyun.outbox.scan-delay-ms:5000}")
    public void scheduledPublish() {
        jdbcTemplate.update("""
                update msg_outbox set status='FAILED', last_error='publish timeout', next_retry_at=now()
                where status='SENDING' and updated_at < date_sub(now(), interval 2 minute)
                """);
        publishPending();
    }

    public void publishPending() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select id,event_id as eventId,payload_json as payloadJson
                from msg_outbox
                where status in ('PENDING','FAILED') and retry_count < 8
                  and (next_retry_at is null or next_retry_at <= now())
                order by created_at asc limit 20
                """);
        rows.forEach(this::publishOne);
    }

    private void publishOne(Map<String, Object> row) {
        long id = ((Number) row.get("id")).longValue();
        String eventId = String.valueOf(row.get("eventId"));
        int claimed = jdbcTemplate.update("""
                update msg_outbox set status='SENDING', retry_count=retry_count+1, last_error=null
                where id=? and status in ('PENDING','FAILED')
                """, id);
        if (claimed == 0) {
            return;
        }
        try {
            WorkflowChangedEvent event = objectMapper.readValue(String.valueOf(row.get("payloadJson")), WorkflowChangedEvent.class);
            CorrelationData correlation = new CorrelationData(eventId);
            rabbitTemplate.convertAndSend(EventBusNames.EXCHANGE, EventBusNames.WORKFLOW_ROUTING_KEY, event, correlation);
            correlation.getFuture().whenComplete((confirm, failure) -> {
                if (failure == null && confirm != null && confirm.isAck() && correlation.getReturned() == null) {
                    jdbcTemplate.update("update msg_outbox set status='SENT', sent_at=now(), next_retry_at=null where id=? and status='SENDING'", id);
                } else {
                    String reason = failure != null ? failure.getMessage()
                            : correlation.getReturned() != null ? "message returned as unroutable"
                            : confirm == null ? "missing publisher confirm" : confirm.getReason();
                    markFailed(id, reason);
                }
            });
        } catch (Exception ex) {
            markFailed(id, ex.getMessage());
        }
    }

    private void markFailed(long id, String error) {
        String safeError = error == null ? "publish failed" : error.substring(0, Math.min(error.length(), 500));
        jdbcTemplate.update("""
                update msg_outbox set status='FAILED', last_error=?, next_retry_at=date_add(now(), interval 30 second)
                where id=? and status='SENDING'
                """, safeError, id);
        log.warn("Outbox publish failed: id={}, error={}", id, safeError);
    }
}
