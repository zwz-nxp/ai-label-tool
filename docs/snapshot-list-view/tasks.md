# Implementation Plan: Snapshot List View

## Overview

This implementation plan breaks down the snapshot list view feature into discrete coding tasks. The approach follows an incremental development strategy: starting with core data structures and state management, then building UI components, implementing snapshot operations, and finally adding filtering, sorting, and error handling. Each task builds on previous work to ensure continuous integration and testability.

## Tasks

- [x] 1. Set up NgRx state management for snapshot list view
  - Create state interface with snapshots, selectedSnapshotId, images, pagination, filters, sortCriteria, loading states, error, and sidebarCollapsed
  - Define actions: loadSnapshots, selectSnapshot, loadSnapshotImages, applyFilters, applySortCriteria, changePage, createProjectFromSnapshot, revertToSnapshot, downloadSnapshot, deleteSnapshot, toggleSidebar
  - Implement reducer to handle all state transitions
  - Create selectors for accessing state slices
  - _Requirements: 1.1, 2.1, 2.3, 3.1, 3.2_

- [ ]* 1.1 Write property test for pagination state updates
  - **Property 2: Pagination Activation**
  - **Validates: Requirements 1.3**

- [x] 2. Create snapshot service for API communication
  - [x] 2.1 Implement TypeScript SnapshotService with HTTP methods
    - getProjectSnapshots(projectId: number): Observable<Snapshot[]>
    - getSnapshotImages(snapshotId: number, page: number, size: number): Observable<PaginatedResponse<ImageListItemDTO>>
    - createProjectFromSnapshot(snapshotId: number, projectName: string): Observable<Project>
    - revertToSnapshot(snapshotId: number, projectId: number): Observable<void>
    - downloadSnapshot(snapshotId: number): Observable<Blob>
    - deleteSnapshot(snapshotId: number): Observable<void>
    - _Requirements: 2.1, 4.3, 5.3, 6.2, 7.3_

  - [ ]* 2.2 Write unit tests for SnapshotService HTTP calls
    - Test each method with mocked HTTP responses
    - Test error handling for failed requests
    - _Requirements: 2.1, 4.3, 5.3, 6.2, 7.3_

- [x] 3. Implement NgRx effects for async operations
  - Create effects for loadSnapshots$, selectSnapshot$, loadSnapshotImages$, applyFilters$, applySortCriteria$, changePage$
  - Create effects for createProjectFromSnapshot$, revertToSnapshot$, downloadSnapshot$, deleteSnapshot$
  - Handle success and error actions for each effect
  - Dispatch notification actions for user feedback
  - _Requirements: 2.1, 2.3, 3.1, 3.2, 4.3, 5.3, 6.2, 7.3_

- [ ]* 3.1 Write unit tests for NgRx effects
  - Test each effect with mocked service responses
  - Test error handling paths
  - Verify correct action dispatching
  - _Requirements: 2.1, 2.3, 3.1, 3.2, 4.3, 5.3, 6.2, 7.3_

- [x] 4. Create SnapshotListViewComponent (main container)
  - [x] 4.1 Implement component class with NgRx integration
    - Connect to store for snapshots$, selectedSnapshotId$, images$, pagination$, filters$, loading$, sidebarCollapsed$
    - Implement onSnapshotSelected, onFilterApplied, onSortApplied, onPageChanged, onManageAction, onToggleSidebar methods
    - Dispatch appropriate actions for each user interaction
    - _Requirements: 1.1, 2.1, 2.3, 3.1, 3.2_

  - [x] 4.2 Create component template with layout structure
    - Add snapshot-toolbar, filter-panel, image-grid, and snapshot-sidebar components
    - Use async pipe for all observables
    - Apply responsive layout with flexbox
    - _Requirements: 1.1, 2.1, 9.1_

  - [x] 4.3 Add component styles
    - Define layout styles for main container, content area, and sidebar
    - Ensure responsive behavior
    - _Requirements: 9.1, 9.4_

  - [ ]* 4.4 Write unit tests for SnapshotListViewComponent
    - Test component initialization and data loading
    - Test user interaction handlers
    - Test action dispatching
    - _Requirements: 1.1, 2.1, 2.3_

