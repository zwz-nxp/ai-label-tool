-- PostgreSQL initialization script
-- Generated from JPA entities in com.nxp.iemdm.model.landingai

-- Create sequences for ID generation
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS image_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS image_label_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS image_metadata_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS image_tag_sequence START WITH 1 INCREMENT BY 1;

-- la_projects table (must be created first as it's referenced by others)
CREATE TABLE la_projects (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20),
    type VARCHAR(20),
    model_name VARCHAR(36),
    group_name VARCHAR(20),
    location_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_project_location FOREIGN KEY (location_id) REFERENCES GLOBAL_LOCATION(id)
);

-- la_project_class table
CREATE TABLE la_project_class (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    class_name VARCHAR(100),
    description VARCHAR(100),
    color_code VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_class_project FOREIGN KEY (project_id) REFERENCES la_projects(id)
);

-- la_project_split table
CREATE TABLE la_project_split (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    train_ratio INTEGER DEFAULT 70,
    dev_ratio INTEGER DEFAULT 20,
    test_ratio INTEGER DEFAULT 10,
    class_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_split_project FOREIGN KEY (project_id) REFERENCES la_projects(id),
	CONSTRAINT fk_split_class FOREIGN KEY (class_id) REFERENCES la_project_class(id)
	
);

-- la_project_tag table
CREATE TABLE la_project_tag (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_tag_project FOREIGN KEY (project_id) REFERENCES la_projects(id)
);

-- la_project_metadata table
CREATE TABLE la_project_metadata (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100),
    type VARCHAR(100),
    value_from VARCHAR(100),
    predefined_values VARCHAR(100),
    multiple_values BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_metadata_project FOREIGN KEY (project_id) REFERENCES la_projects(id)
);

-- la_images_file table (stores actual image binary data)
-- Note: Created BEFORE la_images to allow foreign key reference
-- Note: No foreign key constraint FROM this table to allow retention of files even after image metadata is deleted
CREATE TABLE la_images_file (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    image_file_stream BYTEA NOT NULL,
    legacy_image_id BIGINT,  -- Nullable: only populated during migration
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36)
);

-- la_images table
CREATE TABLE la_images (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    split VARCHAR(10),
    is_no_class BOOLEAN,
    is_labeled BOOLEAN DEFAULT FALSE,
    thumbnail_image BYTEA,
    thumbnail_width_ratio DOUBLE PRECISION,
    thumbnail_height_ratio DOUBLE PRECISION,
    file_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_images_project FOREIGN KEY (project_id) REFERENCES la_projects(id),
    CONSTRAINT fk_images_file FOREIGN KEY (file_id) REFERENCES la_images_file(id)
);

-- la_images_label table
CREATE TABLE la_images_label (
    id BIGINT PRIMARY KEY,
    image_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    position TEXT,
    confidence_rate INTEGER,
    annotation_type VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_label_image FOREIGN KEY (image_id) REFERENCES la_images(id),
	CONSTRAINT fk_label_class FOREIGN KEY (class_id) REFERENCES la_project_class(id)
);

-- la_images_tag table
CREATE TABLE la_images_tag (
    id BIGINT PRIMARY KEY,
    image_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_images_tag_image FOREIGN KEY (image_id) REFERENCES la_images(id),
    CONSTRAINT fk_images_tag_tag FOREIGN KEY (tag_id) REFERENCES la_project_tag(id)
);

-- la_images_metadata table
CREATE TABLE la_images_metadata (
    id BIGINT PRIMARY KEY,
    image_id BIGINT NOT NULL,
    metadata_id BIGINT NOT NULL,
    value VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_images_metadata_image FOREIGN KEY (image_id) REFERENCES la_images(id),
    CONSTRAINT fk_images_metadata_metadata FOREIGN KEY (metadata_id) REFERENCES la_project_metadata(id)
);

-- la_training_record table
CREATE TABLE la_training_record (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status VARCHAR(20),
    model_alias VARCHAR(36),
	track_id VARCHAR(50),
    epochs INTEGER,
    model_size VARCHAR(50),
    transform_param VARCHAR(500),
    augmentation_param VARCHAR(500),
    credit_consumption VARCHAR(500),
    training_count INTEGER,
    dev_count INTEGER,
    test_count INTEGER,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(36),
    CONSTRAINT fk_training_project FOREIGN KEY (project_id) REFERENCES la_projects(id)
);

