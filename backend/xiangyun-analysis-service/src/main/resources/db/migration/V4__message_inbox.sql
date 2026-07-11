create table if not exists msg_inbox(
  id bigint primary key auto_increment,
  event_id varchar(64) not null,
  event_type varchar(96) not null,
  aggregate_type varchar(64) not null,
  aggregate_id varchar(64) not null,
  payload_json text not null,
  status varchar(24) not null default 'PROCESSED',
  processed_at timestamp not null default current_timestamp,
  created_at timestamp not null default current_timestamp,
  unique key uk_msg_inbox_event(event_id),
  index idx_msg_inbox_processed(processed_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists analysis_event_aggregate(
  event_type varchar(96) primary key,
  event_count bigint not null default 0,
  last_event_id varchar(64),
  last_event_at timestamp null,
  updated_at timestamp not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
