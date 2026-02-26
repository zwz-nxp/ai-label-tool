# Design Document: Model Parameter Configuration CRUD

## Overview

The Model Parameter Configuration CRUD feature provides a comprehensive interface for managing AI model parameters within the Owl Vision ADC application. This feature follows the existing architectural patterns of the application, implementing a full-stack solution with Angular frontend, Spring Boot backend, and PostgreSQL database.

The design leverages the existing `la_model_param` table and `ModelParamRepository`, extending the application with new REST endpoints, Angular components, and NgRx state management. The feature integrates seamlessly with the existing location-based filtering mechanism and navigation structure.

## Architecture

### System Architecture

The feature follows a layered architecture consistent with the existing application:

```
┌─────────────────────────────────────────────────────────────┐
│                     Angular Frontend                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Components  │  │  NgRx Store  │  │   Services   │      │
│  │  - List View │  │  - State     │  │  - HTTP      │      │
│  │  - Form      │  │  - Actions   │  │  - Data      │      │
│  │  - Dialogs   │  │  - Effects   │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                    HTTP REST API
                            │
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Backend                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ REST         │  │ Service      │  │ Repository   │      │
│  │ Controllers  │  │ Layer        │  │ (JPA)        │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                      PostgreSQL
                            │
                   ┌────────────────┐
                   │ la_model_param │
                   └────────────────┘
```

### Navigation Integration

The feature adds a new menu item to the existing navigation structure:

```
Owl Vision ADC (Dropdown)
├── Project List
└── Model Param Config (NEW)
```

The navigation will be implemented by converting the existing single button into a dropdown menu using Angular Material's `mat-menu` component.

### Routing Structure

New route: `/landingai/model-params`

This route will be added to the `landingai-routing.module.ts` file and will load the new `ModelParamListComponent`.

## Components and Interfaces

### Frontend Components

#### 1. ModelParamListComponent

**Purpose**: Main container component for displaying and managing model parameters

**Responsibilities**:
- Display list of model parameters in a Material table
- Provide filtering by model type
- Provide search by model name
- Handle location changes from topbar
- Dispatch NgRx actions for CRUD operations
- Show loading states and error messages

**Template Structure**:
```
┌─────────────────────────────────────────────────────┐
│ Header: "Model Parameter Configurations"            │
│ [Create Button]                                      │
├─────────────────────────────────────────────────────┤
│ Filters:                                             │
│ [Model Type Dropdown] [Search Input]                │
├─────────────────────────────────────────────────────┤
│ Material Table:                                      │
│ | Model Name | Model Type | Created At | Actions | │
│ | ...        | ...        | ...        | [E][D]  | │
├─────────────────────────────────────────────────────┤
│ Paginator                                            │
└─────────────────────────────────────────────────────┘
```

**Key Properties**:
- `modelParams$: Observable<ModelParam[]>` - List of model parameters from store
- `loading$: Observable<boolean>` - Loading state
- `error$: Observable<string | null>` - Error state
- `currentLocation$: Observable<Location | null>` - Current location
- `modelTypeFilter: string` - Selected model type filter
- `searchTerm: string` - Search input value

#### 2. ModelParamFormDialogComponent

**Purpose**: Dialog component for creating and editing model parameters

**Responsibilities**:
- Display form with all model parameter fields
- Validate input according to requirements
- Validate JSON syntax in parameters field
- Submit create/update requests
- Show validation errors

**Form Fields**:
- Model Name (text input, required, max 50 chars)
- Model Type (dropdown, required, options: Object Detection, Classification, Segmentation)
- Parameters (textarea with JSON validation, required)

**Validation Rules**:
- Model name: required, max 50 characters
- Model type: required, must be one of three valid types
- Parameters: required, must be valid JSON

#### 3. ModelParamDetailDialogComponent

**Purpose**: Dialog component for viewing model parameter details

**Responsibilities**:
- Display all fields in read-only format
- Format JSON parameters for readability
- Provide close button

#### 4. ModelParamDeleteDialogComponent

**Purpose**: Confirmation dialog for delete operations

**Responsibilities**:
- Display confirmation message with model name
- Provide cancel and confirm buttons
- Return confirmation result

### Backend Components

The backend follows a two-layer architecture:
1. **API Layer** (`code/backend/api`): REST controllers that handle HTTP requests
2. **Operational Layer** (`code/backend/iemdm-operational`): Business logic and data processing

The API layer calls the Operational layer via REST client services.

#### 1. ModelParamApiController (API Layer)

**Location**: `code/backend/api/src/main/java/com/nxp/iemdm/controller/landingai/ModelParamApiController.java`

