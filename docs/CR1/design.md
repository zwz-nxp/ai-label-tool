# Design Document: Landing AI Home

## Overview

The Landing AI Home feature provides a web-based interface for managing AI/ML projects. It consists of two main views: a project list page displaying project cards with thumbnails and metadata, and a project creation page for initializing new Object Detection projects with image uploads. The system follows a three-tier architecture with Angular 19 frontend, Spring Boot 3 backend, and PostgreSQL database.

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Angular 19 Frontend                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Components  │  │   Services   │  │  NgRx State  │      │
│  │  - Project   │  │  - Project   │  │  - Project   │      │
│  │    List      │  │    Service   │  │    State     │      │
│  │  - Project   │  │  - Image     │  │  - Image     │      │
│  │    Create    │  │    Service   │  │    State     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot 3 Backend                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controllers  │  │   Services   │  │ Repositories │      │
│  │  - Project   │  │  - Project   │  │  - Project   │      │
│  │    REST API  │  │    Service   │  │    Repo      │      │
│  │  - Image     │  │  - Image     │  │  - Image     │      │
│  │    REST API  │  │    Service   │  │    Repo      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ JPA/Hibernate
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                       │
│              la_projects  │  la_images                       │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

**Frontend (Angular 19):**
```
frontend/src/main/frontend/src/app/
├── components/
│   └── landingai/
│       ├── project-list/
│       │   ├── project-list.component.ts
│       │   ├── project-list.component.html
│       │   └── project-list.component.scss
│       ├── project-card/
│       │   ├── project-card.component.ts
│       │   ├── project-card.component.html
│       │   └── project-card.component.scss
│       └── project-create/
│           ├── project-create.component.ts
│           ├── project-create.component.html
│           └── project-create.component.scss
├── services/
│   └── landingai/
│       ├── project.service.ts
│       └── image.service.ts
├── models/
│   └── landingai/
│       ├── project.ts
│       └── image.ts
├── modules/
│   └── landingai/
│       ├── landingai.module.ts
│       └── landingai-routing.module.ts
└── state/
    └── landingai/
        ├── landingai.actions.ts
        ├── landingai.reducer.ts
        ├── landingai.effects.ts
        └── landingai.selectors.ts
```

**Backend (Spring Boot 3):**
```
backend/
├── api/src/main/java/com/nxp/iemdm/
│   └── controller/landingai/
│       ├── ProjectController.java
│       └── ImageController.java
├── iemdm-operational/src/main/java/com/nxp/iemdm/operational/
│   ├── repository/landingai/
│   │   ├── ProjectRepository.java
│   │   └── ImageRepository.java
│   └── service/landingai/
│       ├── ProjectService.java
│       └── ImageService.java
├── service/iemdm-services/src/main/java/com/nxp/iemdm/services/
│   └── landingai/
│       ├── ProjectBusinessService.java
│       └── ImageProcessingService.java
├── shared/src/main/java/com/nxp/iemdm/shared/
│   ├── dto/landingai/
│   │   ├── ProjectDTO.java
│   │   ├── ProjectListItemDTO.java
│   │   ├── ProjectCreateRequest.java
│   │   └── ImageUploadResponse.java
│   └── exception/landingai/
│       ├── DuplicateProjectNameException.java
│       ├── InvalidProjectTypeException.java
│       ├── InvalidImageFormatException.java
│       └── ImageProcessingException.java
└── data-model/src/main/java/com/nxp/iemdm/
    └── model/landingai/
        ├── Project.java (existing)
        └── Image.java (existing)
```

## Components and Interfaces

### Frontend Components

#### ProjectListComponent
**Responsibility:** Display grid of project cards with filtering and navigation

**Inputs:**
- None (uses NgRx state)

**Outputs:**
- Navigation to project detail or create page

**Key Methods:**
```typescript
loadProjects(viewAll: boolean): void
toggleViewAll(): void
navigateToCreate(): void
navigateToProject(projectId: number): void
```

**State Dependencies:**
- projects$: Observable<ProjectListItem[]>
- loading$: Observable<boolean>
- currentUser$: Observable<User>
- currentLocation$: Observable<Location>