- [x] 5. Create SnapshotSidebarComponent
  - [x] 5.1 Implement component class
    - Define inputs: snapshots, selectedId, collapsed
    - Define outputs: snapshotSelected, toggleCollapse
    - Implement selectSnapshot and toggleCollapse methods
    - Implement isSelected helper method
    - _Requirements: 2.1, 2.3, 2.4, 2.5_

  - [x] 5.2 Create component template
    - Render list of snapshot-card components
    - Add collapse/expand button
    - Apply conditional styling based on collapsed state
    - _Requirements: 2.1, 2.4, 2.5_

  - [x] 5.3 Add component styles
    - Style sidebar container with fixed width and scrolling
    - Style collapse button and transitions
    - Add collapsed state styles
    - _Requirements: 2.5, 9.4_

  - [ ]* 5.4 Write property test for snapshot display completeness
    - **Property 4: Complete Snapshot Display**
    - **Validates: Requirements 2.1**

  - [ ]* 5.5 Write property test for selected snapshot highlighting
    - **Property 7: Selected Snapshot Highlighting**
    - **Validates: Requirements 2.4**

- [x] 6. Create SnapshotCardComponent
  - [x] 6.1 Implement component class
    - Define inputs: snapshot, selected
    - Define output: clicked
    - Implement click handler
    - _Requirements: 2.2, 2.4_

  - [x] 6.2 Create component template
    - Display snapshot name, creation date, creator, image count, class count
    - Apply selected class conditionally
    - _Requirements: 2.2, 2.4_

  - [x] 6.3 Add component styles
    - Style card with border, padding, hover effects
    - Style selected state with highlight color
    - _Requirements: 2.4, 9.4_

  - [ ]* 6.4 Write property test for snapshot metadata completeness
    - **Property 5: Snapshot Metadata Completeness**
    - **Validates: Requirements 2.2**

- [x] 7. Create SnapshotToolbarComponent
  - [x] 7.1 Implement component class
    - Define input: selectedSnapshot
    - Define output: manageAction
    - Implement onUseSnapshot and onManageSnapshot methods
    - _Requirements: 4.1, 5.1, 6.1, 7.1_

  - [x] 7.2 Create component template
    - Add "Use" button with dropdown (Create new project, Revert to snapshot)
    - Add "Manage" button with dropdown (Download dataset, Delete snapshot)
    - Disable buttons when no snapshot selected
    - _Requirements: 4.1, 5.1, 6.1, 7.1_

  - [x] 7.3 Add component styles
    - Style toolbar with flexbox layout
    - Style dropdown menus
    - _Requirements: 9.4_

  - [ ]* 7.4 Write unit tests for SnapshotToolbarComponent
    - Test button click handlers
    - Test dropdown menu interactions
    - Test disabled state when no snapshot selected
    - _Requirements: 4.1, 5.1, 6.1, 7.1_

- [x] 8. Checkpoint - Ensure frontend components render correctly
  - Verify all components compile without errors
  - Manually test component rendering in browser
  - Ensure all tests pass
  - Ask the user if questions arise

