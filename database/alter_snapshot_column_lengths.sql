-- Migration script to increase column lengths in la_snapshot table
-- Date: 2026-02-11
-- Reason: snapshot_name and description columns are too short (36 chars)
--         Need to accommodate longer names like "Backup before revert - 2026-02-11 14:33:05"

-- Increase snapshot_name from VARCHAR(36) to VARCHAR(100) in main table
ALTER TABLE la_snapshot 
ALTER COLUMN snapshot_name TYPE VARCHAR(100);

-- Increase description from VARCHAR(36) to VARCHAR(200) in main table
ALTER TABLE la_snapshot 
ALTER COLUMN description TYPE VARCHAR(200);

-- Also update the audit table (Hibernate Envers creates _aud tables)
-- Increase snapshot_name from VARCHAR(36) to VARCHAR(100) in audit table
ALTER TABLE la_snapshot_aud 
ALTER COLUMN snapshot_name TYPE VARCHAR(100);

-- Increase description from VARCHAR(36) to VARCHAR(200) in audit table
ALTER TABLE la_snapshot_aud 
ALTER COLUMN description TYPE VARCHAR(200);

-- Verify the changes in main table
SELECT 
    column_name, 
    data_type, 
    character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'la_snapshot' 
  AND column_name IN ('snapshot_name', 'description');

-- Verify the changes in audit table
SELECT 
    column_name, 
    data_type, 
    character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'la_snapshot_aud' 
  AND column_name IN ('snapshot_name', 'description');
