# Implementation Plan: Model Parameter Configuration CRUD

## Overview

This implementation plan breaks down the Model Parameter Configuration CRUD feature into discrete, incremental tasks. The approach follows a bottom-up strategy: starting with backend data layer and validation, then backend operational and API layers, followed by frontend state management, and finally frontend UI components. Each task builds on previous work, ensuring continuous integration and early validation through both unit tests and property-based tests.

## Tasks

- [x] 1. Set up backend DTOs and validation
  - Create ModelParamDTO, ModelParamCreateRequest, and ModelParamUpdateRequest classes in shared module
  - Add Jakarta Bean Validation annotations (@NotBlank, @Size, etc.)
  - Create custom JSON validator for parameters field
  - Create custom enum validator for model_type field
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 1.1 Write unit tests for DTO validation
  - Test validation annotations work correctly
  - Test JSON validator with valid and invalid JSON
  - Test enum validator with valid and invalid model types
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 1.2 Write property test for JSON validation
  - **Property 21: JSON syntax validation**
  - **Validates: Requirements 7.5**

- [x] 2. Implement operational layer service
  - Create ModelParamOperationalService in iemdm-operational module
  - Implement getModelParamsByLocation method using ModelParamRepository
  - Implement getModelParamsByLocationAndType method
  - Implement searchModelParamsByName method
  - Implement getModelParamById method with error handling for not found
  - Implement createModelParam method with automatic field setting (location_id, created_at, created_by)
  - Implement updateModelParam method preserving audit fields
  - Implement deleteModelParam method
  - Add entity-to-DTO mapping logic
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 3.3, 3.4, 3.5, 4.2, 4.3, 5.2, 6.1_

- [ ]* 2.1 Write unit tests for operational service
  - Test each service method with mocked repository
  - Test error handling for not found scenarios
  - Test audit field preservation on update
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 3.3, 3.4, 3.5, 4.2, 4.3, 5.2_

- [ ]* 2.2 Write property test for create operation
  - **Property 7: Create operation with automatic fields**
  - **Validates: Requirements 3.2, 3.3, 3.4, 3.5**

- [ ]* 2.3 Write property test for update operation
  - **Property 10: Update operation preserves audit fields**
  - **Validates: Requirements 4.2, 4.3**

- [ ]* 2.4 Write property test for location filtering
  - **Property 2: Location-based filtering**
  - **Validates: Requirements 2.1, 10.1**

- [ ]* 2.5 Write property test for model type filtering
  - **Property 4: Model type filtering**
  - **Validates: Requirements 2.3**

- [ ]* 2.6 Write property test for search by model name
  - **Property 5: Search by model name**
  - **Validates: Requirements 2.4**

- [x] 3. Implement operational layer REST controller
  - Create ModelParamOperationalController in iemdm-operational module
  - Implement GET /operational/landingai/model-params endpoint
  - Implement GET /operational/landingai/model-params with modelType filter
  - Implement GET /operational/landingai/model-params/search endpoint
  - Implement GET /operational/landingai/model-params/{id} endpoint
  - Implement POST /operational/landingai/model-params endpoint
  - Implement PUT /operational/landingai/model-params/{id} endpoint
  - Implement DELETE /operational/landingai/model-params/{id} endpoint
  - Add @ControllerAdvice exception handler for validation and not found errors
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [ ]* 3.1 Write integration tests for operational controller
  - Test all endpoints with TestRestTemplate
  - Test validation error responses (400)
  - Test not found error responses (404)
  - Use TestContainers for database integration
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_

- [x] 4. Checkpoint - Ensure operational layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement API layer service interface and REST client
  - Create ModelParamService interface in api module
  - Create ModelParamServiceREST implementation in api module
  - Implement all methods using RestTemplate to call operational endpoints
  - Build URLs with UriComponentsBuilder
  - Handle HTTP responses and convert to DTOs
  - Add error handling for REST client exceptions
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1_

- [ ]* 5.1 Write unit tests for REST client service
  - Test REST calls with MockRestServiceServer
  - Test URL building with query parameters
  - Test error handling for network failures
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_

- [x] 6. Implement API layer controller
  - Create ModelParamApiController in api module
  - Implement GET /api/landingai/model-params endpoint
  - Implement GET /api/landingai/model-params with filters
  - Implement GET /api/landingai/model-params/search endpoint
  - Implement GET /api/landingai/model-params/{id} endpoint
  - Implement POST /api/landingai/model-params endpoint
  - Implement PUT /api/landingai/model-params/{id} endpoint
  - Implement DELETE /api/landingai/model-params/{id} endpoint
  - Extract locationId and userId from request parameters
  - Delegate to ModelParamService
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1_

