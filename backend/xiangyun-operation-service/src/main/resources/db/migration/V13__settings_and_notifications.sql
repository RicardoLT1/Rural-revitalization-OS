CREATE TABLE IF NOT EXISTS system_setting (
  village_id BIGINT PRIMARY KEY,
  platform_name VARCHAR(64) NOT NULL,
  village_name VARCHAR(128) NOT NULL,
  map_center_lat DECIMAL(10, 6) NOT NULL,
  map_center_lng DECIMAL(10, 6) NOT NULL,
  approval_timeout_hours INT NOT NULL DEFAULT 24,
  weekly_report_day INT NOT NULL DEFAULT 1,
  workflow_notification_enabled TINYINT NOT NULL DEFAULT 1,
  risk_notification_enabled TINYINT NOT NULL DEFAULT 1,
  contact_phone VARCHAR(32),
  updated_by VARCHAR(128),
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO system_setting(
  village_id, platform_name, village_name, map_center_lat, map_center_lng,
  approval_timeout_hours, weekly_report_day, workflow_notification_enabled,
  risk_notification_enabled, contact_phone, updated_by
)
SELECT id, '乡耘 OS', name, 30.640522, 119.681337, 24, 1, 1, 1, '0572-8001200', 'system'
FROM village
WHERE deleted=0;

CREATE TABLE IF NOT EXISTS admin_notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  village_id BIGINT NOT NULL,
  business_key VARCHAR(160) NOT NULL,
  type VARCHAR(48) NOT NULL,
  title VARCHAR(160) NOT NULL,
  content VARCHAR(512) NOT NULL,
  target_path VARCHAR(255),
  target_role VARCHAR(32) NOT NULL DEFAULT 'STAFF_ADMIN',
  active TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_notification_business(village_id, business_key),
  INDEX idx_notification_scope(village_id, active, updated_at)
);

CREATE TABLE IF NOT EXISTS admin_notification_read (
  notification_id BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(notification_id, user_id),
  INDEX idx_notification_read_user(user_id, read_at)
);
