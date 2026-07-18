CREATE TABLE IF NOT EXISTS auth_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  village_id VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_auth_user_role CHECK (role_code IN ('USER','STAFF','ADMIN')),
  UNIQUE KEY uk_auth_user_username(username),
  INDEX idx_auth_user_scope(village_id, role_code, enabled)
);