- [ ]* 6.1 Write unit tests for API controller
  - Test all endpoints with mocked service
  - Test parameter extraction
  - Test response status codes
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_

- [x] 7. Checkpoint - Ensure backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Create frontend TypeScript models and interfaces
  - Create ModelParam interface matching backend DTO
  - Create ModelParamCreateRequest interface
  - Create ModelParamUpdateRequest interface
  - Create ModelParamFilters interface for state
  - Define ModelType enum with three values
  - _Requirements: 2.3, 3.2, 4.2_

- [x] 9. Implement NgRx state management
  - [x] 9.1 Create model-param state structure
    - Define ModelParamState interface
    - Create initial state with empty arrays and null values
    - _Requirements: 2.1, 2.3, 2.4_
  
  - [x] 9.2 Create model-param actions
    - Create load actions (load, success, failure)
    - Create filter actions (filterByModelType, searchByModelName)
    - Create CRUD actions (create, update, delete with success/failure)
    - Create selection actions (select, clear)
    - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_
  
  - [x] 9.3 Create model-param reducer
    - Handle load actions to update modelParams array
    - Handle filter actions to update filters state
    - Handle CRUD success actions to update state
    - Handle loading and error state updates
    - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_
  
  - [x] 9.4 Create model-param effects
    - Implement loadModelParams$ effect calling HTTP service
    - Implement createModelParam$ effect with success notification
    - Implement updateModelParam$ effect with success notification
    - Implement deleteModelParam$ effect with success notification
    - Implement locationChange$ effect to reload data
    - Handle errors and dispatch failure actions
    - _Requirements: 2.1, 2.2, 3.2, 3.6, 4.2, 4.4, 5.2, 5.3, 9.1, 9.2_
  
  - [x] 9.5 Create model-param selectors
    - Create selectAllModelParams selector
    - Create selectFilteredModelParams selector applying filters
    - Create selectLoading selector
    - Create selectError selector
    - Create selectSelectedModelParam selector
    - _Requirements: 2.1, 2.3, 2.4_

- [ ]* 9.6 Write unit tests for NgRx state
  - Test reducer with all actions
  - Test selectors with various state shapes
  - Test effects with mocked services
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_

- [ ]* 9.7 Write property test for location change reload
  - **Property 3: Location change triggers reload**
  - **Validates: Requirements 2.2, 10.3**

- [x] 10. Create HTTP service for model params
  - Create ModelParamHttpService in Angular
  - Implement getModelParams(locationId) method
  - Implement getModelParamsByType(locationId, modelType) method
  - Implement searchModelParams(locationId, searchTerm) method
  - Implement getModelParamById(id) method
  - Implement createModelParam(request, locationId, userId) method
  - Implement updateModelParam(id, request, userId) method
  - Implement deleteModelParam(id, userId) method
  - Use HttpClient with proper error handling
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1_

- [ ]* 10.1 Write unit tests for HTTP service
  - Test all methods with HttpClientTestingModule
  - Test URL construction
  - Test error handling
  - _Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2_

- [x] 11. Update navigation menu to add Model Param Config item
  - Modify app.component.html to convert "Owl Vision ADC" button to dropdown menu
  - Add mat-menu with two items: "Project List" and "Model Param Config"
  - Set routerLink for "Project List" to /landingai/projects
  - Set routerLink for "Model Param Config" to /landingai/model-params
  - Ensure proper ordering (Project List first, Model Param Config second)
  - _Requirements: 1.1, 1.2, 1.3_

- [ ]* 11.1 Write unit test for navigation menu
  - Test menu items are rendered
  - Test menu item order
  - Test navigation on click
  - _Requirements: 1.1, 1.2, 1.3_

- [ ]* 11.2 Write property test for navigation
  - **Property 1: Navigation to model params route**
  - **Validates: Requirements 1.2**

- [x] 12. Add route to landingai routing module
  - Add route for /landingai/model-params in landingai-routing.module.ts
  - Set component to ModelParamListComponent
  - _Requirements: 1.2_