**Purpose**: REST API controller that handles HTTP requests and delegates to operational layer

**Endpoints**:

```java
GET    /api/landingai/model-params?locationId={id}
       - Get all model parameters for a location
       - Returns: List<ModelParamDTO>

GET    /api/landingai/model-params?locationId={id}&modelType={type}
       - Get filtered model parameters
       - Returns: List<ModelParamDTO>

GET    /api/landingai/model-params/search?locationId={id}&modelName={name}
       - Search model parameters by name
       - Returns: List<ModelParamDTO>

GET    /api/landingai/model-params/{id}
       - Get single model parameter by ID
       - Returns: ModelParamDTO

POST   /api/landingai/model-params?locationId={id}&userId={user}
       - Create new model parameter
       - Body: ModelParamCreateRequest
       - Returns: ModelParamDTO

PUT    /api/landingai/model-params/{id}?userId={user}
       - Update existing model parameter
       - Body: ModelParamUpdateRequest
       - Returns: ModelParamDTO

DELETE /api/landingai/model-params/{id}?userId={user}
       - Delete model parameter
       - Returns: 204 No Content
```

**Responsibilities**:
- Handle HTTP request/response
- Extract parameters from request
- Call ModelParamService (interface) which delegates to ModelParamServiceREST
- Handle exceptions and return appropriate HTTP status codes

**Error Handling**:
- 400 Bad Request: Invalid input, validation errors
- 404 Not Found: Model parameter not found
- 500 Internal Server Error: Database or server errors

#### 2. ModelParamService (Interface)

**Location**: `code/backend/api/src/main/java/com/nxp/iemdm/service/ModelParamService.java`

**Purpose**: Service interface defining model parameter operations

**Methods**:
```java
List<ModelParamDTO> getModelParamsByLocation(Long locationId);
List<ModelParamDTO> getModelParamsByLocationAndType(Long locationId, String modelType);
List<ModelParamDTO> searchModelParamsByName(Long locationId, String modelName);
ModelParamDTO getModelParamById(Long id);
ModelParamDTO createModelParam(ModelParamCreateRequest request, Long locationId, String userId);
ModelParamDTO updateModelParam(Long id, ModelParamUpdateRequest request, String userId);
void deleteModelParam(Long id, String userId);
```

#### 3. ModelParamServiceREST (API Layer Implementation)

**Location**: `code/backend/api/src/main/java/com/nxp/iemdm/service/rest/landingai/ModelParamServiceREST.java`

**Purpose**: REST client service that calls the operational layer endpoints

**Responsibilities**:
- Implement ModelParamService interface
- Make HTTP calls to operational layer using RestTemplate
- Build URLs with query parameters
- Handle HTTP responses and errors
- Convert responses to DTOs

**Example Implementation Pattern**:
```java
@Service
public class ModelParamServiceREST implements ModelParamService {
  
  private final RestTemplate restTemplate;
  private final String operationalServiceUri;
  
  @Override
  public List<ModelParamDTO> getModelParamsByLocation(Long locationId) {
    String url = UriComponentsBuilder
        .fromHttpUrl(operationalServiceUri + "/operational/landingai/model-params")
        .queryParam("locationId", locationId)
        .toUriString();
    
    ResponseEntity<ModelParamDTO[]> response = 
        restTemplate.getForEntity(url, ModelParamDTO[].class);
    
    return Arrays.asList(response.getBody());
  }
}
```

#### 4. ModelParamOperationalService (Operational Layer)

**Location**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/ModelParamOperationalService.java`

**Purpose**: Business logic layer for model parameter operations

**Responsibilities**:
- Implement business logic and validation
- Interact with ModelParamRepository (JPA)
- Transform entities to DTOs
- Validate model name length and presence
- Validate model type against allowed values
- Validate JSON syntax in parameters field
- Validate location exists
- Set created_by and created_at on creation
- Preserve audit fields on update

**Methods**:
```java
List<ModelParamDTO> getModelParamsByLocation(Long locationId);
List<ModelParamDTO> getModelParamsByLocationAndType(Long locationId, String modelType);
List<ModelParamDTO> searchModelParamsByName(Long locationId, String modelName);
ModelParamDTO getModelParamById(Long id);
ModelParamDTO createModelParam(ModelParamCreateRequest request, Long locationId, String userId);
ModelParamDTO updateModelParam(Long id, ModelParamUpdateRequest request, String userId);
void deleteModelParam(Long id, String userId);
```

#### 5. ModelParamOperationalController (Operational Layer)

**Location**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/controller/landingai/ModelParamOperationalController.java`

**Purpose**: REST controller in operational layer that exposes endpoints for the API layer