#### ProjectCardComponent
**Responsibility:** Display individual project card with thumbnail and metadata

**Inputs:**
```typescript
@Input() project: ProjectListItem
```

**Outputs:**
```typescript
@Output() cardClick: EventEmitter<number>
```

**Template Structure:**
```html
<div class="project-card" (click)="onCardClick()">
  <img [src]="getThumbnailUrl()" [alt]="project.name">
  <div class="project-info">
    <h3>{{ project.name }}</h3>
    <p class="creator">Created by {{ project.createdBy }}</p>
    <div class="stats">
      <span>{{ project.imageCount }} images</span>
      <span>{{ project.labelCount }} labels</span>
    </div>
  </div>
</div>
```

#### ProjectCreateComponent
**Responsibility:** Handle project creation workflow with type selection and image upload

**Form Model:**
```typescript
projectForm = {
  name: string,
  type: 'Object Detection' | 'Segmentation' | 'Classification',
  images: File[]
}
```

**Key Methods:**
```typescript
onTypeSelect(type: string): void
onFileSelect(event: Event): void
removeFile(index: number): void
createProject(): void
uploadImages(projectId: number): void
```

### Backend REST APIs

#### ProjectController

**Base Path:** `/api/landingai/projects`

**Endpoints:**

1. **GET /api/landingai/projects**
   - Description: Get projects for current user and location
   - Query Parameters:
     - `viewAll`: boolean (default: false)
   - Response: `List<ProjectListItemDTO>`
   - Security: Requires authentication

2. **POST /api/landingai/projects**
   - Description: Create new project
   - Request Body: `ProjectCreateRequest`
   - Response: `ProjectDTO`
   - Security: Requires authentication

3. **GET /api/landingai/projects/{id}**
   - Description: Get project details
   - Path Variable: `id` (Long)
   - Response: `ProjectDTO`
   - Security: Requires authentication

#### ImageController

**Base Path:** `/api/landingai/images`

**Endpoints:**

1. **POST /api/landingai/images/upload**
   - Description: Upload images to project
   - Request: `MultipartFile[]` + `projectId` (Long)
   - Response: `List<ImageUploadResponse>`
   - Security: Requires authentication

2. **GET /api/landingai/images/{id}/thumbnail**
   - Description: Get image thumbnail
   - Path Variable: `id` (Long)
   - Response: `byte[]` (image/jpeg)
   - Security: Requires authentication

### Service Layer

#### ProjectService

**Methods:**

```java
List<ProjectListItemDTO> getProjectsForUser(String userId, Long locationId, boolean viewAll)
ProjectDTO createProject(ProjectCreateRequest request, String userId, Long locationId)
ProjectDTO getProjectById(Long id)
boolean isProjectNameUnique(String name, Long locationId)
```

**Business Logic:**
- Filter projects by location and optionally by creator
- Validate project name uniqueness within location
- Calculate image and label counts for project cards
- Retrieve first image thumbnail for project display

#### ImageService

**Methods:**

```java
List<ImageUploadResponse> uploadImages(List<MultipartFile> files, Long projectId, String userId)
byte[] generateThumbnail(MultipartFile file)
byte[] getThumbnail(Long imageId)
ImageMetadata extractMetadata(MultipartFile file)
```

**Business Logic:**
- Validate image file types (PNG, JPG, JPEG)
- Extract image dimensions (width, height)
- Generate compressed thumbnails (max 200x200px)
- Store images with metadata
- Associate images with projects

## Data Models

### Database Schema

**la_projects table** (existing):
```sql
CREATE TABLE la_projects (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  status VARCHAR(20),
  type VARCHAR(20),
  model_name VARCHAR(36),
  location_id BIGINT NOT NULL REFERENCES locations(id),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(36)
);

CREATE INDEX idx_la_projects_location ON la_projects(location_id);
CREATE INDEX idx_la_projects_created_by ON la_projects(created_by);
```

**la_images table** (existing):
```sql
CREATE TABLE la_images (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES la_projects(id) ON DELETE CASCADE,
  file_name VARCHAR(255),
  file_path VARCHAR(500),
  file_url VARCHAR(500),
  file_size BIGINT,
  width INTEGER,
  height INTEGER,
  split VARCHAR(10),
  is_no_class BOOLEAN,
  thumbnail_image BYTEA,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(36)
);

CREATE INDEX idx_la_images_project ON la_images(project_id);
```