- [x] 13. Implement ModelParamListComponent
  - [x] 13.1 Create component structure and template
    - Create ModelParamListComponent with Material table
    - Add header with title and create button
    - Add filter controls (model type dropdown, search input)
    - Add table columns: model name, model type, created at, created by, actions
    - Add action buttons (view, edit, delete) in each row
    - Add loading spinner and error message displays
    - Add empty state message when no records
    - Add paginator for table
    - _Requirements: 2.1, 2.3, 2.4, 2.5_
  
  - [x] 13.2 Implement component logic
    - Inject Store and subscribe to selectors
    - Subscribe to currentLocation$ and dispatch load on changes
    - Implement filter methods (onModelTypeChange, onSearchChange)
    - Implement action handlers (onCreate, onEdit, onView, onDelete)
    - Implement retry logic for errors
    - Handle loading and error states
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 4.1, 5.1, 6.1, 9.3, 9.4_
  
  - [x] 13.3 Add component styles
    - Style header and action buttons
    - Style filter controls
    - Style table and paginator
    - Style loading and error states
    - Follow existing Material UI patterns
    - _Requirements: 2.1, 2.5_

- [ ]* 13.4 Write unit tests for list component
  - Test component initialization
  - Test location change triggers reload
  - Test filter interactions
  - Test action button clicks
  - Test loading and error states
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ]* 13.5 Write property test for location-based filtering
  - **Property 2: Location-based filtering**
  - **Validates: Requirements 2.1, 10.1**

- [ ]* 13.6 Write property test for display fields
  - **Property 6: Display required fields**
  - **Validates: Requirements 2.5**

- [x] 14. Implement ModelParamFormDialogComponent
  - [x] 14.1 Create dialog structure and template
    - Create dialog component with Material form
    - Add form fields: model name (input), model type (select), parameters (textarea)
    - Add validation error displays below each field
    - Add JSON validation indicator for parameters field
    - Add dialog actions: Cancel and Save buttons
    - Disable Save button when form is invalid
    - _Requirements: 3.1, 4.1, 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.3_
  
  - [x] 14.2 Implement form logic and validation
    - Create reactive form with FormBuilder
    - Add validators: required, maxLength(50) for model name
    - Add custom JSON validator for parameters field
    - Add real-time JSON validation with visual feedback
    - Pre-populate form for edit mode
    - Implement onSubmit to dispatch create or update action
    - Check for selected location and show error if none
    - _Requirements: 3.1, 3.2, 4.1, 4.2, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1, 8.2, 8.3, 8.4_
  
  - [x] 14.3 Add dialog styles
    - Style form fields and labels
    - Style validation error messages
    - Style JSON validation indicators
    - Style dialog actions
    - _Requirements: 3.1, 4.1, 8.2, 8.3_

- [ ]* 14.4 Write unit tests for form dialog
  - Test form initialization
  - Test validation rules
  - Test JSON validation
  - Test form submission
  - Test edit mode pre-population
  - _Requirements: 3.1, 3.2, 4.1, 4.2, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1_

- [ ]* 14.5 Write property test for empty model name validation
  - **Property 17: Empty model name validation**
  - **Validates: Requirements 7.1**

- [ ]* 14.6 Write property test for model name length validation
  - **Property 18: Model name length validation**
  - **Validates: Requirements 7.2**

- [ ]* 14.7 Write property test for model type required validation
  - **Property 19: Model type required validation**
  - **Validates: Requirements 7.3**

- [ ]* 14.8 Write property test for model type enum validation
  - **Property 20: Model type enum validation**
  - **Validates: Requirements 7.4**

- [ ]* 14.9 Write property test for JSON validation feedback
  - **Property 23: Real-time JSON validation with feedback**
  - **Validates: Requirements 8.1, 8.2, 8.3, 8.4**

- [ ]* 14.10 Write property test for location required validation
  - **Property 22: Location required validation**
  - **Validates: Requirements 7.6**

- [x] 15. Implement ModelParamDetailDialogComponent
  - [x] 15.1 Create dialog structure and template
    - Create dialog component with read-only display
    - Display all fields: id, location_id, model_name, model_type, parameters, created_at, created_by
    - Format JSON parameters with proper indentation
    - Add close button
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [x] 15.2 Implement dialog logic
    - Accept model param data via MAT_DIALOG_DATA
    - Format JSON using JSON.stringify with indentation
    - Format dates for display
    - _Requirements: 6.1, 6.2_
  
  - [x] 15.3 Add dialog styles
    - Style field labels and values
    - Style formatted JSON display (monospace font)
    - Style close button
    - _Requirements: 6.1, 6.2_

