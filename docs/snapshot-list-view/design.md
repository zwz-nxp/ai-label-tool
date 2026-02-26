# Design Document: Snapshot List View

## Overview

The snapshot list view feature provides a dedicated interface for viewing and managing dataset snapshots. The implementation follows a three-tier architecture with an Angular frontend, Java Spring Boot backend, and PostgreSQL database. The feature reuses existing components from the image upload page to maintain UI consistency and leverages the existing snapshot infrastructure.

The core functionality includes:
- Displaying snapshot images with ground truth labels in a paginated grid
- A collapsible sidebar for snapshot selection and metadata display
- Filter and sort capabilities using existing services
- Snapshot operations: create new project, revert to snapshot, download, and delete
- Comprehensive error handling and user feedback

## Architecture

### Component Structure

```
SnapshotListViewComponent (Main Container)
├── SnapshotToolbarComponent (Top toolbar with Manage button)
├── SnapshotSidebarComponent (Right sidebar - collapsible)
│   └── SnapshotCardComponent[] (Individual snapshot cards)
├── FilterPanelComponent (Reused from image-upload)
└── ImageGridComponent (Reused from image-upload)
    └── ImageCardComponent[] (Reused from image-upload)
```

### State Management (NgRx)

**State Shape:**
```typescript
interface SnapshotListState {
  snapshots: Snapshot[];
  selectedSnapshotId: number | null;
  images: ImageListItemDTO[];
  pagination: {
    currentPage: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  };
  filters: ImageFilterRequest;
  sortCriteria: SortCriteria;
  loading: {
    snapshots: boolean;
    images: boolean;
    operation: boolean;
  };
  error: string | null;
  sidebarCollapsed: boolean;
}
```

**Actions:**
- `loadSnapshots` - Fetch all snapshots for current project
- `selectSnapshot` - Change the currently selected snapshot
- `loadSnapshotImages` - Fetch images for selected snapshot
- `applyFilters` - Apply filter criteria to snapshot images
- `applySortCriteria` - Apply sort criteria to snapshot images
- `changePage` - Navigate to different page
- `createProjectFromSnapshot` - Create new project with snapshot data
- `revertToSnapshot` - Revert current project to snapshot state
- `downloadSnapshot` - Export snapshot data
- `deleteSnapshot` - Remove snapshot
- `toggleSidebar` - Collapse/expand sidebar

**Effects:**
- Handle async operations for all actions
- Coordinate multi-step operations (e.g., revert with backup)
- Dispatch success/error notifications

### Backend API Endpoints

**Existing Endpoints (to be used):**
- `GET /api/snapshots/project/{projectId}` - List all snapshots
- `GET /api/snapshots/{snapshotId}/images` - Get snapshot images (paginated)
- `POST /api/images/filter` - Apply filters to images
- `POST /api/images/sort` - Apply sort criteria

**New Endpoints (to be created):**
- `POST /api/snapshots/{snapshotId}/create-project` - Create new project from snapshot
- `POST /api/snapshots/{snapshotId}/revert` - Revert current project to snapshot
- `GET /api/snapshots/{snapshotId}/download` - Download snapshot dataset
- `DELETE /api/snapshots/{snapshotId}` - Delete snapshot

### Database Schema

The feature uses existing tables with a trigger-based snapshot architecture:

**Main Tables:**
- `la_snapshot` - Snapshot metadata (id, project_id, snapshot_name, description, created_by, created_at)

**Snapshot Data Tables (_ss suffix):**
When a snapshot is created, a database trigger (`tgf_do_snapshot`) automatically copies all project data into these tables with a `snapshot_id` reference:
- `la_project_class_ss` - Snapshot class data
- `la_project_tag_ss` - Snapshot tag data
- `la_project_split_ss` - Snapshot split data
- `la_project_metadata_ss` - Snapshot metadata fields
- `la_images_ss` - Snapshot image records
- `la_images_label_ss` - Snapshot image labels
- `la_images_tag_ss` - Snapshot image tags
- `la_images_metadata_ss` - Snapshot image metadata

