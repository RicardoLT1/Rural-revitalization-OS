CREATE TABLE IF NOT EXISTS file_object (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  storage_key VARCHAR(255) NOT NULL UNIQUE,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(128) NOT NULL,
  file_size BIGINT NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  created_by VARCHAR(64),
  created_by_name VARCHAR(128),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  INDEX idx_file_sha256(sha256),
  INDEX idx_file_deleted(deleted_at)
);

CREATE TABLE IF NOT EXISTS resource_material (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  category VARCHAR(32) NOT NULL,
  title VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  is_cover TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  uploaded_by VARCHAR(64),
  uploaded_by_name VARCHAR(128),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  CONSTRAINT fk_resource_material_resource FOREIGN KEY(resource_id) REFERENCES resource(id),
  CONSTRAINT fk_resource_material_file FOREIGN KEY(file_id) REFERENCES file_object(id),
  INDEX idx_material_resource(resource_id, deleted_at, sort_order),
  INDEX idx_material_category(resource_id, category, deleted_at),
  INDEX idx_material_cover(resource_id, is_cover, deleted_at)
);
