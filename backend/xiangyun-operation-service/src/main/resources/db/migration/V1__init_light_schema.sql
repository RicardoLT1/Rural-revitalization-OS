CREATE TABLE IF NOT EXISTS village (
  id BIGINT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  region VARCHAR(128),
  address VARCHAR(255),
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  deleted TINYINT NOT NULL DEFAULT 0
);

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
  intro TEXT,
  owner VARCHAR(128),
  contact VARCHAR(64),
  related_projects VARCHAR(512),
  occupancy_rate INT NOT NULL DEFAULT 0,
  expected_roi INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS resource_tag (
  id BIGINT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS resource_tag_rel (
  resource_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY(resource_id, tag_id)
);

CREATE TABLE IF NOT EXISTS workflow (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(64) NOT NULL,
  status VARCHAR(64) NOT NULL,
  current_node_id VARCHAR(64),
  applicant VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS workflow_node (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  node_key VARCHAR(64) NOT NULL,
  title VARCHAR(128) NOT NULL,
  status VARCHAR(64) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  assignee VARCHAR(64),
  remark VARCHAR(255),
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS todo_item (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  category VARCHAR(64) NOT NULL,
  status VARCHAR(64) NOT NULL,
  due_date DATETIME,
  assignee VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS approval_record (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NOT NULL,
  node_id VARCHAR(64),
  title VARCHAR(160) NOT NULL,
  applicant VARCHAR(64),
  amount DECIMAL(12, 2),
  action VARCHAR(64),
  status VARCHAR(64) NOT NULL,
  remark VARCHAR(255),
  handled_at DATETIME,
  deleted TINYINT NOT NULL DEFAULT 0
);

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
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS forecast_result (
  id BIGINT PRIMARY KEY,
  village_id BIGINT NOT NULL,
  forecast_date DATE NOT NULL,
  actual_value INT,
  predict_value INT NOT NULL,
  upper_value INT NOT NULL,
  lower_value INT NOT NULL,
  risk_level VARCHAR(32) NOT NULL DEFAULT 'low',
  strategy VARCHAR(255),
  deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS investment_match_record (
  id BIGINT PRIMARY KEY,
  resource_id BIGINT NOT NULL,
  investor VARCHAR(128) NOT NULL,
  score INT NOT NULL DEFAULT 0,
  reason VARCHAR(512),
  priority VARCHAR(64) NOT NULL,
  direction VARCHAR(128),
  status VARCHAR(32) NOT NULL DEFAULT 'active',
  deleted TINYINT NOT NULL DEFAULT 0
);