**Endpoints**: Mirror the API layer endpoints but under `/operational` path

**Responsibilities**:
- Handle HTTP requests from API layer
- Call ModelParamOperationalService
- Return responses to API layer

#### Data Flow

```
Frontend (Angular)
    ↓ HTTP Request
API Layer: ModelParamApiController
    ↓ calls
API Layer: ModelParamServiceREST (implements ModelParamService)
    ↓ HTTP Request (RestTemplate)
Operational Layer: ModelParamOperationalController
    ↓ calls
Operational Layer: ModelParamOperationalService
    ↓ calls
Shared Layer: ModelParamRepository (JPA)
    ↓ queries
PostgreSQL Database
```

### NgRx State Management

#### State Structure

```typescript
interface ModelParamState {
  modelParams: ModelParam[];
  selectedModelParam: ModelParam | null;
  loading: boolean;
  error: string | null;
  filters: {
    modelType: string | null;
    searchTerm: string | null;
  };
}
```

#### Actions

```typescript
// Load actions
loadModelParams({ locationId: number })
loadModelParamsSuccess({ modelParams: ModelParam[] })
loadModelParamsFailure({ error: string })

// Filter actions
filterByModelType({ modelType: string | null })
searchByModelName({ searchTerm: string })

// Create actions
createModelParam({ request: ModelParamCreateRequest, locationId: number, userId: string })
createModelParamSuccess({ modelParam: ModelParam })
createModelParamFailure({ error: string })

// Update actions
updateModelParam({ id: number, request: ModelParamUpdateRequest, userId: string })
updateModelParamSuccess({ modelParam: ModelParam })
updateModelParamFailure({ error: string })

// Delete actions
deleteModelParam({ id: number, userId: string })
deleteModelParamSuccess({ id: number })
deleteModelParamFailure({ error: string })

// Detail actions
selectModelParam({ id: number })
clearSelectedModelParam()
```

#### Effects

- `loadModelParams$`: Calls API to load model parameters
- `createModelParam$`: Calls API to create, shows success message
- `updateModelParam$`: Calls API to update, shows success message
- `deleteModelParam$`: Calls API to delete, shows success message
- `locationChange$`: Listens to location changes and reloads data

#### Selectors

```typescript
selectAllModelParams
selectFilteredModelParams  // Applies filters
selectLoading
selectError
selectSelectedModelParam
```

## Data Models

### DTOs (Data Transfer Objects)

#### ModelParamDTO

```typescript
interface ModelParamDTO {
  id: number;
  locationId: number;
  modelName: string;
  modelType: string;
  parameters: string;  // JSON string
  createdAt: string;   // ISO 8601 format
  createdBy: string;
}
```

#### ModelParamCreateRequest

```typescript
interface ModelParamCreateRequest {
  modelName: string;
  modelType: string;
  parameters: string;  // JSON string
}
```

#### ModelParamUpdateRequest

```typescript
interface ModelParamUpdateRequest {
  modelName: string;
  modelType: string;
  parameters: string;  // JSON string
}
```

### Java DTOs

```java
public class ModelParamDTO {
  private Long id;
  private Long locationId;
  private String modelName;
  private String modelType;
  private String parameters;
  private Instant createdAt;
  private String createdBy;
}

public class ModelParamCreateRequest {
  @NotBlank
  @Size(max = 50)
  private String modelName;
  
  @NotBlank
  private String modelType;
  
  @NotBlank
  private String parameters;
}

public class ModelParamUpdateRequest {
  @NotBlank
  @Size(max = 50)
  private String modelName;
  
  @NotBlank
  private String modelType;
  
  @NotBlank
  private String parameters;
}
```

### Validation Annotations

The backend will use Jakarta Bean Validation:
- `@NotBlank`: Ensures field is not null or empty
- `@Size(max = 50)`: Ensures string length constraint
- Custom validator for JSON syntax validation
- Custom validator for model type enum validation

## Correctness Properties


A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.

### Navigation and Routing Properties

Property 1: Navigation to model params route
*For any* application state, when a user clicks the "Model Param Config" menu item, the application should navigate to the route /landingai/model-params
**Validates: Requirements 1.2**

### Data Filtering and Display Properties

Property 2: Location-based filtering
*For any* selected location and set of model parameter records, the displayed list should contain only records where location_id matches the selected location's id
**Validates: Requirements 2.1, 10.1**

Property 3: Location change triggers reload
*For any* two different locations, when a user changes from one location to another, the system should reload the list and display only records for the new location
**Validates: Requirements 2.2, 10.3**

Property 4: Model type filtering
*For any* selected model type and set of model parameter records, when the model type filter is applied, the displayed list should contain only records where model_type matches the selected type
**Validates: Requirements 2.3**