- [x] 9. Implement backend SnapshotController endpoints
  - [x] 9.1 Create POST /api/snapshots/{snapshotId}/create-project endpoint
    - Accept CreateProjectRequest with snapshotId, projectName, userId
    - Call SnapshotService.createProjectFromSnapshot
    - Return ProjectDTO
    - Handle exceptions and return appropriate error responses
    - _Requirements: 4.3, 4.4_

  - [x] 9.2 Create POST /api/snapshots/{snapshotId}/revert endpoint
    - Accept projectId as request parameter
    - Call SnapshotService.revertProjectToSnapshot
    - Return success response
    - Handle exceptions and return appropriate error responses
    - _Requirements: 5.3, 5.4, 5.5, 5.6_

  - [x] 9.3 Create GET /api/snapshots/{snapshotId}/download endpoint
    - Call SnapshotService.downloadSnapshotDataset
    - Return byte array with appropriate content type
    - Handle exceptions and return appropriate error responses
    - _Requirements: 6.2, 6.3_

  - [x] 9.4 Create DELETE /api/snapshots/{snapshotId} endpoint
    - Call SnapshotService.deleteSnapshot
    - Return success response
    - Handle exceptions and return appropriate error responses
    - _Requirements: 7.3_

  - [ ]* 9.5 Write unit tests for SnapshotController endpoints
    - Test each endpoint with valid inputs
    - Test error handling for invalid inputs
    - Test HTTP status codes
    - _Requirements: 4.3, 5.3, 6.2, 7.3_

- [x] 10. Implement SnapshotService business logic (using trigger-based snapshot architecture)
  - [x] 10.1 Implement createProjectFromSnapshot method
    - Create new Project entity with properties from source project
    - Read snapshot data from _ss tables (la_project_class_ss, la_project_tag_ss, etc.)
    - Copy data to new project tables with new IDs, maintaining ID mappings for FK relationships
    - Use @Transactional to ensure atomicity
    - Note: Snapshot data already exists in _ss tables (created by database trigger)
    - _Requirements: 4.3, 4.4_

  - [ ]* 10.2 Write property test for complete project data copy
    - **Property 11: Complete Project Data Copy**
    - **Validates: Requirements 4.3, 4.4**

  - [x] 10.3 Implement revertProjectToSnapshot method
    - Create backup snapshot by inserting into la_snapshot (trigger auto-captures current data)
    - Delete current project data from main tables (respecting FK constraints)
    - Read snapshot data from _ss tables and insert into main project tables (preserving original IDs)
    - Use @Transactional to ensure atomicity and rollback on error
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.8_

  - [ ]* 10.4 Write property test for backup snapshot creation
    - **Property 12: Backup Snapshot Creation**
    - **Validates: Requirements 5.3**

  - [ ]* 10.5 Write property test for complete backup capture
    - **Property 13: Complete Backup Capture**
    - Note: Backup capture is handled by database trigger
    - **Validates: Requirements 5.4**

  - [ ]* 10.6 Write property test for complete data cleanup
    - **Property 14: Complete Data Cleanup**
    - **Validates: Requirements 5.5**

  - [ ]* 10.7 Write property test for complete data restoration
    - **Property 15: Complete Data Restoration**
    - **Validates: Requirements 5.6**

  - [ ]* 10.8 Write property test for revert transaction rollback
    - **Property 16: Revert Transaction Rollback**
    - **Validates: Requirements 5.8**

  - [x] 10.9 Implement downloadSnapshotDataset method
    - Query snapshot data from _ss tables (la_images_ss, la_images_label_ss, etc.)
    - Include image files from la_images_file table using file_id
    - Format data as ZIP archive with images and JSON metadata
    - Return byte array
    - _Requirements: 6.3_

  - [ ]* 10.10 Write property test for complete snapshot export
    - **Property 17: Complete Snapshot Export**
    - **Validates: Requirements 6.3**

  - [x] 10.11 Implement deleteSnapshot method
    - Delete snapshot data from all _ss tables (la_images_metadata_ss, la_images_tag_ss, etc.)
    - Delete the main snapshot record from la_snapshot
    - Use @Transactional to ensure atomicity
    - _Requirements: 7.3_

  - [ ]* 10.12 Write property test for snapshot deletion completeness
    - **Property 18: Snapshot Deletion Completeness**
    - **Validates: Requirements 7.3**

