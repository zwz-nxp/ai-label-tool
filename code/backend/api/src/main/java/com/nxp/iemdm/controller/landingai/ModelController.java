package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.service.ModelService;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Model operations. Delegates business logic to ModelService. */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

  private final ModelService modelService;

  /** Get all models list with evaluation metrics. Uses DTO to avoid JPA association issues. */
  @GetMapping
  public ResponseEntity<List<ModelWithMetricsDto>> getAllModels() {
    List<ModelWithMetricsDto> models = modelService.getAllModels();
    return ResponseEntity.ok(models);
  }

  /**
   * Get paginated models list (preserves original functionality, returns Entity). Note: This method
   * does not include ConfidentialReport data.
   */
  @GetMapping("/page")
  public ResponseEntity<Page<Model>> getModelsPage(Pageable pageable) {
    Page<Model> models = modelService.getModelsPage(pageable);
    return ResponseEntity.ok(models);
  }

  /** Get specific model by ID with evaluation metrics. */
  @GetMapping("/{id}")
  public ResponseEntity<ModelWithMetricsDto> getModelById(@PathVariable Long id) {
    ModelWithMetricsDto model = modelService.getModelById(id);
    if (model != null) {
      return ResponseEntity.ok(model);
    }
    return ResponseEntity.notFound().build();
  }

  /** Get models list by project ID with evaluation metrics. */
  @GetMapping("/project/{projectId}")
  public ResponseEntity<List<ModelWithMetricsDto>> getModelsByProjectId(
      @PathVariable Long projectId) {
    List<ModelWithMetricsDto> models = modelService.getModelsByProjectId(projectId);
    return ResponseEntity.ok(models);
  }

  /** Toggle model favorite status. */
  @PutMapping("/{id}/favorite")
  public ResponseEntity<ModelWithMetricsDto> toggleFavorite(@PathVariable Long id) {
    try {
      ModelWithMetricsDto updatedModel = modelService.toggleFavorite(id);
      return ResponseEntity.ok(updatedModel);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Search models by model alias or creator with evaluation metrics. */
  @GetMapping("/search")
  public ResponseEntity<List<ModelWithMetricsDto>> searchModels(
      @RequestParam(required = false) String query,
      @RequestParam(required = false, defaultValue = "false") Boolean favoritesOnly,
      @RequestParam(required = false) Long projectId) {
    List<ModelWithMetricsDto> models = modelService.searchModels(query, favoritesOnly, projectId);
    return ResponseEntity.ok(models);
  }
}
