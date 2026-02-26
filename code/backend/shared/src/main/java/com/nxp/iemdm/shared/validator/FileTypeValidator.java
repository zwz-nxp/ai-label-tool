package com.nxp.iemdm.shared.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator for file type validation. Validates: Property 4 - File validation Requirements: FR-2.1,
 * NFR-2.1
 *
 * <p>Ensures uploaded files are of allowed image formats (JPG, PNG, BMP)
 */
public class FileTypeValidator implements ConstraintValidator<FileTypeConstraint, MultipartFile> {

  private List<String> allowedTypes;
  private List<String> allowedExtensions;

  @Override
  public void initialize(FileTypeConstraint constraintAnnotation) {
    this.allowedTypes = Arrays.asList(constraintAnnotation.allowedTypes());
    this.allowedExtensions = Arrays.asList(constraintAnnotation.allowedExtensions());
  }

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file == null || file.isEmpty()) {
      return true; // null/empty files are handled by @NotNull/@NotEmpty
    }

    // Check content type
    String contentType = file.getContentType();
    if (contentType != null && allowedTypes.contains(contentType.toLowerCase())) {
      return true;
    }

    // Check file extension as fallback
    String filename = file.getOriginalFilename();
    if (filename != null) {
      String lowerFilename = filename.toLowerCase();
      for (String extension : allowedExtensions) {
        if (lowerFilename.endsWith(extension)) {
          return true;
        }
      }
    }

    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(
            String.format(
                "File type must be one of: %s. Received: %s",
                String.join(", ", allowedExtensions),
                contentType != null ? contentType : "unknown"))
        .addConstraintViolation();

    return false;
  }
}