- [x] 11. Implement helper methods for snapshot operations
  - [x] 11.1 Implement createBackupSnapshot helper
    - Generate backup name with timestamp format: "Backup before revert - YYYY-MM-DD HH:mm:ss"
    - Insert into la_snapshot table (database trigger handles data capture)
    - Return created Snapshot entity
    - _Requirements: 5.3, 5.4_

  - [x] 11.2 Implement deleteProjectData helper
    - Delete records in correct order to respect foreign key constraints:
      1. la_images_metadata (by image.project_id)
      2. la_images_tag (by image.project_id)
      3. la_images_label (by image.project_id)
      4. la_images (by project_id)
      5. la_project_split (by project_id)
      6. la_project_metadata (by project_id)
      7. la_project_tag (by project_id)
      8. la_project_class (by project_id)
    - _Requirements: 5.5_

  - [x] 11.3 Implement restoreSnapshotData helper
    - Read data from _ss tables by snapshot_id
    - Create ID mappings for classes, tags, metadata (old ID -> new ID)
    - Insert into main project tables with new IDs
    - Update foreign key references using ID mappings
    - Order: classes -> tags -> metadata -> splits -> images -> labels -> image_tags -> image_metadata
    - _Requirements: 5.6_

  - [x] 11.4 Implement copySnapshotDataToProject helper methods
    - copySnapshotProjectClasses: Read from la_project_class_ss, insert to la_project_class
    - copySnapshotProjectTags: Read from la_project_tag_ss, insert to la_project_tag
    - copySnapshotProjectMetadata: Read from la_project_metadata_ss, insert to la_project_metadata
    - copySnapshotProjectSplits: Read from la_project_split_ss, insert to la_project_split (with class ID mapping)
    - copySnapshotImages: Read from la_images_ss, insert to la_images
    - copySnapshotImageLabels: Read from la_images_label_ss, insert to la_images_label (with image/class ID mapping)
    - copySnapshotImageTags: Read from la_images_tag_ss, insert to la_images_tag (with image/tag ID mapping)
    - copySnapshotImageMetadata: Read from la_images_metadata_ss, insert to la_images_metadata (with image/metadata ID mapping)
    - _Requirements: 4.3, 4.4, 5.6_

  - [ ]* 11.5 Write unit tests for helper methods
    - Test each helper method independently
    - Test ID mapping correctness
    - Test error handling
    - _Requirements: 5.3, 5.4, 5.5, 5.6_

- [ ] 12. Checkpoint - Ensure backend services work correctly
  - Run all backend unit tests and property tests
  - Test API endpoints with Postman or similar tool
  - Verify database transactions and rollback behavior
  - Ask the user if questions arise

- [x] 13. Integrate filter and sort functionality
  - [x] 13.1 Connect FilterPanelComponent to snapshot images
    - Reuse existing FilterPanelComponent from image-upload
    - Dispatch applyFilters action when filters change
    - _Requirements: 3.1, 3.3_

  - [x] 13.2 Implement filter effect to call backend Filter_Service
    - Use existing ImageService.filterImages method
    - Pass snapshot images and filter criteria
    - Update state with filtered results
    - _Requirements: 3.1_

  - [ ]* 13.3 Write property test for filter application correctness
    - **Property 8: Filter Application Correctness**
    - **Validates: Requirements 3.1**

  - [x] 13.4 Implement sort functionality
    - Add sort dropdown to toolbar or filter panel
    - Dispatch applySortCriteria action when sort changes
    - Implement sort effect to call backend Sort_Service
    - _Requirements: 3.2, 3.4_

  - [ ]* 13.5 Write property test for sort order correctness
    - **Property 9: Sort Order Correctness**
    - **Validates: Requirements 3.2**

  - [ ]* 13.6 Write property test for pagination update on filter
    - **Property 10: Pagination Update on Filter**
    - **Validates: Requirements 3.5**