- [ ]* 15.4 Write unit tests for detail dialog
  - Test dialog displays all fields
  - Test JSON formatting
  - Test close button
  - _Requirements: 6.1, 6.2, 6.3_

- [ ]* 15.5 Write property test for detail view
  - **Property 15: Detail view completeness**
  - **Validates: Requirements 6.1**

- [ ]* 15.6 Write property test for JSON formatting
  - **Property 16: JSON formatting for readability**
  - **Validates: Requirements 6.2**

- [x] 16. Implement ModelParamDeleteDialogComponent
  - [x] 16.1 Create dialog structure and template
    - Create confirmation dialog component
    - Display warning message with model name
    - Add dialog actions: Cancel and Delete buttons
    - Style Delete button as warn color
    - _Requirements: 5.1_
  
  - [x] 16.2 Implement dialog logic
    - Accept model param data via MAT_DIALOG_DATA
    - Return true on confirm, false on cancel
    - _Requirements: 5.1, 5.2, 5.4_

- [ ]* 16.3 Write unit tests for delete dialog
  - Test dialog displays model name
  - Test confirm returns true
  - Test cancel returns false
  - _Requirements: 5.1, 5.2, 5.4_

- [ ]* 16.4 Write property test for delete cancellation
  - **Property 14: Delete cancellation preserves record**
  - **Validates: Requirements 5.4**

- [x] 17. Wire up dialog interactions in list component
  - Update onCreate to open ModelParamFormDialogComponent in create mode
  - Update onEdit to open ModelParamFormDialogComponent in edit mode with data
  - Update onView to open ModelParamDetailDialogComponent with data
  - Update onDelete to open ModelParamDeleteDialogComponent
  - Handle dialog close results and dispatch appropriate actions
  - _Requirements: 3.1, 3.2, 4.1, 4.2, 5.1, 5.2, 6.1_

- [ ]* 17.1 Write integration tests for dialog interactions
  - Test create dialog flow
  - Test edit dialog flow
  - Test view dialog flow
  - Test delete dialog flow
  - _Requirements: 3.1, 3.2, 4.1, 4.2, 5.1, 5.2, 6.1_

- [ ]* 17.2 Write property test for post-creation list refresh
  - **Property 8: Post-creation list refresh**
  - **Validates: Requirements 3.6**

- [ ]* 17.3 Write property test for edit form population
  - **Property 9: Edit form population**
  - **Validates: Requirements 4.1**

- [ ]* 17.4 Write property test for post-update list refresh
  - **Property 11: Post-update list refresh**
  - **Validates: Requirements 4.4**

- [ ]* 17.5 Write property test for delete operation
  - **Property 12: Delete operation removes record**
  - **Validates: Requirements 5.2**

- [ ]* 17.6 Write property test for post-deletion list refresh
  - **Property 13: Post-deletion list refresh**
  - **Validates: Requirements 5.3**

- [x] 18. Implement notification and feedback mechanisms
  - Add MatSnackBar service injection in effects
  - Show success notifications on create, update, delete success
  - Show error notifications on operation failures
  - Configure notification duration and position
  - _Requirements: 9.1, 9.2_

- [ ]* 18.1 Write property test for operation feedback
  - **Property 24: Operation feedback notifications**
  - **Validates: Requirements 9.1, 9.2**

- [x] 19. Add loading indicators and error handling
  - Ensure loading spinner shows during data fetch
  - Ensure loading state shows during create/update/delete
  - Add error message display with retry button
  - Handle network errors with appropriate messages
  - _Requirements: 9.3, 9.4_

- [ ]* 19.1 Write property test for loading state
  - **Property 25: Loading state indicator**
  - **Validates: Requirements 9.3**

- [ ]* 19.2 Write property test for error recovery
  - **Property 26: Network error recovery**
  - **Validates: Requirements 9.4**

- [x] 20. Handle no location selected state
  - Check if location is selected before loading data
  - Display message prompting user to select location when none selected
  - Disable create button when no location selected
  - _Requirements: 7.6, 10.2_

- [~]* 20.1 Write property test for no location state
  - **Property 22: Location required validation** (frontend validation)
  - **Validates: Requirements 10.2**

- [x] 21. Final checkpoint - End-to-end testing
  - Ensure all tests pass
  - Manually test complete CRUD flow
  - Test navigation integration
  - Test location filtering
  - Test all validation scenarios
  - Test error handling and recovery
  - Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- The implementation follows a bottom-up approach: backend first, then frontend state, then UI
- All components follow existing patterns in the Owl Vision ADC application
