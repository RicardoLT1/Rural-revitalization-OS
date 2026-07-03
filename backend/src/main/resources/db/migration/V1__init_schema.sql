CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NULL,
  openid VARCHAR(128) NULL,
  username VARCHAR(64) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_user_village (village_id),
  INDEX idx_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  remark VARCHAR(255) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_role_rel (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  INDEX idx_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS village (
  id BIGINT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  region VARCHAR(128) NULL,
  address VARCHAR(255) NULL,
  intro TEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  category VARCHAR(64) NOT NULL,
  lat DECIMAL(10, 6) NOT NULL,
  lng DECIMAL(10, 6) NOT NULL,
  address VARCHAR(255) NOT NULL,
  area DECIMAL(12, 2) NOT NULL DEFAULT 0,
  annual_estimate DECIMAL(12, 2) NOT NULL DEFAULT 0,
  investment_status VARCHAR(64) NOT NULL,
  intro TEXT NULL,
  owner VARCHAR(128) NULL,
  contact VARCHAR(64) NULL,
  related_projects VARCHAR(512) NULL,
  occupancy_rate INT NOT NULL DEFAULT 0,
  expected_roi INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_resource_village (village_id),
  INDEX idx_resource_category (category),
  INDEX idx_resource_investment_status (investment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource_tag (
  id BIGINT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  type VARCHAR(64) NOT NULL DEFAULT 'resource',
  sort_no INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_resource_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resource_tag_rel (
  resource_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (resource_id, tag_id),
  INDEX idx_resource_tag_rel_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workflow (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(64) NOT NULL,
  status VARCHAR(64) NOT NULL,
  current_node_id VARCHAR(64) NULL,
  blocker VARCHAR(255) NULL,
  applicant VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_workflow_village (village_id),
  INDEX idx_workflow_category (category),
  INDEX idx_workflow_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS workflow_node (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  node_key VARCHAR(64) NOT NULL,
  title VARCHAR(128) NOT NULL,
  status VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  assignee VARCHAR(64) NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_workflow_node_workflow (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS todo_item (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(64) NOT NULL,
  status VARCHAR(64) NOT NULL,
  due_date DATETIME NULL,
  assignee VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_todo_workflow (workflow_id),
  INDEX idx_todo_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS approval_record (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  node_id VARCHAR(64) NULL,
  title VARCHAR(160) NOT NULL,
  applicant VARCHAR(64) NULL,
  amount DECIMAL(12, 2) NULL,
  action VARCHAR(64) NULL,
  status VARCHAR(64) NOT NULL,
  remark VARCHAR(255) NULL,
  handled_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_approval_workflow (workflow_id),
  INDEX idx_approval_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS archive_record (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  archive_no VARCHAR(64) NOT NULL,
  archive_title VARCHAR(160) NOT NULL,
  archive_time DATETIME NULL,
  handler VARCHAR(64) NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_archive_no (archive_no),
  INDEX idx_archive_workflow (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report_snapshot (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  stat_date DATE NOT NULL,
  visitor_count INT NOT NULL DEFAULT 0,
  revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
  project_progress DECIMAL(5, 2) NOT NULL DEFAULT 0,
  risk_count INT NOT NULL DEFAULT 0,
  investment_conversion_rate DECIMAL(5, 2) NOT NULL DEFAULT 0,
  culture_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
  product_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
  service_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_report_snapshot_day (village_id, stat_date),
  INDEX idx_report_snapshot_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS forecast_result (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  forecast_date DATE NOT NULL,
  actual_value INT NULL,
  predict_value INT NOT NULL,
  upper_value INT NOT NULL,
  lower_value INT NOT NULL,
  risk_level VARCHAR(32) NOT NULL DEFAULT 'low',
  strategy VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_forecast_village_date (village_id, forecast_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS investment_match_record (
  id BIGINT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  investor VARCHAR(128) NOT NULL,
  score INT NOT NULL DEFAULT 0,
  reason VARCHAR(512) NULL,
  priority VARCHAR(64) NOT NULL,
  direction VARCHAR(128) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  INDEX idx_investment_match_resource (resource_id),
  INDEX idx_investment_match_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