-- la_model table
CREATE TABLE la_model (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    training_record_id BIGINT NOT NULL,
    model_alias VARCHAR(36),
    model_version VARCHAR(36),
    status VARCHAR(50),
    track_id VARCHAR(50),
    training_f1_rate NUMERIC(4,1),
    training_precision_rate NUMERIC(4,1),
    training_recall_rate NUMERIC(4,1),
    dev_f1_rate NUMERIC(4,1),
    dev_precision_rate NUMERIC(4,1),
    dev_recall_rate NUMERIC(4,1),
    test_f1_rate NUMERIC(4,1),
    test_precision_rate NUMERIC(4,1),
    test_recall_rate NUMERIC(4,1),
    image_count INTEGER,
    label_count INTEGER,
    is_favorite BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_model_project FOREIGN KEY (project_id) REFERENCES la_projects(id),
    CONSTRAINT fk_model_training FOREIGN KEY (training_record_id) REFERENCES la_training_record(id)
);

-- la_confidential_report table
CREATE TABLE la_confidential_report (
    id BIGINT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    training_correct_rate INTEGER,
    dev_correct_rate INTEGER,
    test_correct_rate INTEGER,
    confidence_threshold INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_report_model FOREIGN KEY (model_id) REFERENCES la_model(id)
);

