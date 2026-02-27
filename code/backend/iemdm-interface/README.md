# Databricks Integration Service

This module provides REST APIs for integrating with Databricks for machine learning model training, testing, and management.

## Quick Start

### Run in Mock Mode (No Databricks Required)

```bash
cd code/backend/iemdm-interface
mvn spring-boot:run -Dspring.profiles.active=mock
```

The service will start on `http://localhost:8080`

### Run in Real Mode (Requires Databricks)

```bash
export DATABRICKS_TOKEN=your_token_here
export DATABRICKS_WORKSPACE_URL=https://your-workspace.cloud.databricks.com
export DATABRICKS_CLUSTER_ID=your_cluster_id

mvn spring-boot:run -Dspring.profiles.active=real
```

## API Endpoints

### 1. Submit Training Data
```
POST /api/databricks/training
Content-Type: application/json

{
  "epochs": "100",
  "modelSize": "medium",
  "transformParam": "resize:640",
  "augmentationParam": "flip:true",
  "classList": ["Neil", "Chips"],
  "modelFullName": "YOLO-WB",
  "trainingCount": "80",
  "devCount": "10",
  "testCount": "10",
  "imageList": [{
    "imageUrl": "https://example.com/image1.jpg",
    "dataSet": "train",
    "labelList": [{
      "xcenter": "0.5",
      "ycenter": "0.5",
      "width": "0.2",
      "height": "0.3",
      "className": "Neil"
    }]
  }]
}

Response:
{
  "errorMessage": "",
  "trackId": "1234567890"
}
```

### 2. Get Training Results
```
GET /api/databricks/training/results?modelFullName=YOLO-WB&version=0&trackId=1234567890

Response:
{
  "f1Rate": "0.85",
  "precisionRate": "0.87",
  "recallRate": "0.83",
  "modelVersion": "1",
  "trainingCorrectRate": "0.92",
  "devCorrectRate": "0.88",
  "testCorrectRate": "0.86",
  "confidenceThreshhold": "0.5",
  "imageList": [...]
}
```

### 3. Test Model
```
POST /api/databricks/model/test
Content-Type: application/json

{
  "modelFullName": "YOLO-WB",
  "version": 0,
  "trackId": "1234567890",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.png"
  ]
}

Response: (Same structure as Get Training Results)
```

### 4. Download Model
```
GET /api/databricks/model/download?model_name=YOLO-WB&version=0&track_id=12345

Response:
{
  "modelFullName": "YOLO-WB",
  "version": 0,
  "trackId": "12345",
  "artifact": {
    "downloadUrl": "https://databricks.example.com/files/models/12345/model.zip"
  }
}
```

### 5. Delete Model
```
DELETE /api/databricks/model
Content-Type: application/json

{
  "modelFullName": "YOLO-WB",
  "version": 0,
  "trackId": "12345"
}

Response:
{
  "stateCode": "DELETED",
  "message": "Model removed successfully.",
  "model": {
    "modelFullName": "YOLO-WB",
    "version": 0,
    "trackId": "12345"
  }
}
```

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
databricks:
  mode: mock  # or real
  workspace:
    url: https://your-workspace.cloud.databricks.com
    token: ${DATABRICKS_TOKEN}
  training:
    notebook-path: /Workspace/training/yolo_training
    cluster-id: ${DATABRICKS_CLUSTER_ID}
```

## Mock Mode vs Real Mode

- **Mock Mode**: Returns simulated responses instantly. Perfect for development and testing without Databricks access.
- **Real Mode**: Makes actual calls to Databricks platform. Requires valid credentials and configuration.

## Testing with cURL

### Submit Training
```bash
curl -X POST http://localhost:8080/api/databricks/training \
  -H "Content-Type: application/json" \
  -d '{
    "epochs": "100",
    "modelFullName": "YOLO-WB",
    "classList": ["Neil", "Chips"],
    "imageList": []
  }'
```

### Get Results
```bash
curl "http://localhost:8080/api/databricks/training/results?modelFullName=YOLO-WB&version=0&trackId=1234567890"
```

## Development Status

✅ **Completed:**
- DTOs (Request/Response models)
- REST Controller with 5 endpoints
- Mock Service implementation
- Configuration management
- Exception handling

🚧 **In Progress:**
- Real Databricks integration (DatabricksClient)
- Request/Response mappers
- Integration tests

## Next Steps

1. **For Teammates (CR4, CR5)**: You can start integrating against the mock endpoints now!
2. **For Real Implementation**: We'll implement the DatabricksClient class to make actual Databricks SDK calls.

## Support

For questions or issues, contact the CR6 team.
