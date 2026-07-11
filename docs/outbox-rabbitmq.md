# Outbox and RabbitMQ

Phase 4 delivers an event enhancement path for workflow status changes. Analysis keeps its existing Feign query and stale-cache fallback; RabbitMQ is not the only dashboard data source.

## Event Flow

```text
Operation approval transaction
  -> update workflow and todo
  -> insert approval record and operation log
  -> insert msg_outbox(PENDING)
  -> commit
  -> afterCommit publisher wake-up
  -> RabbitMQ publisher confirm
  -> msg_outbox(SENT)
  -> Analysis consumer
  -> msg_inbox(PROCESSED, unique event_id)
  -> increment analysis_event_aggregate
  -> invalidate dashboard cache
```

The scheduled Outbox scan runs as a fallback. Failed publishes are retried with a delay, abandoned `SENDING` records are recovered, and messages stop retrying after eight attempts for manual inspection.

## RabbitMQ Topology

- Exchange: `xiangyun.business.events`
- Routing key: `workflow.changed`
- Queue: `xiangyun.analysis.workflow-changed`
- Dead-letter exchange: `xiangyun.business.events.dlx`
- Dead-letter queue: `xiangyun.analysis.workflow-changed.dlq`

Local management UI: `http://127.0.0.1:15672`

Demo credentials are configured in `docker-compose.demo.yml`. Production credentials must come from environment variables or a secret manager.

## Database Ownership

Operation Flyway migration `V10__message_outbox.sql` owns `msg_outbox`.

Analysis Flyway migration `V4__message_inbox.sql` owns `msg_inbox` and `analysis_event_aggregate`.

`event_id` is unique in both Outbox and Inbox. A duplicate delivery is acknowledged without applying the aggregate or cache invalidation twice.

## Operations

Useful status queries:

```sql
select status, count(*) from msg_outbox group by status;
select event_type, count(*) from msg_inbox group by event_type;
select * from analysis_event_aggregate order by updated_at desc;
```

An Outbox row is complete only when its status is `SENT`. RabbitMQ availability must not block the local approval transaction; pending or failed events remain recoverable by the scanner.