**Key Architecture Points:**
1. Snapshot creation is handled by database triggers - the service only needs to insert into `la_snapshot` table
2. The trigger automatically copies all project data to `_ss` tables with the new snapshot_id
3. For operations like createProjectFromSnapshot and revertToSnapshot, the service reads from `_ss` tables
4. For deleteSnapshot, the service must delete from both `_ss` tables and the main `la_snapshot` table

## Components and Interfaces

### Frontend Components

#### SnapshotListViewComponent
**Responsibility:** Main container component that orchestrates the snapshot list view

**Inputs:** None (reads projectId from route)

**Outputs:** None

**Key Methods:**
- `ngOnInit()` - Initialize component, load snapshots
- `onSnapshotSelected(snapshotId: number)` - Handle snapshot selection
- `onFilterApplied(filters: ImageFilterRequest)` - Handle filter changes
- `onSortApplied(sort: SortCriteria)` - Handle sort changes
- `onPageChanged(page: number)` - Handle pagination

**Template Structure:**
```html
<div class="snapshot-list-view">
  <app-snapshot-toolbar
    [selectedSnapshot]="selectedSnapshot$ | async"
    (manageAction)="onManageAction($event)">
  </app-snapshot-toolbar>
  
  <div class="content-area">
    <div class="main-content">
      <app-filter-panel
        [filters]="filters$ | async"
        (filtersChanged)="onFilterApplied($event)">
      </app-filter-panel>
      
      <app-image-grid
        [images]="images$ | async"
        [pagination]="pagination$ | async"
        [loading]="loading$ | async"
        (pageChanged)="onPageChanged($event)">
      </app-image-grid>
    </div>
    
    <app-snapshot-sidebar
      [snapshots]="snapshots$ | async"
      [selectedId]="selectedSnapshotId$ | async"
      [collapsed]="sidebarCollapsed$ | async"
      (snapshotSelected)="onSnapshotSelected($event)"
      (toggleCollapse)="onToggleSidebar()">
    </app-snapshot-sidebar>
  </div>
</div>
```

#### SnapshotSidebarComponent
**Responsibility:** Display list of snapshots with metadata and handle selection

**Inputs:**
- `snapshots: Snapshot[]` - List of available snapshots
- `selectedId: number | null` - Currently selected snapshot ID
- `collapsed: boolean` - Sidebar collapse state

**Outputs:**
- `snapshotSelected: EventEmitter<number>` - Emits when snapshot is selected
- `toggleCollapse: EventEmitter<void>` - Emits when collapse button clicked

**Key Methods:**
- `selectSnapshot(id: number)` - Handle snapshot card click
- `toggleCollapse()` - Toggle sidebar collapsed state
- `isSelected(id: number): boolean` - Check if snapshot is selected

#### SnapshotCardComponent
**Responsibility:** Display individual snapshot metadata card

**Inputs:**
- `snapshot: Snapshot` - Snapshot data
- `selected: boolean` - Whether this snapshot is selected

**Outputs:**
- `clicked: EventEmitter<number>` - Emits snapshot ID when clicked

**Template Structure:**
```html
<div class="snapshot-card" [class.selected]="selected">
  <div class="snapshot-name">{{ snapshot.name }}</div>
  <div class="snapshot-metadata">
    <div class="metadata-item">
      <span class="label">Created:</span>
      <span class="value">{{ snapshot.createdAt | date }}</span>
    </div>
    <div class="metadata-item">
      <span class="label">Creator:</span>
      <span class="value">{{ snapshot.createdBy }}</span>
    </div>
    <div class="metadata-item">
      <span class="label">Images:</span>
      <span class="value">{{ snapshot.imageCount }}</span>
    </div>
    <div class="metadata-item">
      <span class="label">Classes:</span>
      <span class="value">{{ snapshot.classCount }}</span>
    </div>
  </div>
</div>
```

#### SnapshotToolbarComponent
**Responsibility:** Display toolbar with Use and Manage buttons

**Inputs:**
- `selectedSnapshot: Snapshot | null` - Currently selected snapshot

**Outputs:**
- `manageAction: EventEmitter<ManageAction>` - Emits manage action type

**Key Methods:**
- `onUseSnapshot(action: 'create' | 'revert')` - Handle Use button actions
- `onManageSnapshot(action: 'download' | 'delete')` - Handle Manage button actions

