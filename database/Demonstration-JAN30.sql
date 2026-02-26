CREATE TABLE la_images_prediction_label (
    id BIGINT NOT NULL,
    image_id BIGINT,
    class_id BIGINT,
    model_id BIGINT,
    position TEXT,
    confidence_rate INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(36),
    CONSTRAINT la_images_prediction_label_pkey PRIMARY KEY (id),
    CONSTRAINT fk_prediction_label_class FOREIGN KEY (class_id)
        REFERENCES la_project_class (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_prediction_label_image FOREIGN KEY (image_id)
        REFERENCES la_images (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_prediction_label_model FOREIGN KEY (model_id)
        REFERENCES la_model (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX fki_fk_prediction_label_class
    ON la_images_prediction_label USING btree (class_id ASC NULLS LAST);

CREATE INDEX fki_fk_prediction_label_image
    ON la_images_prediction_label USING btree (image_id ASC NULLS LAST);

CREATE INDEX fki_fk_prediction_label_model
    ON la_images_prediction_label USING btree (model_id ASC NULLS LAST);
	
	-- la_loss_chart table
CREATE TABLE la_loss_chart
(
    id bigint NOT NULL,
    model_id bigint NOT NULL,
    loss integer,
    created_at timestamp without time zone,
    created_by character varying(36) COLLATE pg_catalog."default",
    CONSTRAINT la_loss_chart_pkey PRIMARY KEY (id),
    CONSTRAINT fk_loss_chart_model FOREIGN KEY (model_id)
        REFERENCES public.la_model (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX fki_ft_loss_chart_model
    ON public.la_loss_chart USING btree
    (model_id ASC NULLS LAST);
	
-- la_validation_chart table
CREATE TABLE la_validation_chart
(
    id bigint NOT NULL,
    model_id bigint NOT NULL,
    map numeric(5,4),
    created_at timestamp without time zone,
    created_by character varying(36) COLLATE pg_catalog."default",
    CONSTRAINT la_validation_chart_pkey PRIMARY KEY (id),
    CONSTRAINT fk_validation_chart_model FOREIGN KEY (model_id)
        REFERENCES public.la_model (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE INDEX fki_fk_validation_chart_model
    ON public.la_validation_chart USING btree
    (model_id ASC NULLS LAST);
	

alter table la_images add thumbnail_height_ratio DOUBLE PRECISION;
alter table la_images add thumbnail_width_ratio DOUBLE PRECISION;
alter table la_images add is_labeled boolean;
alter table la_images_ss add thumbnail_height_ratio DOUBLE PRECISION;
alter table la_images_ss add thumbnail_width_ratio DOUBLE PRECISION;
alter table la_images_ss add is_labeled boolean;
alter table la_images_aud add thumbnail_width_ratio DOUBLE PRECISION;
alter table la_images_aud add thumbnail_height_ratio DOUBLE PRECISION;
alter table la_images_aud add is_labeled boolean;
alter table la_model add training_f1_rate INTEGER;
alter table la_model add training_precision_rate INTEGER;
alter table la_model add training_recall_rate INTEGER;
alter table la_model add dev_f1_rate INTEGER;
alter table la_model add dev_precision_rate INTEGER;
alter table la_model add dev_recall_rate INTEGER;
alter table la_model add test_f1_rate INTEGER;
alter table la_model add test_precision_rate INTEGER;
alter table la_model add test_recall_rate INTEGER;
alter table la_model add status VARCHAR(50);
alter table la_model_aud add training_f1_rate INTEGER;
alter table la_model_aud add training_precision_rate INTEGER;
alter table la_model_aud add training_recall_rate INTEGER;
alter table la_model_aud add dev_f1_rate INTEGER;
alter table la_model_aud add dev_precision_rate INTEGER;
alter table la_model_aud add dev_recall_rate INTEGER;
alter table la_model_aud add test_f1_rate INTEGER;
alter table la_model_aud add test_precision_rate INTEGER;
alter table la_model_aud add test_recall_rate INTEGER;
alter table la_model_aud add status VARCHAR(50);
ALTER TABLE la_images_label ALTER COLUMN annotation_type TYPE VARCHAR(36);
ALTER TABLE la_images_label_ss ALTER COLUMN annotation_type TYPE VARCHAR(36);
ALTER TABLE la_images_label_aud ALTER COLUMN annotation_type TYPE VARCHAR(36);

-- Migration script: Change snapshot tables from snapshot_name to snapshot_id
-- This script migrates all _ss (snapshot) tables to use snapshot_id (BIGINT) instead of snapshot_name (VARCHAR)

-- =====================================================
-- STEP 1: Add snapshot_id column to all snapshot tables
-- =====================================================

ALTER TABLE la_project_split_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_project_class_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_project_tag_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_project_metadata_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_images_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_images_label_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_images_tag_ss ADD COLUMN snapshot_id BIGINT;
ALTER TABLE la_images_metadata_ss ADD COLUMN snapshot_id BIGINT;

-- =====================================================
-- STEP 2: Populate snapshot_id from la_snapshot table
-- =====================================================

UPDATE la_project_split_ss ss
SET snapshot_id = s.id
FROM la_snapshot s
WHERE ss.snapshot_name = s.snapshot_name AND ss.project_id = s.project_id;

UPDATE la_project_class_ss ss
SET snapshot_id = s.id
FROM la_snapshot s
WHERE ss.snapshot_name = s.snapshot_name AND ss.project_id = s.project_id;

UPDATE la_project_tag_ss ss
SET snapshot_id = s.id
FROM la_snapshot s
WHERE ss.snapshot_name = s.snapshot_name AND ss.project_id = s.project_id;

UPDATE la_project_metadata_ss ss
SET snapshot_id = s.id
FROM la_snapshot s
WHERE ss.snapshot_name = s.snapshot_name AND ss.project_id = s.project_id;

UPDATE la_images_ss ss
SET snapshot_id = s.id
FROM la_snapshot s
WHERE ss.snapshot_name = s.snapshot_name AND ss.project_id = s.project_id;

-- For tables without project_id, join through la_images_ss
UPDATE la_images_label_ss lss
SET snapshot_id = iss.snapshot_id
FROM la_images_ss iss
WHERE lss.image_id = iss.id AND lss.snapshot_name = iss.snapshot_name;

UPDATE la_images_tag_ss tss
SET snapshot_id = iss.snapshot_id
FROM la_images_ss iss
WHERE tss.image_id = iss.id AND tss.snapshot_name = iss.snapshot_name;

UPDATE la_images_metadata_ss mss
SET snapshot_id = iss.snapshot_id
FROM la_images_ss iss
WHERE mss.image_id = iss.id AND mss.snapshot_name = iss.snapshot_name;

-- =====================================================
-- STEP 3: Drop the old snapshot_name column
-- =====================================================

ALTER TABLE la_project_split_ss DROP COLUMN snapshot_name;
ALTER TABLE la_project_class_ss DROP COLUMN snapshot_name;
ALTER TABLE la_project_tag_ss DROP COLUMN snapshot_name;
ALTER TABLE la_project_metadata_ss DROP COLUMN snapshot_name;
ALTER TABLE la_images_ss DROP COLUMN snapshot_name;
ALTER TABLE la_images_label_ss DROP COLUMN snapshot_name;
ALTER TABLE la_images_tag_ss DROP COLUMN snapshot_name;
ALTER TABLE la_images_metadata_ss DROP COLUMN snapshot_name;

-- =====================================================
-- STEP 4: Add foreign key constraints (optional but recommended)
-- =====================================================

ALTER TABLE la_project_split_ss 
ADD CONSTRAINT fk_project_split_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_project_class_ss 
ADD CONSTRAINT fk_project_class_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_project_tag_ss 
ADD CONSTRAINT fk_project_tag_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_project_metadata_ss 
ADD CONSTRAINT fk_project_metadata_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_images_ss 
ADD CONSTRAINT fk_images_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_images_label_ss 
ADD CONSTRAINT fk_images_label_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_images_tag_ss 
ADD CONSTRAINT fk_images_tag_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

ALTER TABLE la_images_metadata_ss 
ADD CONSTRAINT fk_images_metadata_ss_snapshot FOREIGN KEY (snapshot_id) REFERENCES la_snapshot(id);

-- =====================================================
-- STEP 5: Create indexes for better query performance
-- =====================================================

CREATE INDEX idx_project_split_ss_snapshot ON la_project_split_ss(snapshot_id);
CREATE INDEX idx_project_class_ss_snapshot ON la_project_class_ss(snapshot_id);
CREATE INDEX idx_project_tag_ss_snapshot ON la_project_tag_ss(snapshot_id);
CREATE INDEX idx_project_metadata_ss_snapshot ON la_project_metadata_ss(snapshot_id);
CREATE INDEX idx_images_ss_snapshot ON la_images_ss(snapshot_id);
CREATE INDEX idx_images_label_ss_snapshot ON la_images_label_ss(snapshot_id);
CREATE INDEX idx_images_tag_ss_snapshot ON la_images_tag_ss(snapshot_id);
CREATE INDEX idx_images_metadata_ss_snapshot ON la_images_metadata_ss(snapshot_id);



