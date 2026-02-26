# Requirements Document

## Introduction

This document specifies the requirements for implementing the Landing AI home functionality, which includes a project list view and project creation capability. The system enables users to manage AI/ML projects for object detection, segmentation, and classification tasks, with image upload and management capabilities.

## Glossary

- **Project**: An AI/ML project entity stored in the la_projects table containing images and labels for training
- **Image**: A visual asset stored in the la_images table associated with a project
- **Thumbnail**: A compressed preview version of an image stored as byte array for quick display
- **Location**: A site or facility that maintains its own isolated project list
- **Label**: An annotation or classification tag applied to an image
- **Object_Detection**: A project type for identifying and locating objects within images
- **Segmentation**: A project type for pixel-level classification of image regions
- **Classification**: A project type for categorizing entire images into classes
- **Default_Image**: A placeholder image displayed when a project has no uploaded images
- **Created_By**: The user identifier of the person who created the project
- **View_All_Mode**: A display mode showing all projects regardless of creator

## Requirements

### Requirement 1: Project List Display

**User Story:** As a user, I want to view a list of projects, so that I can access and manage my AI/ML projects.

#### Acceptance Criteria

1. WHEN a user accesses the project list page, THE System SHALL display all projects created by the logged-in user
2. WHEN displaying a project card, THE System SHALL show the project's first image as the card thumbnail
3. IF a project has no images, THEN THE System SHALL display a default placeholder image
4. WHEN displaying a project card, THE System SHALL show the project name below the thumbnail
5. WHEN displaying a project card, THE System SHALL show the creator's name below the project name
6. WHEN displaying a project card, THE System SHALL show the total count of images in the project
7. WHEN displaying a project card, THE System SHALL show the total count of labels in the project
8. WHEN a user's location is set, THE System SHALL filter projects to show only those belonging to that location

### Requirement 2: View All Projects Toggle

**User Story:** As a user, I want to toggle between viewing only my projects and all projects, so that I can see what others have created.

#### Acceptance Criteria

1. WHEN a user clicks the "View All" button, THE System SHALL display all projects in the user's location regardless of creator
2. WHEN viewing all projects, THE System SHALL still filter by the user's location
3. WHEN a user toggles back from "View All" mode, THE System SHALL display only projects created by the logged-in user

### Requirement 3: Project Creation Navigation

**User Story:** As a user, I want to navigate to a project creation page, so that I can create new AI/ML projects.

#### Acceptance Criteria

1. WHEN a user clicks the "Create Project" button, THE System SHALL navigate to the project creation page
2. THE System SHALL display the "Create Project" button prominently on the project list page

### Requirement 4: Project Type Selection

**User Story:** As a user, I want to select a project type, so that I can create projects suited to my AI/ML task.

#### Acceptance Criteria

1. WHEN a user accesses the project creation page, THE System SHALL display three project type options: Object Detection, Segmentation, and Classification
2. WHEN a user views project type options, THE System SHALL show Object Detection as the only selectable option
3. WHEN a user views project type options, THE System SHALL display Segmentation and Classification as disabled options
4. WHEN a user selects Object Detection, THE System SHALL enable the project creation workflow

### Requirement 5: Project Creation

**User Story:** As a user, I want to create a new project with a name and type, so that I can organize my AI/ML work.

#### Acceptance Criteria

1. WHEN a user provides a project name and selects Object Detection type, THE System SHALL create a new project record in the la_projects table
2. WHEN creating a project, THE System SHALL set the created_by field to the logged-in user's identifier
3. WHEN creating a project, THE System SHALL set the location_id to the user's current location
4. WHEN creating a project, THE System SHALL set the type field to "Object Detection"
5. WHEN creating a project, THE System SHALL set the created_at timestamp to the current time
6. WHEN a project name already exists, THE System SHALL prevent creation and display an error message

### Requirement 6: Image Upload to Project

**User Story:** As a user, I want to upload images to a new project, so that I can build a dataset for training.

#### Acceptance Criteria

1. WHEN a user is creating a project, THE System SHALL provide an image upload interface
2. WHEN a user uploads images, THE System SHALL accept PNG, JPG, and JPEG file formats
3. WHEN a user uploads an image, THE System SHALL store the image record in the la_images table
4. WHEN storing an image, THE System SHALL associate it with the project via the project_id foreign key
5. WHEN storing an image, THE System SHALL record the file_name, file_path, file_size, width, and height
6. WHEN storing an image, THE System SHALL set the created_by field to the logged-in user's identifier
7. WHEN storing an image, THE System SHALL set the created_at timestamp to the current time

### Requirement 7: Thumbnail Generation

**User Story:** As a system, I want to generate thumbnail images during upload, so that project lists can display quickly.

#### Acceptance Criteria

1. WHEN an image is uploaded, THE System SHALL generate a thumbnail version of the image
2. WHEN generating a thumbnail, THE System SHALL compress the image to reduce file size
3. WHEN generating a thumbnail, THE System SHALL store the thumbnail as a byte array in the thumbnail_image field
4. WHEN displaying project cards, THE System SHALL use the thumbnail_image data for rendering

### Requirement 8: Location-Based Project Isolation

**User Story:** As a location administrator, I want projects to be isolated by location, so that each facility maintains its own project list.

#### Acceptance Criteria

1. WHEN querying projects, THE System SHALL filter results by the user's location_id
2. WHEN a user switches locations, THE System SHALL display only projects belonging to the new location
3. WHEN creating a project, THE System SHALL automatically assign the user's current location_id

### Requirement 9: Project Card Interaction

**User Story:** As a user, I want to click on a project card, so that I can view and manage project details.

#### Acceptance Criteria

1. WHEN a user clicks on a project card, THE System SHALL navigate to the project detail page
2. WHEN hovering over a project card, THE System SHALL provide visual feedback indicating it is clickable

### Requirement 10: Frontend-Backend Separation

**User Story:** As a system architect, I want clear separation between frontend and backend components, so that the system is maintainable.

#### Acceptance Criteria

1. WHEN implementing the frontend, THE System SHALL create components in a dedicated landingai package
2. WHEN implementing the backend, THE System SHALL create services and controllers in a dedicated landingai package
3. WHEN implementing the feature, THE System SHALL not modify existing packages for other features