**la_project_class table** (existing - for labels):
```sql
CREATE TABLE la_project_class (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES la_projects(id) ON DELETE CASCADE,
  class_name VARCHAR(100),
  description VARCHAR(100),
  color_code VARCHAR(7),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(36)
);

CREATE INDEX idx_la_project_class_project ON la_project_class(project_id);
```

**la_images_label table** (existing - for image annotations):
```sql
CREATE TABLE la_images_label (
  id BIGSERIAL PRIMARY KEY,
  image_id BIGINT NOT NULL REFERENCES la_images(id) ON DELETE CASCADE,
  class_id BIGINT NOT NULL REFERENCES la_project_class(id),
  position TEXT,
  confidence_rate INTEGER,
  annotation_type VARCHAR(10),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(36)
);

CREATE INDEX idx_la_images_label_image ON la_images_label(image_id);
CREATE INDEX idx_la_images_label_class ON la_images_label(class_id);
```

### DTOs

#### ProjectListItemDTO
```java
public class ProjectListItemDTO {
  private Long id;
  private String name;
  private String type;
  private String createdBy;
  private Instant createdAt;
  private Integer imageCount;
  private Integer labelCount;
  private byte[] firstImageThumbnail;
}
```

#### ProjectCreateRequest
```java
public class ProjectCreateRequest {
  @NotBlank
  private String name;
  
  @NotBlank
  private String type; // Must be "Object Detection"
}
```

#### ImageUploadResponse
```java
public class ImageUploadResponse {
  private Long id;
  private String fileName;
  private Long fileSize;
  private Integer width;
  private Integer height;
  private boolean success;
  private String errorMessage;
}
```

### Frontend Models

#### Project Model
```typescript
export interface Project {
  id: number;
  name: string;
  status: string;
  type: ProjectType;
  modelName: string;
  locationId: number;
  location?: Location; // Added: Project has ManyToOne with Location
  createdAt: Date;
  createdBy: string;
  
  // Related objects (optional, loaded when needed)
  projectClasses?: ProjectClass[];
  projectTags?: ProjectTag[];
  projectMetadata?: ProjectMetadata[];
  // Removed: images and trainingRecords - not in JPA Project entity
}

export interface Location {
  id: number;
  name: string;
  code: string;
  // Add other Location fields as needed
}

export type ProjectType = 'Object Detection' | 'Segmentation' | 'Classification';

export interface ProjectListItem {
  id: number;
  name: string;
  type: ProjectType;
  createdBy: string;
  createdAt: Date;
  imageCount: number;
  labelCount: number;
  thumbnailUrl: string;
}

export interface ProjectClass {
  id: number;
  projectId: number;
  project?: Project;
  className: string;
  description: string;
  colorCode: string;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectTag {
  id: number;
  projectId: number;
  project?: Project;
  name: string;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectMetadata {
  id: number;
  projectId: number;
  project?: Project;
  name: string;
  type: string; // TEXT, NUMBER, BOOLEAN
  valueFrom: string; // PREDEFINED, INPUT
  predefinedValues: string;
  multipleValues: boolean;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectSplit {
  id: number;
  projectId: number;
  project?: Project;
  trainRatio: number;
  devRatio: number;
  testRatio: number;
  classId?: number;
  projectClass?: ProjectClass;
  createdAt: Date;
  createdBy: string;
}
```

