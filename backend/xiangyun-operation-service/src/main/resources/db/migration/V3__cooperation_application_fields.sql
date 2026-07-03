ALTER TABLE workflow
  ADD COLUMN resource_id BIGINT NULL AFTER category,
  ADD COLUMN applicant_user_id VARCHAR(64) NULL AFTER applicant,
  ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER applicant_user_id;

ALTER TABLE todo_item
  ADD COLUMN assignee_id VARCHAR(64) NULL AFTER assignee;
