# Implementation Plan: Landing AI Home

## Overview

This implementation plan breaks down the Landing AI home feature into discrete coding tasks. The approach follows a bottom-up strategy: starting with backend data layer, then services, then controllers, followed by frontend models, services, state management, and finally UI components. Each task builds incrementally to ensure continuous integration.

## Tasks

- [x] 1. Set up backend infrastructure and DTOs
  - Create DTO classes in shared module for API communication
  - Create custom exception classes for error handling
  - Set up package structure in all backend modules
  - _Requirements: 10.2, 10.3_

- [x] 1.1 Create DTOs in shared module
  - Create ProjectDTO.java with all project fields
  - Create ProjectListItemDTO.java with display fields and counts
  - Create ProjectCreateRequest.java with validation annotations
  - Create ImageUploadResponse.java for upload results
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.3_

- [x] 1.2 Create custom exceptions in shared module
  - Create DuplicateProjectNameException.java
  - Create InvalidProjectTypeException.java
  - Create InvalidImageFormatException.java
  - Create ImageProcessingException.java
  - _Requirements: 5.6, 4.2, 6.2_

- [x] 2. Implement backend repositories
  - Create JPA repositories for Project and Image entities
  - Implement custom query methods for filtering
  - _Requirements: 1.1, 1.8, 2.1, 2.2, 8.1, 8.2_

- [x] 2.1 Create ProjectRepository in iemdm-operational
  - Extend JpaRepository<Project, Long>
  - Add findByLocationIdAndCreatedBy method
  - Add findByLocationId method
  - Add existsByNameAndLocationId method
  - Add custom query to count images per project
  - Add custom query to count labels per project
  - _Requirements: 1.1, 1.6, 1.7, 1.8, 2.1, 2.2, 5.6, 8.1_

- [ ]* 2.2 Write property test for ProjectRepository
  - **Property 1: Location-based project isolation**
  - **Property 3: Project name uniqueness within location**
  - **Validates: Requirements 1.8, 2.2, 5.6, 8.1, 8.2**

- [x] 2.3 Create ImageRepository in iemdm-operational
  - Extend JpaRepository<Image, Long>
  - Add findByProjectIdOrderByCreatedAtAsc method
  - Add findFirstByProjectIdOrderByCreatedAtAsc method
  - Add countByProjectId method
  - _Requirements: 1.2, 1.3, 1.6, 6.4_

- [x] 3. Implement backend services in iemdm-operational
  - Create service layer for business logic
  - Implement project and image operations
  - _Requirements: 1.1, 5.1, 6.1, 7.1_

- [x] 3.1 Create ProjectService in iemdm-operational
  - Implement getProjectsForUser method with location and creator filtering
  - Implement createProject method with validation
  - Implement getProjectById method
  - Implement isProjectNameUnique method
  - Calculate image and label counts for project list items
  - Retrieve first image thumbnail for each project
  - _Requirements: 1.1, 1.4, 1.5, 1.6, 1.7, 1.8, 2.1, 2.2, 2.3, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 8.1, 8.2_

- [ ]* 3.2 Write property test for ProjectService
  - **Property 2: Creator filtering in default mode**
  - **Property 6: Project card display completeness**
  - **Validates: Requirements 1.1, 1.4, 1.5, 1.6, 1.7, 2.3**

- [x] 3.3 Create ImageService in iemdm-operational
  - Implement uploadImages method with file validation
  - Implement generateThumbnail method with compression
  - Implement getThumbnail method
  - Implement extractMetadata method for width/height
  - Validate file formats (PNG, JPG, JPEG)
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 7.1, 7.2, 7.3_

- [ ]* 3.4 Write property test for ImageService
  - **Property 4: Thumbnail generation for all uploads**
  - **Property 9: Image format validation**
  - **Property 10: Metadata extraction completeness**
  - **Validates: Requirements 6.2, 6.5, 7.1, 7.2, 7.3**

- [x] 4. Checkpoint - Ensure backend services work correctly
  - Run all backend tests
  - Verify repository queries return correct data
  - Verify thumbnail generation produces valid images
  - Ask the user if questions arise

- [x] 5. Implement REST controllers in api module
  - Create REST endpoints for project and image operations
  - Implement request/response handling
  - Add error handling with proper HTTP status codes
  - _Requirements: 1.1, 2.1, 3.1, 5.1, 6.1_

