-- Migration: Decouple image files from image records
-- Purpose: Allow multiple projects to share the same image files
-- Date: 2026-02-05

-- IMPORTANT: Backup your database before running this migration!

-- Step 1: Add new id column to la_images_file
ALTER TABLE la_images_file
  ADD COLUMN id BIGSERIAL;

-- Step 2: Drop the old primary key constraint
ALTER TABLE la_images_file
  DROP CONSTRAINT la_images_file_pkey;

-- Step 3: Add new primary key on id
ALTER TABLE la_images_file
  ADD PRIMARY KEY (id);

-- Step 4: Rename image_id to legacy_image_id for reference
ALTER TABLE la_images_file
  RENAME COLUMN image_id TO legacy_image_id;

-- Step 4a: Remove NOT NULL constraint from legacy_image_id (if exists)
-- This allows new uploads to have NULL legacy_image_id
ALTER TABLE la_images_file
  ALTER COLUMN legacy_image_id DROP NOT NULL;

-- Step 5: Add file_id column to la_images
ALTER TABLE la_images
  ADD COLUMN file_id BIGINT;

-- Step 6: Populate file_id in la_images by matching legacy_image_id
UPDATE la_images i
SET file_id = (
  SELECT f.id 
  FROM la_images_file f 
  WHERE f.legacy_image_id = i.id
);

-- Step 7: Add foreign key constraint (now that primary key is established)
ALTER TABLE la_images
  ADD CONSTRAINT la_images_file_id_fkey 
  FOREIGN KEY (file_id) 
  REFERENCES la_images_file(id);

-- Step 8: Add file_id to la_images_ss (snapshot table)
ALTER TABLE la_images_ss
  ADD COLUMN file_id BIGINT;

-- Step 9: Populate file_id in la_images_ss
UPDATE la_images_ss ss
SET file_id = (
  SELECT f.id 
  FROM la_images_file f 
  WHERE f.legacy_image_id = ss.id
);

-- Step 10: Create indexes for performance
CREATE INDEX idx_la_images_file_id ON la_images(file_id);
CREATE INDEX idx_la_images_ss_file_id ON la_images_ss(file_id);

-- Step 11: Add file_id to audit table (for Hibernate Envers)
ALTER TABLE la_images_aud
  ADD COLUMN file_id BIGINT;

-- Step 12: Add comments for documentation
COMMENT ON COLUMN la_images_file.id IS 'Primary key for image file records';
COMMENT ON COLUMN la_images_file.legacy_image_id IS 'Legacy reference to la_images.id (deprecated, kept for migration reference)';
COMMENT ON COLUMN la_images.file_id IS 'Foreign key to la_images_file.id - links to shared image file';
COMMENT ON COLUMN la_images_ss.file_id IS 'Foreign key to la_images_file.id - preserved in snapshot';

-- Verification queries (run these to verify migration success)
-- SELECT COUNT(*) FROM la_images WHERE file_id IS NULL; -- Should be 0
-- SELECT COUNT(*) FROM la_images_ss WHERE file_id IS NULL; -- Should be 0 if snapshots exist
-- SELECT COUNT(*) FROM la_images_file WHERE id IS NULL; -- Should be 0

-- Success message
DO $$
BEGIN
    RAISE NOTICE 'Migration completed successfully!';
    RAISE NOTICE 'la_images_file now uses id as primary key';
    RAISE NOTICE 'la_images now references files via file_id';
    RAISE NOTICE 'la_images_ss now includes file_id for snapshot support';
    RAISE NOTICE 'la_images_aud now includes file_id for audit support';
END $$;