#### Image Model
```typescript
export interface Image {
  id: number;
  projectId: number;
  project?: Project;
  fileName: string;
  filePath: string;
  fileUrl: string;
  fileSize: number;
  width: number;
  height: number;
  split: string; // training/dev/test
  isNoClass: boolean;
  thumbnailImage?: string; // Base64 encoded or URL
  createdAt: Date;
  createdBy: string;
  
  // Related objects (optional, loaded when needed)
  labels?: ImageLabel[];
  tags?: ImageTag[];
  metadata?: ImageMetadata[];
}

export interface ImageLabel {
  id: number;
  imageId: number;
  image?: Image;
  classId: number;
  projectClass?: ProjectClass;
  position: string; // JSON string (Yolo/Coco format)
  confidenceRate: number;
  annotationType: string; // Ground Truth, Prediction
  createdAt: Date;
  createdBy: string;
}

export interface ImageTag {
  id: number;
  imageId: number;
  image?: Image;
  tagId: number;
  projectTag?: ProjectTag; // Changed from 'tag' to 'projectTag' to match JPA
  createdAt: Date;
  createdBy: string;
}

export interface ImageMetadata {
  id: number;
  imageId: number;
  image?: Image;
  metadataId: number;
  projectMetadata?: ProjectMetadata; // Changed from 'metadata' to 'projectMetadata' to match JPA
  value: string;
  createdAt: Date;
  createdBy: string;
}

export interface ImageUploadResult {
  id: number;
  fileName: string;
  fileSize: number;
  width: number;
  height: number;
  success: boolean;
  errorMessage?: string;
}
```