- [x] 14. Implement pagination functionality
  - [x] 14.1 Add pagination controls to ImageGridComponent
    - Reuse existing pagination component if available
    - Display current page, total pages, page size
    - Add previous/next buttons and page number input
    - _Requirements: 1.3, 1.4_

  - [x] 14.2 Implement page change handler
    - Dispatch changePage action when user navigates
    - Update state with new page number
    - Trigger loadSnapshotImages effect with new page
    - _Requirements: 1.4_

  - [ ]* 14.3 Write property test for correct page subset
    - **Property 3: Correct Page Subset**
    - **Validates: Requirements 1.4**

- [x] 15. Implement confirmation dialogs
  - [x] 15.1 Create ConfirmDialogComponent (reusable)
    - Define inputs: title, message, confirmButtonText, cancelButtonText, confirmButtonStyle
    - Define output: confirmed
    - Create template with dialog layout
    - _Requirements: 5.2, 7.2, 8.4_

  - [x] 15.2 Integrate confirmation dialog for delete snapshot
    - Open dialog when user clicks Delete in Manage menu
    - Set title: "Delete Snapshot?"
    - Set message: "Are you sure you want to delete '[snapshot name]'? This action cannot be undone."
    - Dispatch deleteSnapshot action only if confirmed
    - _Requirements: 7.2, 8.4_

  - [x] 15.3 Integrate confirmation dialog for revert to snapshot
    - Open dialog when user clicks Revert in Use menu
    - Set title: "Revert to Snapshot?"
    - Set message: "This will replace your current project data with the snapshot '[snapshot name]'. Your current data will be backed up automatically. Do you want to continue?"
    - Dispatch revertToSnapshot action only if confirmed
    - _Requirements: 5.2, 8.4_

  - [ ]* 15.4 Write unit tests for confirmation dialogs
    - Test dialog opening and closing
    - Test confirm and cancel actions
    - _Requirements: 5.2, 7.2, 8.4_

- [x] 16. Implement user feedback (loading, success, error messages)
  - [x] 16.1 Add loading indicators
    - Show spinner in image grid when loading images
    - Show spinner in sidebar when loading snapshots
    - Show spinner overlay during operations (create, revert, delete)
    - _Requirements: 8.1, 8.5_

  - [ ]* 16.2 Write property test for loading indicator display
    - **Property 21: Loading Indicator Display**
    - **Validates: Requirements 8.1, 8.5**

  - [x] 16.3 Implement success notifications
    - Use Angular Material Snackbar or similar
    - Display success message after create project, revert, download, delete operations
    - Auto-dismiss after 3-5 seconds
    - _Requirements: 4.5, 5.7, 7.5, 8.2_

  - [x] 16.4 Implement error notifications
    - Display error message when operations fail
    - Include error details from backend
    - Provide retry option where appropriate
    - _Requirements: 8.3_

  - [ ]* 16.5 Write property test for error message display
    - **Property 22: Error Message Display**
    - **Validates: Requirements 8.3**

- [x] 17. Implement create new project from snapshot flow
  - [x] 17.1 Create ProjectNameDialogComponent
    - Define input: defaultName
    - Define output: projectName
    - Create template with input field and buttons
    - Validate project name (non-empty, unique)
    - _Requirements: 4.2_

  - [x] 17.2 Integrate project creation flow
    - Open ProjectNameDialogComponent when user clicks "Create new project" in Use menu
    - Dispatch createProjectFromSnapshot action with snapshot ID and project name
    - Navigate to new project on success
    - _Requirements: 4.2, 4.3, 4.5_

  - [ ]* 17.3 Write unit tests for project creation flow
    - Test dialog opening and input validation
    - Test action dispatching
    - Test navigation on success
    - _Requirements: 4.2, 4.3, 4.5_