Property 5: Search by model name
*For any* search term and set of model parameter records, the filtered list should contain only records where model_name contains the search term (case-insensitive)
**Validates: Requirements 2.4**

Property 6: Display required fields
*For any* model parameter record in the list view, the rendered output should include model_name, model_type, created_at, and created_by
**Validates: Requirements 2.5**

### Create Operation Properties

Property 7: Create operation with automatic fields
*For any* valid create request with model_name, model_type, and parameters, when submitted, the system should create a new record where:
- location_id equals the currently selected location's id
- created_at is set to a timestamp within 1 second of submission time
- created_by equals the current user's identifier
- model_name, model_type, and parameters match the submitted values
**Validates: Requirements 3.2, 3.3, 3.4, 3.5**

Property 8: Post-creation list refresh
*For any* successfully created model parameter record, the list view should be refreshed and contain the newly created record, and a success notification should be displayed
**Validates: Requirements 3.6**

### Update Operation Properties

Property 9: Edit form population
*For any* model parameter record, when the edit button is clicked, the form should be pre-populated with values matching the record's current model_name, model_type, and parameters
**Validates: Requirements 4.1**

Property 10: Update operation preserves audit fields
*For any* model parameter record and valid update request, when the update is submitted, the updated record should:
- Have model_name, model_type, and parameters matching the submitted values
- Preserve the original created_at value (unchanged)
- Preserve the original created_by value (unchanged)
**Validates: Requirements 4.2, 4.3**

Property 11: Post-update list refresh
*For any* successfully updated model parameter record, the list view should be refreshed and display the updated values, and a success notification should be displayed
**Validates: Requirements 4.4**

### Delete Operation Properties

Property 12: Delete operation removes record
*For any* model parameter record, when deletion is confirmed, the record should no longer exist in the database and should not appear in subsequent queries
**Validates: Requirements 5.2**

Property 13: Post-deletion list refresh
*For any* successfully deleted model parameter record, the list view should be refreshed and not contain the deleted record, and a success notification should be displayed
**Validates: Requirements 5.3**

Property 14: Delete cancellation preserves record
*For any* model parameter record, when deletion is initiated but then cancelled, the record should remain unchanged in the database
**Validates: Requirements 5.4**

### Detail View Properties

Property 15: Detail view completeness
*For any* model parameter record, when the detail view is opened, the displayed information should include all fields: id, location_id, model_name, model_type, parameters, created_at, and created_by
**Validates: Requirements 6.1**

Property 16: JSON formatting for readability
*For any* valid JSON string in the parameters field, when displayed in the detail view, the JSON should be formatted with proper indentation and line breaks for readability
**Validates: Requirements 6.2**

### Validation Properties

Property 17: Empty model name validation
*For any* form submission where model_name is an empty string or contains only whitespace, the system should prevent submission and display an error message
**Validates: Requirements 7.1**

Property 18: Model name length validation
*For any* form submission where model_name exceeds 50 characters, the system should prevent submission and display an error message
**Validates: Requirements 7.2**

Property 19: Model type required validation
*For any* form submission where model_type is not selected (null or empty), the system should prevent submission and display an error message
**Validates: Requirements 7.3**

Property 20: Model type enum validation
*For any* form submission where model_type is not one of the valid values ("Object Detection", "Classification", "Segmentation"), the system should prevent submission and display an error message
**Validates: Requirements 7.4**

Property 21: JSON syntax validation
*For any* form submission where the parameters field contains invalid JSON syntax, the system should prevent submission and display an error message indicating the specific JSON syntax error
**Validates: Requirements 7.5**

Property 22: Location required validation
*For any* form submission when no location is selected in the topbar, the system should prevent submission and display an error message
**Validates: Requirements 7.6**

### JSON Editing Properties

Property 23: Real-time JSON validation with feedback
*For any* text entered in the parameters field, the system should:
- Validate the text as JSON in real-time
- Display a visual error indicator and error message when JSON is invalid
- Display a visual success indicator when JSON is valid
- Accept any valid JSON structure (objects, arrays, primitives, nested structures)
**Validates: Requirements 8.1, 8.2, 8.3, 8.4**

### User Feedback Properties

Property 24: Operation feedback notifications
*For any* create, update, or delete operation:
- When the operation succeeds, a success notification should be displayed
- When the operation fails, an error notification with a descriptive message should be displayed
**Validates: Requirements 9.1, 9.2**

Property 25: Loading state indicator
*For any* data loading operation (initial load, create, update, delete), while the operation is in progress, a loading indicator should be visible to the user
**Validates: Requirements 9.3**