#### Training and Model Interfaces
```typescript
export interface TrainingRecord {
  id: number;
  projectId: number;
  project?: Project;
  status: string; // pending, complete
  modelAlias: string;
  trackId: string;
  epochs: number;
  modelSize: string;
  transformParam: string; // JSON
  augmentationParam: string; // JSON
  creditConsumption: string; // JSON
  trainingCount: number;
  devCount: number;
  testCount: number;
  startedAt: Date;
  completedAt: Date;
  createdBy: string;
  
  // 1:1 relationship - one training record has one model
  model?: Model;
}

export interface Model {
  id: number;
  projectId: number;
  trainingRecordId: number;
  trainingRecord?: TrainingRecord;
  modelAlias: string;
  trackId: string;
  modelVersion: string;
  f1Rate: number;
  precisionRate: number;
  recallRate: number;
  imageCount: number;
  labelCount: number;
  isFavorite: boolean;
  createdAt: Date;
  createdBy: string;
  
  // Related objects
  confidentialReports?: ConfidentialReport[];
}

export interface ConfidentialReport {
  id: number;
  modelId: number;
  model?: Model;
  trainingCorrectRate: number; // Changed from Long to number (Integer in JPA)
  devCorrectRate: number;
  testCorrectRate: number;
  confidenceThreshold: number;
  createdAt: Date;
  createdBy: string;
}

export interface Snapshot {
  id: number;
  projectId: number;
  project?: Project; // Added: Snapshot has ManyToOne with Project
  snapshotName: string;
  description: string;
  createdBy: string;
  createdAt: Date;
}
  description: string;
  createdBy: string;
  createdAt: Date;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Location-based project isolation
*For any* user with a specific location, querying projects should return only projects belonging to that location, regardless of view mode.
**Validates: Requirements 1.8, 2.2, 8.1, 8.2**

### Property 2: Creator filtering in default mode
*For any* user querying projects in default mode (viewAll=false), all returned projects should have created_by matching the user's identifier.
**Validates: Requirements 1.1, 2.3**

### Property 3: Project name uniqueness within location
*For any* location, attempting to create two projects with the same name should result in the second creation failing with an error.
**Validates: Requirements 5.6**

### Property 4: Thumbnail generation for all uploads
*For any* successfully uploaded image, the database record should contain a non-null thumbnail_image byte array.
**Validates: Requirements 7.1, 7.2, 7.3**

### Property 5: Image-project association
*For any* uploaded image, the image record should have a project_id that references an existing project in la_projects.
**Validates: Requirements 6.4**

### Property 6: Project card display completeness
*For any* project displayed in the list, the project card should show name, creator, image count, and label count.
**Validates: Requirements 1.4, 1.5, 1.6, 1.7**

### Property 7: Default image fallback
*For any* project with zero images, the project card should display a default placeholder image rather than null or broken image.
**Validates: Requirements 1.3**

### Property 8: Object Detection type restriction
*For any* project creation request with type other than "Object Detection", the system should reject the request.
**Validates: Requirements 4.2, 4.3, 5.4**

### Property 9: Image format validation
*For any* file upload, if the file format is not PNG, JPG, or JPEG, the upload should fail with an appropriate error message.
**Validates: Requirements 6.2**

### Property 10: Metadata extraction completeness
*For any* successfully uploaded image, the database record should contain file_name, file_size, width, and height values.
**Validates: Requirements 6.5**

## Error Handling

### Frontend Error Handling

**Network Errors:**
- Display toast notification with retry option
- Maintain form state for retry
- Log errors to console for debugging

**Validation Errors:**
- Display inline validation messages
- Prevent form submission until valid
- Highlight invalid fields

**Upload Errors:**
- Display per-file error messages
- Allow removal of failed uploads
- Continue with successful uploads

### Backend Error Handling

**Exception Types:**

1. **DuplicateProjectNameException**
   - HTTP Status: 409 Conflict
   - Message: "Project name already exists in this location"

2. **InvalidProjectTypeException**
   - HTTP Status: 400 Bad Request
   - Message: "Only Object Detection projects are currently supported"

3. **InvalidImageFormatException**
   - HTTP Status: 400 Bad Request
   - Message: "Only PNG, JPG, and JPEG formats are supported"

4. **ImageProcessingException**
   - HTTP Status: 500 Internal Server Error
   - Message: "Failed to process image: {details}"

5. **UnauthorizedAccessException**
   - HTTP Status: 403 Forbidden
   - Message: "You do not have permission to access this resource"

**Error Response Format:**
```json
{
  "timestamp": "2026-01-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Project name already exists in this location",
  "path": "/api/landingai/projects"
}
```

## Testing Strategy

### Unit Testing

**Frontend Unit Tests (Jasmine/Karma):**
- Component rendering with mock data
- Service HTTP calls with HttpClientTestingModule
- State management actions and reducers
- Form validation logic
- Error handling scenarios

**Backend Unit Tests (JUnit 5 + Mockito):**
- Service layer business logic
- Repository query methods
- DTO mapping and validation
- Thumbnail generation algorithm
- Exception handling

### Property-Based Testing

**Testing Framework:** JUnit 5 with jqwik for Java, fast-check for TypeScript

**Configuration:** Minimum 100 iterations per property test

**Property Test Examples:**

1. **Location Isolation Property Test**
   - Generate random projects with different locations
   - Query with random user location
   - Assert all results match the query location

2. **Creator Filtering Property Test**
   - Generate random projects with different creators
   - Query with viewAll=false and random user
   - Assert all results match the user's identifier

3. **Thumbnail Generation Property Test**
   - Generate random valid image files
   - Upload and retrieve from database
   - Assert thumbnail_image is non-null and valid JPEG

4. **Name Uniqueness Property Test**
   - Generate random project names
   - Attempt duplicate creation in same location
   - Assert second creation fails

### Integration Testing

**API Integration Tests:**
- Full request/response cycle testing
- Database transaction verification
- File upload and storage
- Authentication and authorization

**End-to-End Tests (Cypress/Playwright):**
- Complete user workflows
- Project list navigation
- Project creation with image upload
- View all toggle functionality

### Test Tags

All property-based tests must include a comment tag:
```java
// Feature: landing-ai-home, Property 1: Location-based project isolation
```

```typescript
// Feature: landing-ai-home, Property 3: Project name uniqueness within location
```

## Implementation Notes

### Image Storage Strategy

For MVP, images will be stored in the database as byte arrays. Future enhancements may include:
- Cloud storage (S3, Azure Blob)
- CDN integration for thumbnails
- Lazy loading and pagination

### Thumbnail Generation

Use Java ImageIO or similar library:
- Target size: 200x200px (maintain aspect ratio)
- Compression: JPEG quality 0.7
- Format: Always JPEG for consistency

### Security Considerations

- Validate file sizes (max 10MB per image)
- Scan uploaded files for malware
- Sanitize file names
- Implement rate limiting on uploads
- Verify user has access to location

### Performance Optimization

- Implement pagination for project list (future)
- Cache thumbnail images in browser
- Use database indexes on location_id and created_by
- Lazy load project images
- Compress API responses with gzip

### Accessibility

- Provide alt text for all images
- Keyboard navigation support
- ARIA labels for interactive elements
- Screen reader friendly error messages
- High contrast mode support