### Backend Services

#### SnapshotService (Java)
**Responsibility:** Business logic for snapshot operations

**Key Methods:**
```java
List<SnapshotDTO> getProjectSnapshots(Long projectId)
PaginatedResponse<ImageListItemDTO> getSnapshotImages(Long snapshotId, int page, int size)
ProjectDTO createProjectFromSnapshot(Long snapshotId, String newProjectName, String userId)
void revertProjectToSnapshot(Long projectId, Long snapshotId, String userId)
byte[] downloadSnapshotDataset(Long snapshotId)
void deleteSnapshot(Long snapshotId)
```

**Note on Snapshot Creation:**
Snapshot data capture is handled by database triggers. When a record is inserted into `la_snapshot`, the trigger `tgf_do_snapshot` automatically copies all project data to the `_ss` tables.

**createProjectFromSnapshot Implementation:**
```java
@Transactional
public ProjectDTO createProjectFromSnapshot(Long snapshotId, String newProjectName, String userId) {
    // 1. Validate snapshot exists
    Snapshot snapshot = snapshotRepository.findById(snapshotId)
        .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));
    
    // 2. Create new project entity
    Project newProject = createNewProject(snapshot.getProject(), newProjectName, userId);
    
    // 3. Copy data from _ss tables to new project tables
    // Build ID mappings for foreign key relationships
    Map<Long, Long> classIdMapping = copySnapshotProjectClasses(snapshotId, newProject.getId(), userId);
    Map<Long, Long> tagIdMapping = copySnapshotProjectTags(snapshotId, newProject.getId(), userId);
    Map<Long, Long> metadataIdMapping = copySnapshotProjectMetadata(snapshotId, newProject.getId(), userId);
    copySnapshotProjectSplits(snapshotId, newProject.getId(), classIdMapping, userId);
    Map<Long, Long> imageIdMapping = copySnapshotImages(snapshotId, newProject.getId(), userId);
    copySnapshotImageLabels(snapshotId, imageIdMapping, classIdMapping, userId);
    copySnapshotImageTags(snapshotId, imageIdMapping, tagIdMapping, userId);
    copySnapshotImageMetadata(snapshotId, imageIdMapping, metadataIdMapping, userId);
    
    return convertProjectToDTO(newProject);
}
```

**Revert Operation Implementation:**

**IMPORTANT:** The revert operation preserves original IDs from the snapshot. This is critical because:
- `la_images_file` table stores image binary data keyed by `image_id`
- If we generate new IDs, we lose the link to the actual image files
- All 8 tables (ProjectClass, ProjectTag, ProjectMetadata, ProjectSplit, Image, ImageLabel, ImageTag, ImageMetadata) are restored with their original IDs

