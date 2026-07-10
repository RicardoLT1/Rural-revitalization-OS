ALTER TABLE resource
  ADD COLUMN ownership_status VARCHAR(64) NOT NULL DEFAULT '村集体确认' AFTER contact,
  ADD COLUMN material_status VARCHAR(64) NOT NULL DEFAULT '基础材料齐全' AFTER ownership_status,
  ADD COLUMN field_photos VARCHAR(1024) NULL AFTER material_status,
  ADD COLUMN investment_note VARCHAR(512) NULL AFTER field_photos;