Property 26: Network error recovery
*For any* network error during an operation, the system should display an error message and provide a retry mechanism to attempt the operation again
**Validates: Requirements 9.4**

## Error Handling

### Frontend Error Handling

**HTTP Error Responses**:
- 400 Bad Request: Display validation error messages from server
- 404 Not Found: Display "Record not found" message
- 500 Internal Server Error: Display generic error message with retry option
- Network errors: Display connection error with retry option

**Form Validation Errors**:
- Display inline error messages below form fields
- Prevent form submission until all errors are resolved
- Highlight invalid fields with red border
- Show error summary at top of form

**State Management Errors**:
- Catch errors in NgRx effects
- Dispatch failure actions with error messages
- Update error state in store
- Display error messages using Material snackbar

### Backend Error Handling

**Validation Errors**:
- Return 400 Bad Request with detailed error messages
- Use Jakarta Bean Validation for automatic validation
- Custom validators for JSON and enum validation
- Include field names in error responses

**Database Errors**:
- Catch JPA exceptions
- Log errors with stack traces
- Return 500 Internal Server Error with generic message
- Don't expose internal database details to client

**Not Found Errors**:
- Return 404 when model parameter ID doesn't exist
- Include helpful error message
- Log warning for debugging

**Exception Handling Strategy**:
```java
@ControllerAdvice
public class ModelParamExceptionHandler {
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    // Return 400 with field-specific errors
  }
  
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      EntityNotFoundException ex) {
    // Return 404 with error message
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericError(
      Exception ex) {
    // Log error, return 500 with generic message
  }
}
```

## Testing Strategy

### Dual Testing Approach

This feature will use both unit tests and property-based tests to ensure comprehensive coverage:

**Unit Tests**: Focus on specific examples, edge cases, and integration points
- Test specific validation scenarios (empty strings, boundary values)
- Test error handling paths
- Test component initialization and lifecycle
- Test service method calls with mocked dependencies
- Test NgRx reducers with specific actions
- Test dialog interactions

**Property-Based Tests**: Verify universal properties across all inputs
- Generate random model parameter data
- Test CRUD operations with varied inputs
- Test filtering and search with random terms
- Test JSON validation with generated JSON structures
- Verify invariants hold across operations

### Property-Based Testing Configuration

**Library**: Use `fast-check` for TypeScript/Angular and `jqwik` for Java/Spring Boot

**Test Configuration**:
- Minimum 100 iterations per property test
- Each test tagged with feature name and property number
- Tag format: `Feature: model-param-config, Property {number}: {property_text}`

**Example Property Test Structure**:

TypeScript (fast-check):
```typescript
// Feature: model-param-config, Property 7: Create operation with automatic fields
it('should create record with automatic fields for any valid input', () => {
  fc.assert(
    fc.property(
      fc.record({
        modelName: fc.string({ minLength: 1, maxLength: 50 }),
        modelType: fc.constantFrom('Object Detection', 'Classification', 'Segmentation'),
        parameters: fc.jsonValue().map(JSON.stringify)
      }),
      (request) => {
        const result = service.create(request, locationId, userId);
        expect(result.locationId).toBe(locationId);
        expect(result.createdBy).toBe(userId);
        expect(result.createdAt).toBeCloseTo(Date.now(), -3);
      }
    ),
    { numRuns: 100 }
  );
});
```

Java (jqwik):
```java
// Feature: model-param-config, Property 21: JSON syntax validation
@Property(tries = 100)
void shouldRejectInvalidJSON(@ForAll("invalidJSON") String invalidJson) {
  ModelParamCreateRequest request = new ModelParamCreateRequest();
  request.setModelName("Test");
  request.setModelType("Object Detection");
  request.setParameters(invalidJson);
  
  assertThrows(ValidationException.class, () -> {
    validator.validate(request);
  });
}
```

### Test Coverage Requirements

**Frontend**:
- Component tests: 80% coverage minimum
- Service tests: 90% coverage minimum
- NgRx tests: 100% coverage (reducers, effects, selectors)
- Property tests for all correctness properties

**Backend**:
- Controller tests: 80% coverage minimum
- Service tests: 90% coverage minimum
- Repository tests: Integration tests for custom queries
- Property tests for validation and business logic

### Testing Tools

**Frontend**:
- Jasmine/Karma for unit tests
- fast-check for property-based tests
- Angular Testing Library for component tests
- NgRx testing utilities for state tests

**Backend**:
- JUnit 5 for unit tests
- jqwik for property-based tests
- Mockito for mocking
- Spring Boot Test for integration tests
- TestContainers for database integration tests