```java
@Transactional
public void revertProjectToSnapshot(Long projectId, Long snapshotId, String userId) {
    // Step 1: Create backup snapshot of current state
    // This triggers the database trigger to capture current data
    Snapshot backupSnapshot = createBackupSnapshot(projectId, userId);
    
    try {
        // Step 2: Delete current project data (respecting FK constraints)
        // Order: ImageMetadata -> ImageTag -> ImageLabel -> Image -> ProjectSplit -> ProjectMetadata -> ProjectTag -> ProjectClass
        deleteProjectData(projectId);
        
        // Step 3: Restore snapshot data from _ss tables to project tables
        // PRESERVING ORIGINAL IDs to maintain link to la_images_file
        // Order: ProjectClass -> ProjectTag -> ProjectMetadata -> ProjectSplit -> Image -> ImageLabel -> ImageTag -> ImageMetadata
        restoreSnapshotData(projectId, snapshotId, userId);
        
    } catch (Exception e) {
        // Rollback handled by @Transactional
        throw new SnapshotRevertException("Failed to revert to snapshot", e);
    }
}

private Snapshot createBackupSnapshot(Long projectId, String userId) {
    // Create snapshot - trigger will auto-capture current data
    String backupName = "Backup before revert - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    SnapshotCreateRequest request = new SnapshotCreateRequest();
    request.setProjectId(projectId);
    request.setSnapshotName(backupName);
    request.setDescription("Automatic backup before revert operation");
    return createSnapshot(request, userId);
}

private void deleteProjectData(Long projectId) {
    // Delete in correct order to respect foreign key constraints
    imageMetadataRepository.deleteByImage_Project_Id(projectId);
    imageTagRepository.deleteByImage_Project_Id(projectId);
    imageLabelRepository.deleteByImage_Project_Id(projectId);
    imageRepository.deleteByProject_Id(projectId);
    projectSplitRepository.deleteByProject_Id(projectId);
    projectMetadataRepository.deleteByProject_Id(projectId);
    projectTagRepository.deleteByProject_Id(projectId);
    projectClassRepository.deleteByProject_Id(projectId);
}

private void restoreSnapshotData(Long projectId, Long snapshotId, String userId) {
    // Copy data from _ss tables to project tables with new IDs
    Map<Long, Long> classIdMapping = restoreSnapshotProjectClasses(snapshotId, projectId, userId);
    Map<Long, Long> tagIdMapping = restoreSnapshotProjectTags(snapshotId, projectId, userId);
    Map<Long, Long> metadataIdMapping = restoreSnapshotProjectMetadata(snapshotId, projectId, userId);
    restoreSnapshotProjectSplits(snapshotId, projectId, classIdMapping, userId);
    Map<Long, Long> imageIdMapping = restoreSnapshotImages(snapshotId, projectId, userId);
    restoreSnapshotImageLabels(snapshotId, imageIdMapping, classIdMapping, userId);
    restoreSnapshotImageTags(snapshotId, imageIdMapping, tagIdMapping, userId);
    restoreSnapshotImageMetadata(snapshotId, imageIdMapping, metadataIdMapping, userId);
}
```

**deleteSnapshot Implementation:**
```java
@Transactional
public void deleteSnapshot(Long snapshotId, String userId) {
    // 1. Validate snapshot exists
    if (!snapshotRepository.existsById(snapshotId)) {
        throw new SnapshotNotFoundException(snapshotId);
    }
    
    // 2. Delete from _ss tables first (no FK constraints on these)
    snapshotImageMetadataRepository.deleteBySnapshotId(snapshotId);
    snapshotImageTagRepository.deleteBySnapshotId(snapshotId);
    snapshotImageLabelRepository.deleteBySnapshotId(snapshotId);
    snapshotImageRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectSplitRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectMetadataRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectTagRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectClassRepository.deleteBySnapshotId(snapshotId);
    
    // 3. Delete the main snapshot record
    snapshotRepository.deleteById(snapshotId);
}
```

#### SnapshotController (Java)
**Responsibility:** REST API endpoints for snapshot operations

**Endpoints:**
```java
@GetMapping("/api/snapshots/project/{projectId}")
ResponseEntity<List<SnapshotDTO>> getProjectSnapshots(@PathVariable Long projectId)

@GetMapping("/api/snapshots/{snapshotId}/images")
ResponseEntity<PaginatedResponse<ImageListItemDTO>> getSnapshotImages(
    @PathVariable Long snapshotId,
    @RequestParam int page,
    @RequestParam int size)

@PostMapping("/api/snapshots/{snapshotId}/create-project")
ResponseEntity<ProjectDTO> createProjectFromSnapshot(
    @PathVariable Long snapshotId,
    @RequestBody CreateProjectRequest request)

@PostMapping("/api/snapshots/{snapshotId}/revert")
ResponseEntity<Void> revertToSnapshot(
    @PathVariable Long snapshotId,
    @RequestParam Long projectId)

@GetMapping("/api/snapshots/{snapshotId}/download")
ResponseEntity<byte[]> downloadSnapshot(@PathVariable Long snapshotId)

@DeleteMapping("/api/snapshots/{snapshotId}")
ResponseEntity<Void> deleteSnapshot(@PathVariable Long snapshotId)
```

### Frontend Services

#### SnapshotService (TypeScript)
**Responsibility:** HTTP client for snapshot API calls

