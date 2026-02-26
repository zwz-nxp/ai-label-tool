-- Create la_model_param table
CREATE TABLE la_model_param (
    id BIGSERIAL PRIMARY KEY,
    location_id INTEGER NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    parameters TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(96),
    CONSTRAINT fk_model_param_location FOREIGN KEY (location_id) REFERENCES global_location(id) ON DELETE CASCADE
);

-- Add comments
COMMENT ON TABLE la_model_param IS 'Model parameters for Object Detection, Classification, and Segmentation';
COMMENT ON COLUMN la_model_param.id IS 'Primary key';
COMMENT ON COLUMN la_model_param.location_id IS 'Foreign key to global_location';
COMMENT ON COLUMN la_model_param.model_name IS 'Name of the model (e.g., default value1, default value2)';
COMMENT ON COLUMN la_model_param.model_type IS 'Type of model: Object Detection, Classification, Segmentation';
COMMENT ON COLUMN la_model_param.parameters IS 'JSON string containing model parameters (Param_name1=default value1, Param_name2=default value2)';
COMMENT ON COLUMN la_model_param.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN la_model_param.created_by IS 'User who created the record';

-- Create index on location_id for faster lookups
CREATE INDEX idx_model_param_location ON la_model_param(location_id);
CREATE INDEX idx_model_param_type ON la_model_param(model_type);


-- Create la_model_param table
CREATE TABLE la_model_param_aud (
    id BIGSERIAL,
    location_id INTEGER NOT NULL,
    model_name VARCHAR(50) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    parameters TEXT,
    created_at TIMESTAMP,
    created_by VARCHAR(96),
	rev bigint,
    revtype smallint
);