package com.nxp.iemdm.shared.dto.landingai;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured error response for API exceptions. Provides consistent error information across all
 * endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String message;
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();
  private String path;
  private Integer status;
  private List<String> details;

  // Legacy fields for backward compatibility
  private String errorCode;
  private Map<String, String> detailsMap;

  public ErrorResponse(String errorCode, String message) {
    this.errorCode = errorCode;
    this.code = errorCode;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }

  public ErrorResponse(String errorCode, String message, String path) {
    this(errorCode, message);
    this.path = path;
  }

  public String getErrorCode() {
    return errorCode != null ? errorCode : code;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
    this.code = errorCode;
  }

  public Map<String, String> getDetails() {
    if (detailsMap == null) {
      detailsMap = new HashMap<>();
    }
    return detailsMap;
  }

  public void setDetails(Map<String, String> details) {
    this.detailsMap = details;
  }

  public void addDetail(String key, String value) {
    if (detailsMap == null) {
      detailsMap = new HashMap<>();
    }
    this.detailsMap.put(key, value);
  }
}
