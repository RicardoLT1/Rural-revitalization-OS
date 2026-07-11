create table if not exists msg_outbox(
  id bigint primary key auto_increment,
  event_id varchar(64) not null,
  event_type varchar(96) not null,
  aggregate_type varchar(64) not null,
  aggregate_id varchar(64) not null,
  payload_json text not null,
  status varchar(24) not null default 'PENDING',
  retry_count int not null default 0,
  next_retry_at timestamp null,
  last_error varchar(512),
  created_at timestamp not null default current_timestamp,
  sent_at timestamp null,
  updated_at timestamp not null default current_timestamp on update current_timestamp,
  unique key uk_msg_outbox_event(event_id),
  index idx_msg_outbox_scan(status, next_retry_at, created_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