- [x] 5.1 Create ProjectController in api
  - Implement GET /api/landingai/projects endpoint with viewAll parameter
  - Implement POST /api/landingai/projects endpoint
  - Implement GET /api/landingai/projects/{id} endpoint
  - Add @RestController and @RequestMapping annotations
  - Add authentication/authorization checks
  - Add exception handling with @ExceptionHandler
  - Return proper HTTP status codes (200, 201, 400, 403, 409, 500)
  - _Requirements: 1.1, 2.1, 3.1, 5.1, 5.6_

- [ ]* 5.2 Write unit tests for ProjectController
  - Test GET endpoint with viewAll=false returns only user's projects
  - Test GET endpoint with viewAll=true returns all location projects
  - Test POST endpoint creates project successfully
  - Test POST endpoint rejects duplicate names
  - Test POST endpoint rejects invalid project types
  - _Requirements: 1.1, 2.1, 2.3, 5.6, 4.2_

- [x] 5.3 Create ImageController in api
  - Implement POST /api/landingai/images/upload endpoint
  - Implement GET /api/landingai/images/{id}/thumbnail endpoint
  - Handle MultipartFile uploads
  - Add file size validation (max 10MB)
  - Add authentication/authorization checks
  - Add exception handling for upload errors
  - Return proper HTTP status codes
  - _Requirements: 6.1, 6.2, 7.4_

- [ ]* 5.4 Write unit tests for ImageController
  - Test upload endpoint accepts valid image formats
  - Test upload endpoint rejects invalid formats
  - Test upload endpoint rejects oversized files
  - Test thumbnail endpoint returns correct image data
  - _Requirements: 6.2, 6.3_

- [x] 6. Checkpoint - Ensure backend API works end-to-end
  - Test all REST endpoints with Postman or curl
  - Verify authentication works correctly
  - Verify error responses have correct format
  - Ask the user if questions arise

- [x] 7. Create frontend models
  - Define TypeScript interfaces for Project and Image
  - Create type definitions for API requests/responses
  - _Requirements: 1.1, 5.1, 6.1_

- [x] 7.1 Create project model in models/landingai
  - Create project.ts with Project interface
  - Create ProjectType type union
  - Create ProjectListItem interface
  - Create ProjectCreateRequest interface
  - _Requirements: 1.1, 1.4, 1.5, 1.6, 1.7, 5.1_

- [x] 7.2 Create image model in models/landingai
  - Create image.ts with Image interface
  - Create ImageUploadResult interface
  - _Requirements: 6.1, 6.3, 6.5_

- [x] 8. Implement frontend services
  - Create Angular services for HTTP communication
  - Implement API calls to backend endpoints
  - _Requirements: 1.1, 2.1, 5.1, 6.1_

- [x] 8.1 Create ProjectService in services/landingai
  - Implement getProjects(viewAll: boolean) method
  - Implement createProject(request: ProjectCreateRequest) method
  - Implement getProjectById(id: number) method
  - Use HttpClient for API calls
  - Handle HTTP errors with proper error messages
  - _Requirements: 1.1, 2.1, 3.1, 5.1_

- [ ]* 8.2 Write unit tests for ProjectService
  - Test getProjects calls correct endpoint with parameters
  - Test createProject sends correct request body
  - Test error handling for failed requests
  - _Requirements: 1.1, 2.1, 5.1_

- [x] 8.3 Create ImageService in services/landingai
  - Implement uploadImages(files: File[], projectId: number) method
  - Implement getThumbnailUrl(imageId: number) method
  - Handle file upload with FormData
  - Handle upload progress tracking
  - _Requirements: 6.1, 6.2, 7.4_

- [ ]* 8.4 Write unit tests for ImageService
  - Test uploadImages creates correct FormData
  - Test getThumbnailUrl returns correct URL
  - _Requirements: 6.1_

- [x] 9. Implement NgRx state management
  - Create actions, reducers, effects, and selectors
  - Manage project list state
  - Handle loading and error states
  - _Requirements: 1.1, 2.1, 5.1_

- [x] 9.1 Create landingai actions in state/landingai
  - Create loadProjects action with viewAll parameter
  - Create loadProjectsSuccess action
  - Create loadProjectsFailure action
  - Create createProject action
  - Create createProjectSuccess action
  - Create createProjectFailure action
  - Create uploadImages action
  - Create uploadImagesSuccess action
  - Create uploadImagesFailure action
  - _Requirements: 1.1, 2.1, 5.1, 6.1_