**Key Methods:**
```typescript
getProjectSnapshots(projectId: number): Observable<Snapshot[]>
getSnapshotImages(snapshotId: number, page: number, size: number): Observable<PaginatedResponse<ImageListItemDTO>>
createProjectFromSnapshot(snapshotId: number, projectName: string): Observable<Project>
revertToSnapshot(snapshotId: number, projectId: number): Observable<void>
downloadSnapshot(snapshotId: number): Observable<Blob>
deleteSnapshot(snapshotId: number): Observable<void>
```

## Data Models

### TypeScript Models

```typescript
interface Snapshot {
  id: number;
  projectId: number;
  name: string;
  description: string;
  createdAt: Date;
  createdBy: string;
  imageCount: number;
  classCount: number;
}

interface SnapshotListState {
  snapshots: Snapshot[];
  selectedSnapshotId: number | null;
  images: ImageListItemDTO[];
  pagination: PaginationState;
  filters: ImageFilterRequest;
  sortCriteria: SortCriteria;
  loading: LoadingState;
  error: string | null;
  sidebarCollapsed: boolean;
}

interface PaginationState {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

interface LoadingState {
  snapshots: boolean;
  images: boolean;
  operation: boolean;
}

interface ManageAction {
  type: 'download' | 'delete';
  snapshotId: number;
}

interface CreateProjectRequest {
  snapshotId: number;
  projectName: string;
  userId: string;
}
```

### Java DTOs

