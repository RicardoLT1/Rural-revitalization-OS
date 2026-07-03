ALTER TABLE workflow
  ADD COLUMN request_id VARCHAR(64) NULL AFTER applicant_user_id,
  ADD COLUMN applicant_name VARCHAR(128) NULL AFTER request_id,
  ADD COLUMN approver_id VARCHAR(64) NULL AFTER applicant_name,
  ADD COLUMN approver_name VARCHAR(128) NULL AFTER approver_id,
  ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER approver_name,
  ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

CREATE UNIQUE INDEX uk_workflow_request_id
  ON workflow(request_id);

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY,
  workflow_id BIGINT NULL,
  resource_id BIGINT NULL,
  action VARCHAR(64) NOT NULL,
  operator_id VARCHAR(64),
  operator_name VARCHAR(128),
  remark VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);