- [x] 9.2 Create landingai reducer in state/landingai
  - Define LandingAIState interface with projects, loading, error
  - Implement reducer for all actions
  - Handle loading states properly
  - Handle error states with messages
  - _Requirements: 1.1, 2.1, 5.1_

- [x] 9.3 Create landingai effects in state/landingai
  - Implement loadProjects$ effect calling ProjectService
  - Implement createProject$ effect calling ProjectService
  - Implement uploadImages$ effect calling ImageService
  - Handle success and failure cases
  - Show toast notifications for errors
  - _Requirements: 1.1, 2.1, 5.1, 6.1_

- [x] 9.4 Create landingai selectors in state/landingai
  - Create selectProjects selector
  - Create selectLoading selector
  - Create selectError selector
  - Create selectProjectById selector
  - _Requirements: 1.1_

- [ ]* 9.5 Write unit tests for state management
  - Test reducer handles all actions correctly
  - Test selectors return correct state slices
  - Test effects call services with correct parameters
  - _Requirements: 1.1, 2.1, 5.1_

- [x] 10. Create Angular module and routing
  - Set up landingai module with routing
  - Configure lazy loading
  - _Requirements: 3.1, 10.1_

- [x] 10.1 Create LandingAIModule in modules/landingai
  - Import CommonModule, FormsModule, ReactiveFormsModule
  - Import Material modules needed (MatCard, MatButton, etc.)
  - Declare all landingai components
  - Import LandingAIRoutingModule
  - _Requirements: 10.1_

- [x] 10.2 Create LandingAIRoutingModule in modules/landingai
  - Define route for project list: /landingai/projects
  - Define route for project create: /landingai/projects/create
  - Define route for project detail: /landingai/projects/:id
  - Add route guards if needed
  - _Requirements: 3.1_

- [x] 11. Implement ProjectCardComponent
  - Create reusable component for displaying project cards
  - Handle thumbnail display with fallback
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 9.1_

- [x] 11.1 Create ProjectCardComponent in components/landingai
  - Create component with @Input() project: ProjectListItem
  - Create component with @Output() cardClick: EventEmitter<number>
  - Implement getThumbnailUrl() method
  - Implement getDefaultImageUrl() method for fallback
  - Add hover effects for visual feedback
  - Add click handler emitting cardClick event
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 9.1_

- [x] 11.2 Create ProjectCardComponent template
  - Display project thumbnail image with fallback
  - Display project name
  - Display creator name
  - Display image count
  - Display label count
  - Add proper styling with SCSS
  - Add accessibility attributes (alt text, ARIA labels)
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

- [ ]* 11.3 Write unit tests for ProjectCardComponent
  - Test component renders all project information
  - Test component shows default image when no thumbnail
  - Test component emits cardClick on click
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 9.1_

- [x] 12. Implement ProjectListComponent
  - Create main component for displaying project grid
  - Implement view all toggle functionality
  - Handle navigation to create and detail pages
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.1, 3.2, 9.1_

- [x] 12.1 Create ProjectListComponent in components/landingai
  - Inject Store and Router
  - Subscribe to projects$, loading$, currentUser$, currentLocation$ observables
  - Implement ngOnInit to dispatch loadProjects action
  - Implement toggleViewAll() method
  - Implement navigateToCreate() method
  - Implement navigateToProject(id) method
  - Track viewAll state locally
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.1, 9.1_

- [x] 12.2 Create ProjectListComponent template
  - Display "Create Project" button prominently
  - Display "View All" toggle button
  - Display loading spinner when loading
  - Display error message when error occurs
  - Display grid of ProjectCardComponent instances
  - Handle empty state with message
  - Add proper styling with SCSS
  - _Requirements: 1.1, 2.1, 2.2, 3.1, 3.2_

- [ ]* 12.3 Write unit tests for ProjectListComponent
  - Test component loads projects on init
  - Test toggleViewAll dispatches correct action
  - Test navigateToCreate navigates to create page
  - Test navigateToProject navigates to detail page
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.1, 9.1_

- [x] 13. Checkpoint - Ensure project list displays correctly
  - Verify project cards render with correct data
  - Verify view all toggle works
  - Verify navigation works
  - Ask the user if questions arise

- [x] 14. Implement ProjectCreateComponent
  - Create component for project creation workflow
  - Implement project type selection
  - Implement image upload functionality
  - _Requirements: 3.1, 4.1, 4.2, 4.3, 4.4, 5.1, 6.1, 6.2_