```java
public class SnapshotDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
    private Integer imageCount;
    private Integer classCount;
}

public class CreateProjectRequest {
    private Long snapshotId;
    private String projectName;
    private String userId;
}

public class SnapshotRevertRequest {
    private Long projectId;
    private Long snapshotId;
    private String userId;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Complete Image Rendering
*For any* set of snapshot images with labels, the rendered output should contain image thumbnails, ground truth label overlays, filename, and label information for each image.
**Validates: Requirements 1.1, 1.5**

### Property 2: Pagination Activation
*For any* snapshot with total image count and page size, pagination controls should be enabled if and only if the total count exceeds the page size.
**Validates: Requirements 1.3**

### Property 3: Correct Page Subset
*For any* page number N and page size P, the returned images should be the subset from index (N-1)*P to min(N*P, totalCount).
**Validates: Requirements 1.4**

### Property 4: Complete Snapshot Display
*For any* project with N snapshots, the sidebar should render exactly N snapshot cards.
**Validates: Requirements 2.1**

### Property 5: Snapshot Metadata Completeness
*For any* snapshot, the rendered card should contain name, creation date, creator, image count, and class count.
**Validates: Requirements 2.2**

### Property 6: Snapshot Selection Synchronization
*For any* snapshot selection change, the displayed images should match the selected snapshot's image set.
**Validates: Requirements 2.3**

### Property 7: Selected Snapshot Highlighting
*For any* selected snapshot ID, the corresponding sidebar card should have the selected visual state applied.
**Validates: Requirements 2.4**

### Property 8: Filter Application Correctness
*For any* filter criteria applied to a snapshot's images, all returned images should satisfy the filter conditions.
**Validates: Requirements 3.1**

### Property 9: Sort Order Correctness
*For any* sort criteria applied to a snapshot's images, the returned images should be ordered according to the sort specification.
**Validates: Requirements 3.2**

### Property 10: Pagination Update on Filter
*For any* filter that reduces the result set size, the total page count should be recalculated as ceil(filteredCount / pageSize).
**Validates: Requirements 3.5**

### Property 11: Complete Project Data Copy
*For any* snapshot, creating a new project should copy all ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata records to the new project.
**Validates: Requirements 4.3, 4.4**

### Property 12: Backup Snapshot Creation
*For any* revert operation, a backup snapshot should be created before any project data is modified.
**Validates: Requirements 5.3**

### Property 13: Complete Backup Capture
*For any* project being backed up, the backup snapshot should contain all ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata records.
**Validates: Requirements 5.4**

### Property 14: Complete Data Cleanup
*For any* project being reverted, after backup creation, all ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata records should be deleted.
**Validates: Requirements 5.5**

### Property 15: Complete Data Restoration
*For any* snapshot being reverted to, all snapshot data should be inserted into the project's ProjectClass, ProjectTag, ProjectSplit, ProjectMetadata, Image, ImageLabel, ImageTag, and ImageMetadata tables.
**Validates: Requirements 5.6**

### Property 16: Revert Transaction Rollback
*For any* revert operation that encounters an error at any step, the project state should remain unchanged from its pre-revert state.
**Validates: Requirements 5.8**

### Property 17: Complete Snapshot Export
*For any* snapshot, the exported data should contain all images and their associated metadata (labels, tags, metadata fields).
**Validates: Requirements 6.3**

### Property 18: Snapshot Deletion Completeness
*For any* snapshot being deleted, after deletion, the snapshot and all associated data should not exist in the database.
**Validates: Requirements 7.3**

### Property 19: Sidebar Update After Deletion
*For any* deleted snapshot, the sidebar should not display that snapshot after deletion completes.
**Validates: Requirements 7.4**

### Property 20: Selection Fallback on Deletion
*For any* currently selected snapshot that is deleted, the system should automatically select the most recent remaining snapshot.
**Validates: Requirements 7.6**

### Property 21: Loading Indicator Display
*For any* long-running operation in progress, a loading indicator should be visible in the UI.
**Validates: Requirements 8.1, 8.5**

### Property 22: Error Message Display
*For any* operation that fails, an error message containing failure details should be displayed to the user.
**Validates: Requirements 8.3**

## Error Handling

### Frontend Error Handling

**Network Errors:**
- Display user-friendly error messages for failed API calls
- Provide retry options for transient failures
- Log detailed error information to console for debugging

**Validation Errors:**
- Validate user inputs before API calls (e.g., project name not empty)
- Display inline validation messages
- Prevent invalid operations from being submitted

**State Errors:**
- Handle missing or invalid snapshot selections gracefully
- Provide fallback behavior when expected data is unavailable
- Reset error state when user takes corrective action

**Error Message Examples:**
```typescript
const ERROR_MESSAGES = {
  LOAD_SNAPSHOTS_FAILED: 'Failed to load snapshots. Please try again.',
  LOAD_IMAGES_FAILED: 'Failed to load snapshot images. Please try again.',
  CREATE_PROJECT_FAILED: 'Failed to create project from snapshot. Please try again.',
  REVERT_FAILED: 'Failed to revert to snapshot. Your project data has not been changed.',
  DOWNLOAD_FAILED: 'Failed to download snapshot. Please try again.',
  DELETE_FAILED: 'Failed to delete snapshot. Please try again.',
  INVALID_PROJECT_NAME: 'Please enter a valid project name.',
  NO_SNAPSHOT_SELECTED: 'Please select a snapshot first.'
};
```

### Backend Error Handling

**Transaction Management:**
- Use `@Transactional` annotation for all data-modifying operations
- Ensure automatic rollback on exceptions
- Log transaction failures with full stack traces

**Validation:**
- Validate all input parameters (non-null, valid IDs, etc.)
- Throw `IllegalArgumentException` for invalid inputs
- Return appropriate HTTP status codes (400 for validation errors)

**Business Logic Errors:**
- Throw custom exceptions for business rule violations
- Use `@ControllerAdvice` for centralized exception handling
- Return structured error responses with error codes and messages

**Database Errors:**
- Handle constraint violations gracefully
- Provide meaningful error messages for foreign key violations
- Log SQL exceptions with query context

**Error Response Format:**
```java
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> details;
}
```

**Custom Exceptions:**
```java
public class SnapshotNotFoundException extends RuntimeException {
    public SnapshotNotFoundException(Long snapshotId) {
        super("Snapshot not found: " + snapshotId);
    }
}

