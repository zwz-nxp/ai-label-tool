-- Create database
CREATE DATABASE IF NOT EXISTS image_recognition;
USE image_recognition;

-- Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Project Classes Table
CREATE TABLE IF NOT EXISTS project_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    class_index INT NOT NULL,
    color_code VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_class (project_id, class_name),
    INDEX idx_project_id (project_id)
);

-- Images Table
CREATE TABLE IF NOT EXISTS images (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    width INT,
    height INT,
    status VARCHAR(20) DEFAULT 'uploaded',
    is_labeled BOOLEAN DEFAULT FALSE,
    dataset_type VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_status (project_id, status),
    INDEX idx_labeled (is_labeled),
    INDEX idx_dataset_type (dataset_type)
);

-- Labels Table
CREATE TABLE IF NOT EXISTS labels (
    id VARCHAR(36) PRIMARY KEY,
    image_id VARCHAR(36) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    x_min DECIMAL(10,4) NOT NULL,
    y_min DECIMAL(10,4) NOT NULL,
    x_max DECIMAL(10,4) NOT NULL,
    y_max DECIMAL(10,4) NOT NULL,
    confidence DECIMAL(5,4) DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE,
    INDEX idx_image_id (image_id),
    INDEX idx_class_name (class_name)
);

-- Training Jobs Table
CREATE TABLE IF NOT EXISTS training_jobs (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    epochs INT,
    batch_size INT,
    learning_rate DECIMAL(10,8),
    current_epoch INT DEFAULT 0,
    current_loss DECIMAL(10,6),
    progress INT DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_status (project_id, status),
    INDEX idx_status (status)
);

-- Models Table
CREATE TABLE IF NOT EXISTS models (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    training_job_id VARCHAR(36),
    model_name VARCHAR(255) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    model_path VARCHAR(500),
    map_score DECIMAL(5,4),
    precision_score DECIMAL(5,4),
    recall_score DECIMAL(5,4),
    f1_score DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (training_job_id) REFERENCES training_jobs(id) ON DELETE SET NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_training_job_id (training_job_id)
);

-- Training History Table
CREATE TABLE IF NOT EXISTS training_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_job_id VARCHAR(36) NOT NULL,
    epoch INT NOT NULL,
    train_loss DECIMAL(10,6),
    val_loss DECIMAL(10,6),
    map_score DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (training_job_id) REFERENCES training_jobs(id) ON DELETE CASCADE,
    INDEX idx_training_epoch (training_job_id, epoch)
);
