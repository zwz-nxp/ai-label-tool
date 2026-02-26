
ALTER TABLE la_images_label_ss
    DROP COLUMN confidence_rate,
    DROP COLUMN annotation_type;

ALTER TABLE la_images_label_aud
    DROP COLUMN confidence_rate,
    DROP COLUMN annotation_type;