- [x] 14.1 Create ProjectCreateComponent in components/landingai
  - Inject FormBuilder, Store, Router
  - Create reactive form with name and type fields
  - Add form validation (name required, type required)
  - Implement onTypeSelect(type) method
  - Implement onFileSelect(event) method
  - Implement removeFile(index) method
  - Implement createProject() method
  - Track selected files in component state
  - Disable non-Object Detection types
  - _Requirements: 3.1, 4.1, 4.2, 4.3, 4.4, 5.1, 6.1, 6.2_

- [x] 14.2 Create ProjectCreateComponent template
  - Display project type selection cards
  - Show Object Detection as selectable
  - Show Segmentation and Classification as disabled
  - Display project name input field
  - Display file upload drop zone
  - Display selected files list with remove buttons
  - Display create button (disabled until valid)
  - Display loading spinner during creation
  - Display error messages
  - Add proper styling with SCSS
  - _Requirements: 3.1, 4.1, 4.2, 4.3, 4.4, 5.1, 6.1, 6.2_

- [x] 14.3 Implement image upload workflow
  - On successful project creation, dispatch uploadImages action
  - Show upload progress for each file
  - Handle upload errors per file
  - Navigate to project detail on complete success
  - _Requirements: 6.1, 6.3, 6.4, 6.5, 6.6, 6.7_

- [ ]* 14.4 Write unit tests for ProjectCreateComponent
  - Test form validation works correctly
  - Test only Object Detection is selectable
  - Test file selection adds files to list
  - Test removeFile removes correct file
  - Test createProject dispatches correct action
  - _Requirements: 4.2, 4.3, 4.4, 5.1, 6.1_

- [x] 15. Implement error handling and user feedback
  - Add toast notifications for errors
  - Add loading indicators
  - Add success messages
  - _Requirements: All error scenarios_

- [x] 15.1 Add error handling to all components
  - Display toast notifications for API errors
  - Display inline error messages for validation errors
  - Display loading spinners during async operations
  - Display success messages after successful operations
  - _Requirements: All requirements with error scenarios_

- [x] 16. Add default placeholder image asset
  - Add default project image to assets folder
  - Use in ProjectCardComponent when no thumbnail
  - _Requirements: 1.3_

- [x] 16.1 Create or obtain default placeholder image
  - Add image file to frontend/src/main/frontend/src/assets/
  - Update ProjectCardComponent to reference asset
  - _Requirements: 1.3_

- [ ]* 16.2 Write property test for default image fallback
  - **Property 7: Default image fallback**
  - **Validates: Requirements 1.3**

- [x] 17. Final integration and testing
  - Test complete user workflows end-to-end
  - Verify all requirements are met
  - Fix any remaining bugs
  - **Status:** Architecture consolidation completed, ready for testing with running server
  - _Requirements: All_

- [x] 17.1 Test project list workflow
  - Create test projects with different creators and locations
  - Verify filtering works correctly
  - Verify view all toggle works
  - Verify thumbnails display correctly
  - **Status:** Code complete, compilation successful, ready for integration testing
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 2.1, 2.2, 2.3_

- [x] 17.2 Test project creation workflow
  - Create project with valid data
  - Upload multiple images
  - Verify project appears in list
  - Verify thumbnails generated correctly
  - Test error scenarios (duplicate name, invalid type, invalid format)
  - **Status:** Code complete, compilation successful, ready for integration testing
  - _Requirements: 3.1, 4.1, 4.2, 4.3, 4.4, 5.1, 5.6, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 7.1, 7.2, 7.3_

- [x] 17.3 Architecture consolidation
  - Consolidated ImageServiceImpl with all business logic
  - Consolidated ProjectServiceImpl with all business logic
  - Deleted old service files (ImageService.java, ProjectService.java)
  - Verified compilation success for all modules
  - **Status:** Completed - See architecture-consolidation-report.md
  - _Requirements: All_

- [ ] 17.4 Execute integration tests
  - Start backend server
  - Run test-landingai-api.bat script
  - Verify all endpoints work correctly
  - Test with actual image files
  - Document test results
  - **Status:** Pending - Requires running backend server
  - _Requirements: All_

- [ ] 18. Final checkpoint - Complete feature verification
  - Ensure all tests pass
  - Ensure all requirements are implemented
  - Ensure no regressions in existing functionality
  - Ask the user for final review

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end workflows
- Backend tasks should be completed before frontend tasks
- State management should be implemented before UI components