-- la_snapshot table
CREATE TABLE la_snapshot (
    id BIGINT PRIMARY KEY,
    project_id BIGINT,
    snapshot_name VARCHAR(36),
    description VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_snapshot_project FOREIGN KEY (project_id) REFERENCES la_projects(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_project_class_project ON la_project_class(project_id);
CREATE INDEX idx_project_tag_project ON la_project_tag(project_id);
CREATE INDEX idx_project_metadata_project ON la_project_metadata(project_id);
CREATE INDEX idx_images_project ON la_images(project_id);
CREATE INDEX idx_images_split ON la_images(split);
CREATE INDEX idx_images_file_id ON la_images(file_id);
CREATE INDEX idx_label_image ON la_images_label(image_id);
CREATE INDEX idx_label_class ON la_images_label(class_id);
CREATE INDEX idx_images_tag_image ON la_images_tag(image_id);
CREATE INDEX idx_images_tag_tag ON la_images_tag(tag_id);
CREATE INDEX idx_images_metadata_image ON la_images_metadata(image_id);
CREATE INDEX idx_images_metadata_metadata ON la_images_metadata(metadata_id);
CREATE INDEX idx_training_project ON la_training_record(project_id);
CREATE INDEX idx_training_status ON la_training_record(status);
CREATE INDEX idx_model_project ON la_model(project_id);
CREATE INDEX idx_model_training ON la_model(training_record_id);
CREATE INDEX idx_report_model ON la_confidential_report(model_id);
CREATE INDEX idx_snapshot_project ON la_snapshot(project_id);

-- Snapshot tables (for historical data)

-- la_project_split_ss (snapshot) table
CREATE TABLE la_project_split_ss (
    id BIGINT,
    project_id BIGINT,
    train_ratio INTEGER,
    dev_ratio INTEGER,
    test_ratio INTEGER,
    class_id BIGINT,
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_project_class_ss (snapshot) table
CREATE TABLE la_project_class_ss (
    id BIGINT,
    project_id BIGINT,
    class_name VARCHAR(100),
    description VARCHAR(100),
    color_code VARCHAR(7),
    sequence INTEGER NOT NULL,
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_project_tag_ss (snapshot) table
CREATE TABLE la_project_tag_ss (
    id BIGINT,
    project_id BIGINT,
    name VARCHAR(100),
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_project_metadata_ss (snapshot) table
CREATE TABLE la_project_metadata_ss (
    id BIGINT,
    project_id BIGINT,
    name VARCHAR(100),
    type VARCHAR(100),
    value_from VARCHAR(100),
    predefined_values VARCHAR(100),
    multiple_values BOOLEAN,
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_images_ss (snapshot) table
CREATE TABLE la_images_ss (
    id BIGINT,
    project_id BIGINT,
    file_name VARCHAR(255),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    split VARCHAR(10),
    is_no_class BOOLEAN,
    is_labeled BOOLEAN,
    thumbnail_image BYTEA,
    thumbnail_width_ratio DOUBLE PRECISION,
    thumbnail_height_ratio DOUBLE PRECISION,
    file_id BIGINT,
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_images_label_ss (snapshot) table
CREATE TABLE la_images_label_ss (
    id BIGINT,
    image_id BIGINT,
    class_id BIGINT,
    position TEXT,
    confidence_rate INTEGER,
    annotation_type VARCHAR(32),
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_images_tag_ss (snapshot) table
CREATE TABLE la_images_tag_ss (
    id BIGINT,
    image_id BIGINT,
    tag_id BIGINT,
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- la_images_metadata_ss (snapshot) table
CREATE TABLE la_images_metadata_ss (
    id BIGINT,
    image_id BIGINT,
    metadata_id BIGINT,
    value VARCHAR(500),
    snapshot_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    PRIMARY KEY (id, snapshot_id)
);

-- audit tables
CREATE TABLE la_projects_aud (
    id BIGINT,
    name VARCHAR(255),
    status VARCHAR(20),
    type VARCHAR(20),
    model_name VARCHAR(36),
    group_name VARCHAR(20),
    location_id INTEGER,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
    revtype SMALLINT
);

-- la_project_class table
CREATE TABLE la_project_class_aud (
    id BIGINT,
    project_id BIGINT,
    class_name VARCHAR(100),
    description VARCHAR(100),
    color_code VARCHAR(7),
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
	revtype SMALLINT
);

-- la_project_split table
CREATE TABLE la_project_split_aud (
    id BIGINT,
    project_id BIGINT,
    train_ratio INTEGER,
    dev_ratio INTEGER,
    test_ratio INTEGER,
    class_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
	revtype SMALLINT
);

-- la_project_tag table
CREATE TABLE la_project_tag_aud (
    id BIGINT,
    project_id BIGINT,
    name VARCHAR(100),
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
	revtype SMALLINT
);

-- la_project_metadata table
CREATE TABLE la_project_metadata_aud (
    id BIGINT,
    project_id BIGINT,
    name VARCHAR(100),
    type VARCHAR(100),
    value_from VARCHAR(100),
    predefined_values VARCHAR(100),
    multiple_values BOOLEAN,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
	revtype SMALLINT
);

-- la_images table
CREATE TABLE la_images_aud (
    id BIGINT,
    project_id BIGINT,
    file_name VARCHAR(255),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    split VARCHAR(10),
    is_no_class BOOLEAN,
    is_labeled BOOLEAN,
    thumbnail_image BYTEA,
    thumbnail_width_ratio DOUBLE PRECISION,
    thumbnail_height_ratio DOUBLE PRECISION,
    file_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
    revtype SMALLINT
);

-- la_images_tag table
CREATE TABLE la_images_tag_aud (
    id BIGINT,
    image_id BIGINT,
    tag_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR(36),
	rev BIGINT,
	revtype SMALLINT
);

-- la_images_metadata table
CREATE TABLE la_images_metadata_aud (
    id BIGINT,
    image_id BIGINT,
    metadata_id BIGINT,
    value VARCHAR(500),
    created_at TIMESTAMP,
    created_by VARCHAR(36),
    rev BIGINT,
	revtype SMALLINT
);


-- la_confidential_report table
CREATE TABLE la_confidential_report_aud (
    id BIGINT,
    model_id BIGINT,
    training_correct_rate INTEGER,
    dev_correct_rate INTEGER,
    test_correct_rate INTEGER,
    confidence_threshold INTEGER,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    rev BIGINT,
	revtype SMALLINT
);

-- la_snapshot table
CREATE TABLE la_snapshot_aud (
    id BIGINT ,
    project_id BIGINT,
    snapshot_name VARCHAR(36),
    description VARCHAR(36),
    created_by VARCHAR(36),
    created_at TIMESTAMP,
    rev BIGINT,
	revtype SMALLINT
);


-- la_images_prediction_label table
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
    loss numeric(5,2),,
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
    map numeric(3,2),
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