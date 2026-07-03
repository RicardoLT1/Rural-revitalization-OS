SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN request_id VARCHAR(64) NULL AFTER applicant_user_id',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'request_id'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN applicant_name VARCHAR(128) NULL AFTER request_id',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'applicant_name'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN approver_id VARCHAR(64) NULL AFTER applicant_name',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'approver_id'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN approver_name VARCHAR(128) NULL AFTER approver_id',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'approver_name'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER approver_name',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'version'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE workflow ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND COLUMN_NAME = 'updated_at'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

SET @ddl = (
  SELECT IF(COUNT(*) = 0,
    'CREATE UNIQUE INDEX uk_workflow_request_id ON workflow(request_id)',
    'SELECT 1')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'workflow'
    AND INDEX_NAME = 'uk_workflow_request_id'
);
PREPARE xiangyun_stmt FROM @ddl;
EXECUTE xiangyun_stmt;
DEALLOCATE PREPARE xiangyun_stmt;

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
