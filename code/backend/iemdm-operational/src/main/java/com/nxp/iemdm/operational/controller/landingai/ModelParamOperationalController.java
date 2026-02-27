package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.operational.service.landingai.ModelParamOperationalService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ModelParamCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ModelParamDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelParamUpdateRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/operational/landingai/model-params")
public class ModelParamOperationalController {

  private final ModelParamOperationalService modelParamOperationalService;

  public ModelParamOperationalController(
      ModelParamOperationalService modelParamOperationalService) {
    this.modelParamOperationalService = modelParamOperationalService;
  }

  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ModelParamDTO>> getModelParams(
      @RequestParam("locationId") Long locationId,
      @RequestParam(value = "modelType", required = false) String modelType) {
    List<ModelParamDTO> modelParams;
    if (modelType != null && !modelType.trim().isEmpty()) {
      modelParams =
          modelParamOperationalService.getModelParamsByLocationAndType(locationId, modelType);
    } else {
      modelParams = modelParamOperationalService.getModelParamsByLocation(locationId);
    }
    return ResponseEntity.ok(modelParams);
  }

  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ModelParamDTO>> searchModelParams(
      @RequestParam("locationId") Long locationId, @RequestParam("modelName") String modelName) {
    List<ModelParamDTO> modelParams =
        modelParamOperationalService.searchModelParamsByName(locationId, modelName);
    return ResponseEntity.ok(modelParams);
  }

  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> getModelParamById(@PathVariable("id") Long id) {
    ModelParamDTO modelParam = modelParamOperationalService.getModelParamById(id);
    return ResponseEntity.ok(modelParam);
  }

  @MethodLog
  @Transactional
  @PostMapping(produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> createModelParam(
      @Valid @RequestBody ModelParamCreateRequest request,
      @RequestParam("locationId") Long locationId,
      @RequestParam("userId") String userId) {
    ModelParamDTO created =
        modelParamOperationalService.createModelParam(request, locationId, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @MethodLog
  @Transactional
  @PutMapping(
      value = "/{id}",
      produces = MediaType.APPLICATION_JSON,
      consumes = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> updateModelParam(
      @PathVariable("id") Long id,
      @Valid @RequestBody ModelParamUpdateRequest request,
      @RequestParam("userId") String userId) {
    ModelParamDTO updated = modelParamOperationalService.updateModelParam(id, request, userId);
    return ResponseEntity.ok(updated);
  }

  @MethodLog
  @Transactional
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteModelParam(
      @PathVariable("id") Long id, @RequestParam("userId") String userId) {
    modelParamOperationalService.deleteModelParam(id, userId);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            err -> {
              String fieldName = ((FieldError) err).getField();
              String message = err.getDefaultMessage();
              errors.put(fieldName, message);
            });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }
}
