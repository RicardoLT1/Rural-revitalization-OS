create table if not exists weekly_report(
  id bigint primary key,
  village_id bigint not null,
  week_start date not null,
  week_end date not null,
  title varchar(160) not null,
  summary text not null,
  highlights text,
  risks text,
  next_week_plan text,
  author_id varchar(64),
  author_name varchar(128),
  status varchar(32) not null default 'PUBLISHED',
  created_at timestamp default current_timestamp,
  updated_at timestamp default current_timestamp on update current_timestamp,
  deleted tinyint default 0,
  index idx_weekly_report_week(week_start, deleted)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