- [x] 18. Implement snapshot selection synchronization
  - [x] 18.1 Update loadSnapshotImages effect
    - Trigger when selectSnapshot action is dispatched
    - Load images for newly selected snapshot
    - Reset pagination to page 1
    - _Requirements: 2.3_

  - [ ]* 18.2 Write property test for snapshot selection synchronization
    - **Property 6: Snapshot Selection Synchronization**
    - **Validates: Requirements 2.3**

  - [x] 18.3 Implement selection fallback after deletion
    - When deleteSnapshot succeeds, check if deleted snapshot was selected
    - If yes, select most recent remaining snapshot
    - Dispatch selectSnapshot action with new snapshot ID
    - _Requirements: 7.6_

  - [ ]* 18.4 Write property test for selection fallback on deletion
    - **Property 20: Selection Fallback on Deletion**
    - **Validates: Requirements 7.6**

- [x] 19. Implement sidebar update after deletion
  - [x] 19.1 Update deleteSnapshot effect
    - After successful deletion, dispatch loadSnapshots action to refresh list
    - Update sidebar to remove deleted snapshot
    - _Requirements: 7.4_

  - [ ]* 19.2 Write property test for sidebar update after deletion
    - **Property 19: Sidebar Update After Deletion**
    - **Validates: Requirements 7.4**

- [x] 20. Add routing and navigation
  - [x] 20.1 Add route for snapshot list view
    - Add route in landingai-routing.module.ts: '/projects/:projectId/snapshots'
    - Configure route to load SnapshotListViewComponent
    - _Requirements: 1.1_

  - [x] 20.2 Add navigation from project page
    - Add "View Snapshots" button or link in project toolbar
    - Navigate to snapshot list view route
    - _Requirements: 1.1_

  - [ ]* 20.3 Write unit tests for routing
    - Test route configuration
    - Test navigation
    - _Requirements: 1.1_

- [x] 21. Implement complete image rendering
  - [x] 21.1 Verify ImageCardComponent displays all required data
    - Ensure thumbnail image is displayed
    - Ensure ground truth label overlay is displayed
    - Ensure filename is displayed
    - Ensure label information is displayed
    - _Requirements: 1.1, 1.5_

  - [ ]* 21.2 Write property test for complete image rendering
    - **Property 1: Complete Image Rendering**
    - **Validates: Requirements 1.1, 1.5**

- [x] 22. Add error handling and exception management
  - [x] 22.1 Create custom exception classes
    - SnapshotNotFoundException
    - SnapshotRevertException
    - ProjectCreationException
    - _Requirements: 5.8, 8.3_

  - [x] 22.2 Implement @ControllerAdvice for centralized exception handling
    - Handle custom exceptions and return appropriate HTTP status codes
    - Return structured ErrorResponse with error code, message, timestamp
    - Log exceptions with full context
    - _Requirements: 8.3_

  - [x] 22.3 Add frontend error handling
    - Handle HTTP errors in effects
    - Display user-friendly error messages
    - Log detailed errors to console
    - _Requirements: 8.3_

  - [ ]* 22.4 Write unit tests for error handling
    - Test exception throwing and catching
    - Test error response formatting
    - Test frontend error display
    - _Requirements: 5.8, 8.3_

- [ ] 23. Final checkpoint - Integration testing and polish
  - Run all unit tests and property tests (frontend and backend)
  - Perform manual end-to-end testing of all flows
  - Verify UI consistency with existing pages
  - Test error scenarios and edge cases
  - Ensure all confirmation dialogs work correctly
  - Verify loading indicators and notifications
  - Ask the user if questions arise

- [ ] 24. Documentation and cleanup
  - Add JSDoc comments to all TypeScript services and components
  - Add Javadoc comments to all Java classes and methods
  - Update API documentation with new endpoints
  - Remove any debug logging or commented code
  - _Requirements: All_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties across all inputs
- Unit tests validate specific examples, edge cases, and integration points
- The implementation reuses existing components (ImageGridComponent, ImageCardComponent, FilterPanelComponent) to maintain consistency
- All data-modifying operations use @Transactional to ensure atomicity and automatic rollback on errors