public class SnapshotRevertException extends RuntimeException {
    public SnapshotRevertException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ProjectCreationException extends RuntimeException {
    public ProjectCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Confirmation Dialogs

**Destructive Operations:**
All destructive operations require explicit user confirmation:

1. **Delete Snapshot:**
   - Title: "Delete Snapshot?"
   - Message: "Are you sure you want to delete '[snapshot name]'? This action cannot be undone."
   - Buttons: "Cancel" (default), "Delete" (danger)

2. **Revert to Snapshot:**
   - Title: "Revert to Snapshot?"
   - Message: "This will replace your current project data with the snapshot '[snapshot name]'. Your current data will be backed up automatically. Do you want to continue?"
   - Buttons: "Cancel" (default), "Revert" (warning)

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests to ensure comprehensive coverage:

**Unit Tests** focus on:
- Specific examples of component behavior
- Integration between components
- Edge cases and error conditions
- UI interactions and state changes

**Property Tests** focus on:
- Universal properties that hold for all inputs
- Data integrity across operations
- Correctness of algorithms (pagination, filtering, sorting)
- Transaction behavior and rollback

Together, these approaches provide comprehensive coverage: unit tests catch concrete bugs in specific scenarios, while property tests verify general correctness across all possible inputs.

### Property-Based Testing

**Library:** For TypeScript/Angular frontend, use **fast-check**. For Java backend, use **jqwik**.

**Configuration:**
- Each property test must run a minimum of 100 iterations
- Each test must reference its design document property using a comment tag
- Tag format: `// Feature: snapshot-list-view, Property {number}: {property_text}`

**Example Property Test (TypeScript):**
```typescript
// Feature: snapshot-list-view, Property 3: Correct Page Subset
it('should return correct page subset for any page number and size', () => {
  fc.assert(
    fc.property(
      fc.array(fc.integer(), { minLength: 0, maxLength: 1000 }), // images
      fc.integer({ min: 1, max: 100 }), // pageSize
      fc.integer({ min: 1, max: 50 }), // pageNumber
      (images, pageSize, pageNumber) => {
        const result = paginateImages(images, pageNumber, pageSize);
        const expectedStart = (pageNumber - 1) * pageSize;
        const expectedEnd = Math.min(pageNumber * pageSize, images.length);
        const expected = images.slice(expectedStart, expectedEnd);
        expect(result).toEqual(expected);
      }
    ),
    { numRuns: 100 }
  );
});
```

**Example Property Test (Java):**
```java
// Feature: snapshot-list-view, Property 11: Complete Project Data Copy
@Property(tries = 100)
void shouldCopyAllSnapshotDataToNewProject(
    @ForAll @Size(min = 1, max = 50) List<ProjectClass> classes,
    @ForAll @Size(min = 1, max = 100) List<Image> images) {
    
    // Create snapshot with test data
    Snapshot snapshot = createTestSnapshot(classes, images);
    
    // Create project from snapshot
    Project newProject = snapshotService.createProjectFromSnapshot(
        snapshot.getId(), "Test Project", "testUser");
    
    // Verify all data was copied
    List<ProjectClass> copiedClasses = projectClassRepository
        .findByProjectId(newProject.getId());
    List<Image> copiedImages = imageRepository
        .findByProjectId(newProject.getId());
    
    assertThat(copiedClasses).hasSize(classes.size());
    assertThat(copiedImages).hasSize(images.size());
}
```

### Unit Testing

**Frontend Unit Tests (Jasmine/Karma):**
- Component rendering and lifecycle
- User interactions (clicks, form submissions)
- NgRx actions, reducers, and effects
- Service HTTP calls (mocked)
- Error handling and validation

**Backend Unit Tests (JUnit):**
- Controller endpoint behavior
- Service business logic
- Repository queries
- DTO mapping
- Exception handling

**Integration Tests:**
- End-to-end API flows
- Database transactions
- Multi-step operations (revert with backup)

### Test Coverage Goals

- Minimum 80% code coverage for all components and services
- 100% coverage for critical paths (revert operation, data copying)
- All error paths must have explicit tests
- All confirmation dialogs must have interaction tests

### Manual Testing Checklist

- [ ] Load snapshot list view with multiple snapshots
- [ ] Select different snapshots and verify images update
- [ ] Apply filters and verify results
- [ ] Apply sorts and verify order
- [ ] Navigate through pages
- [ ] Collapse and expand sidebar
- [ ] Create new project from snapshot
- [ ] Revert to snapshot (verify backup created)
- [ ] Download snapshot
- [ ] Delete snapshot (verify confirmation)
- [ ] Test error scenarios (network failures, invalid data)
- [ ] Verify loading indicators appear during operations
- [ ] Verify success/error messages display correctly
