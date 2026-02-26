-- Migration: Remove annotation_type and confidence_rate columns from la_images_label
-- Date: 2026-02-09
-- Reason: Ground truth labels are now stored separately from predictions
--         Predictions are stored in la_images_prediction_label table
--         These columns are no longer needed in la_images_label

-- Remove annotation_type column
ALTER TABLE la_images_label DROP COLUMN IF EXISTS annotation_type;

-- Remove confidence_rate column
ALTER TABLE la_images_label DROP COLUMN IF EXISTS confidence_rate;

-- Verify the changes
\d la_images_label;
