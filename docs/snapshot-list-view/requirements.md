# Requirements Document

## Introduction

The snapshot list view feature enables users to view, manage, and interact with dataset snapshots within a project. This feature provides a dedicated interface for browsing snapshot images, switching between snapshots, and performing snapshot operations such as reverting to previous states or creating new projects from snapshots. The initial implementation focuses on the Images tab, which displays snapshot images with their ground truth labels in a layout similar to the existing image upload page.

## Glossary

- **Snapshot**: A point-in-time capture of a project's complete dataset state, including images, labels, classes, tags, splits, and metadata
- **Images_Tab**: The primary view within the snapshot list interface that displays image thumbnails with label overlays
- **Snapshot_Sidebar**: A collapsible right-side panel that displays all available snapshots for the current project and allows snapshot selection
- **Ground_Truth_Label**: The verified, correct label assigned to an image by a human annotator
- **Revert_Operation**: The process of restoring a project's dataset to a previous snapshot state
- **Snapshot_Metadata**: Information about a snapshot including name, creation date, creator, image count, and class count
- **Filter_Service**: Backend service that applies filtering criteria to image datasets
- **Sort_Service**: Backend service that applies sorting criteria to image datasets
- **Project_State**: The complete set of data defining a project at a given time, including ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata

## Requirements

### Requirement 1: Display Snapshot Images

**User Story:** As a user, I want to view images from a selected snapshot with their ground truth labels, so that I can review the dataset state at that point in time.

#### Acceptance Criteria

1. WHEN the Images_Tab is displayed, THE System SHALL render image thumbnails with ground truth label overlays
2. WHEN displaying images, THE System SHALL use the same visual presentation as the image upload page ground truth mode
3. WHEN a snapshot contains more images than can fit on one page, THE System SHALL provide pagination controls
4. WHEN the user navigates between pages, THE System SHALL load and display the appropriate image subset
5. THE System SHALL display image metadata including filename and label information for each thumbnail

### Requirement 2: Snapshot Selection and Display

**User Story:** As a user, I want to view all available snapshots for my project and switch between them, so that I can compare different dataset states.

#### Acceptance Criteria

1. WHEN the snapshot list view loads, THE Snapshot_Sidebar SHALL display all snapshots for the current project
2. WHEN displaying a snapshot in the sidebar, THE System SHALL show its name, creation date, creator, image count, and class count
3. WHEN a user selects a different snapshot, THE System SHALL update the Images_Tab to display that snapshot's images
4. WHEN a snapshot is currently selected, THE Snapshot_Sidebar SHALL highlight that snapshot visually
5. THE Snapshot_Sidebar SHALL be collapsible to maximize the image viewing area

### Requirement 3: Filter and Sort Snapshot Images

**User Story:** As a user, I want to filter and sort images within a snapshot, so that I can find specific images or organize them by relevant criteria.

#### Acceptance Criteria

1. WHEN the user applies a filter, THE System SHALL use the Filter_Service to filter the displayed snapshot images
2. WHEN the user applies a sort criterion, THE System SHALL use the Sort_Service to reorder the displayed snapshot images
3. THE System SHALL provide the same filter options available on the image upload page
4. THE System SHALL provide sort options including date, name, and label status
5. WHEN filters or sorts are applied, THE System SHALL maintain pagination and update page counts accordingly

### Requirement 4: Create New Project from Snapshot

**User Story:** As a user, I want to create a new project using a snapshot's data, so that I can work with a copy of a previous dataset state without affecting the original project.

#### Acceptance Criteria

1. WHEN the user clicks the "Use" button, THE System SHALL display a dropdown with "Create new project (with snapshot data)" option
2. WHEN the user selects "Create new project (with snapshot data)", THE System SHALL prompt for a new project name
3. WHEN the user confirms project creation, THE System SHALL create a new project containing all data from the selected snapshot
4. THE System SHALL copy ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata from the snapshot to the new project
5. WHEN project creation completes, THE System SHALL display a success message and provide navigation to the new project

### Requirement 5: Revert Project to Snapshot

**User Story:** As a user, I want to revert my current project to a previous snapshot state, so that I can undo changes and restore a known good dataset configuration.

#### Acceptance Criteria

1. WHEN the user clicks the "Use" button, THE System SHALL display a dropdown with "Revert current dataset to this snapshot" option
2. WHEN the user selects "Revert current dataset to this snapshot", THE System SHALL display a confirmation dialog warning about data loss
3. WHEN the user confirms the revert operation, THE System SHALL create a backup snapshot of the current project state before reverting
4. WHEN creating the backup snapshot, THE System SHALL capture ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata
5. WHEN the backup is complete, THE System SHALL delete current project data from ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata tables
6. WHEN current data is deleted, THE System SHALL insert all snapshot data into ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata tables
7. WHEN the revert operation completes, THE System SHALL display a success message and refresh the project view
8. IF any step in the revert operation fails, THEN THE System SHALL roll back all changes and display an error message

### Requirement 6: Download Snapshot Dataset

**User Story:** As a user, I want to download a snapshot's dataset, so that I can export the data for external use or backup purposes.

#### Acceptance Criteria

1. WHEN the user clicks the "Manage" button in the toolbar, THE System SHALL display a dropdown with "Download dataset" option
2. WHEN the user selects "Download dataset", THE System SHALL initiate a download of the snapshot data
3. THE System SHALL export all snapshot images and associated metadata in a standard format
4. WHEN the download is prepared, THE System SHALL provide the file to the user's browser for download
5. WHEN the download is in progress, THE System SHALL display a progress indicator

### Requirement 7: Delete Snapshot

**User Story:** As a user, I want to delete snapshots I no longer need, so that I can manage storage and keep my snapshot list organized.

#### Acceptance Criteria

1. WHEN the user clicks the "Manage" button in the toolbar, THE System SHALL display a dropdown with "Delete snapshot" option
2. WHEN the user selects "Delete snapshot", THE System SHALL display a confirmation dialog
3. WHEN the user confirms deletion, THE System SHALL remove the snapshot and all associated data from the database
4. WHEN deletion completes, THE System SHALL update the Snapshot_Sidebar to remove the deleted snapshot
5. WHEN deletion completes, THE System SHALL display a success message
6. IF the deleted snapshot was currently selected, THEN THE System SHALL select the most recent remaining snapshot

### Requirement 8: Visual Feedback and Loading States

**User Story:** As a user, I want clear visual feedback during snapshot operations, so that I understand what the system is doing and when operations complete.

#### Acceptance Criteria

1. WHEN a long-running operation is in progress, THE System SHALL display a loading indicator
2. WHEN an operation completes successfully, THE System SHALL display a success message
3. WHEN an operation fails, THE System SHALL display an error message with details
4. WHEN the user initiates a destructive action, THE System SHALL display a confirmation modal dialog
5. THE System SHALL provide progress indicators for operations that may take significant time

### Requirement 9: UI Layout and Consistency

**User Story:** As a user, I want the snapshot list view to follow the same layout patterns as the image upload page, so that the interface feels familiar and consistent.

#### Acceptance Criteria

1. THE System SHALL use a layout similar to the image upload page with a main content area and right sidebar
2. THE System SHALL reuse existing image display components from the image upload page
3. THE System SHALL integrate with existing Filter_Service and Sort_Service implementations
4. THE System SHALL use consistent styling, spacing, and visual design patterns from the existing application
5. THE System SHALL provide a collapsible sidebar to maximize the image viewing area
