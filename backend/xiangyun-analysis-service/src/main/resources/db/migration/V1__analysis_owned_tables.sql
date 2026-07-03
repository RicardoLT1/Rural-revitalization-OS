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
