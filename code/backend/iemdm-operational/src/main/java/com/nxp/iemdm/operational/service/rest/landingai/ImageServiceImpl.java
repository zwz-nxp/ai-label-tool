package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.exception.landingai.ImageProcessingException;
import com.nxp.iemdm.exception.landingai.InvalidImageFormatException;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageFile;
import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.ImagePredictionLabel;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest;
import com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ImageUploadResponse;
import com.nxp.iemdm.shared.dto.landingai.LabelOverlayDTO;
import com.nxp.iemdm.shared.dto.landingai.PaginatedResponse;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageFileRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImagePredictionLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageTagRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.NotNull;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for Landing AI image operations in the operational layer. Provides internal
 * endpoints that are called by the API service layer. Includes all business logic for image
 * management.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/images")
public class ImageServiceImpl {

  private static final Set<String> ALLOWED_FORMATS = Set.of("png", "jpg", "jpeg");
  private static final int THUMBNAIL_MAX_SIZE = 200;
  private static final float THUMBNAIL_QUALITY = 0.7f;
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  private final ImageRepository imageRepository;
  private final ImageFileRepository imageFileRepository;
  private final ProjectRepository projectRepository;
  private final ProjectClassRepository projectClassRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final ImageMetadataRepository imageMetadataRepository;
  private final ProjectMetadataRepository projectMetadataRepository;
  private final ImageTagRepository imageTagRepository;
  private final FilterService filterService;
  private final SortService sortService;
  private final com.nxp.iemdm.operational.service.landingai.ImageService imageService;

  @PersistenceContext private EntityManager entityManager;

  @Autowired
  public ImageServiceImpl(
      ImageRepository imageRepository,
      ImageFileRepository imageFileRepository,
      ProjectRepository projectRepository,
      ProjectClassRepository projectClassRepository,
      ImageLabelRepository imageLabelRepository,
      ImagePredictionLabelRepository imagePredictionLabelRepository,
      ImageMetadataRepository imageMetadataRepository,
      ProjectMetadataRepository projectMetadataRepository,
      ImageTagRepository imageTagRepository,
      FilterService filterService,
      SortService sortService,
      com.nxp.iemdm.operational.service.landingai.ImageService imageService) {
    this.imageRepository = imageRepository;
    this.imageFileRepository = imageFileRepository;
    this.projectRepository = projectRepository;
    this.projectClassRepository = projectClassRepository;
    this.imageLabelRepository = imageLabelRepository;
    this.imagePredictionLabelRepository = imagePredictionLabelRepository;
    this.imageMetadataRepository = imageMetadataRepository;
    this.projectMetadataRepository = projectMetadataRepository;
    this.imageTagRepository = imageTagRepository;
    this.filterService = filterService;
    this.sortService = sortService;
    this.imageService = imageService;
  }

  /**
   * Upload images to a project with file validation.
   *
   * @param files the image files to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return list of upload responses
   */
  @MethodLog
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public List<ImageUploadResponse> uploadImages(
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam("projectId") @NotNull Long projectId,
      @RequestParam("userId") String userId) {

    log.info(
        "Operational REST: Uploading {} images to project: {} by user: {}",
        files.size(),
        projectId,
        userId);

    List<ImageUploadResponse> responses = new ArrayList<>();

    for (MultipartFile file : files) {
      try {
        // Validate file
        validateImageFile(file);

        // Extract metadata
        ImageMetadata metadata = extractMetadata(file);

        // Generate thumbnail (always as JPG)
        byte[] thumbnail = generateThumbnail(file);

        // Calculate thumbnail dimensions for scale ratios
        BufferedImage thumbnailImg = ImageIO.read(new java.io.ByteArrayInputStream(thumbnail));
        int thumbnailWidth = thumbnailImg.getWidth();
        int thumbnailHeight = thumbnailImg.getHeight();

        // Calculate scale ratios
        ThumbnailRatios ratios = calculateThumbnailRatios(file, thumbnailWidth, thumbnailHeight);

        // Convert image to JPG format if it's PNG or JPEG
        byte[] fileContent = convertToJpg(file);

        // Get original filename and convert extension to .jpg
        String originalFilename = file.getOriginalFilename();
        String jpgFilename = convertFilenameToJpg(originalFilename);

        // Create image entity
        Image image = new Image();
        image.setFileName(jpgFilename); // Store filename with .jpg extension
        image.setFileSize((long) fileContent.length); // Use converted file size
        image.setWidth(metadata.width);
        image.setHeight(metadata.height);
        image.setThumbnailImage(thumbnail);
        image.setThumbnailWidthRatio(ratios.widthRatio);
        image.setThumbnailHeightRatio(ratios.heightRatio);
        image.setCreatedBy(userId);

        // Set project reference
        Project project = new Project();
        project.setId(projectId);
        image.setProject(project);

        // Create and save image file FIRST to get the file ID
        String uniqueFilename = generateUniqueFilename(System.currentTimeMillis(), jpgFilename);
        ImageFile imageFile = new ImageFile();
        imageFile.setFileName(uniqueFilename); // Store unique filename with .jpg extension
        imageFile.setImageFileStream(fileContent); // Store converted JPG content
        imageFile.setCreatedBy(userId);

        // Save image file to database and get generated ID
        ImageFile savedImageFile = imageFileRepository.save(imageFile);

        // Set file_id reference in image
        image.setFileId(savedImageFile.getId());

        // Save image with file reference
        Image savedImage = imageRepository.save(image);

        log.info(
            "Uploaded image with id: {} to database, original: {}, converted to: {}, file size: {} bytes, width ratio: {}, height ratio: {}",
            savedImage.getId(),
            originalFilename,
            jpgFilename,
            fileContent.length,
            ratios.widthRatio,
            ratios.heightRatio);

        // Create success response
        responses.add(
            new ImageUploadResponse(
                savedImage.getId(),
                savedImage.getFileName(),
                savedImage.getFileSize(),
                savedImage.getWidth(),
                savedImage.getHeight()));

      } catch (InvalidImageFormatException | ImageProcessingException e) {
        log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
        responses.add(new ImageUploadResponse(file.getOriginalFilename(), e.getMessage()));
      } catch (IOException e) {
        log.error("Failed to read image file: {}", file.getOriginalFilename(), e);
        responses.add(
            new ImageUploadResponse(
                file.getOriginalFilename(), "Failed to read file: " + e.getMessage()));
      }
    }

    return responses;
  }

  /**
   * Upload a ZIP file containing classified images. The ZIP should contain folders named by class,
   * with images inside each folder. New classes will be created automatically if they don't exist.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return map containing upload statistics
   */
  @MethodLog
  @PostMapping(path = "/upload-classified", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public java.util.Map<String, Object> uploadClassifiedImagesZip(
      @RequestParam("file") MultipartFile file,
      @RequestParam("projectId") @NotNull Long projectId,
      @RequestParam("userId") String userId) {

    log.info(
        "Operational REST: Uploading classified images ZIP to project: {} by user: {}",
        projectId,
        userId);

    java.util.Map<String, Object> result = new java.util.HashMap<>();
    java.util.List<String> errors = new java.util.ArrayList<>();
    int totalImages = 0;
    int classesCreated = 0;
    int classesReused = 0;

    // Map to cache class name -> ProjectClass
    java.util.Map<String, com.nxp.iemdm.model.landingai.ProjectClass> classCache =
        new java.util.HashMap<>();

    // Get existing classes for the project
    java.util.List<com.nxp.iemdm.model.landingai.ProjectClass> existingClasses =
        projectClassRepository.findByProject_IdOrderByCreatedAt(projectId);
    for (com.nxp.iemdm.model.landingai.ProjectClass pc : existingClasses) {
      classCache.put(pc.getClassName().toLowerCase(), pc);
    }

    // Get existing color codes to avoid duplicates
    java.util.Set<String> usedColors =
        existingClasses.stream()
            .map(com.nxp.iemdm.model.landingai.ProjectClass::getColorCode)
            .filter(c -> c != null)
            .collect(java.util.stream.Collectors.toSet());

    try (java.util.zip.ZipInputStream zipInputStream =
        new java.util.zip.ZipInputStream(file.getInputStream())) {

      java.util.zip.ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        String entryName = entry.getName();

        // Skip directories and hidden files
        if (entry.isDirectory() || entryName.startsWith("__MACOSX") || entryName.startsWith(".")) {
          continue;
        }

        // Parse the path to get class name and filename
        // Expected format: className/filename.jpg or className/subdir/filename.jpg
        String[] pathParts = entryName.split("/");
        if (pathParts.length < 2) {
          // File is at root level, skip it
          log.warn("Skipping file at root level: {}", entryName);
          errors.add("Skipped file at root level: " + entryName);
          continue;
        }

        String className = pathParts[0];
        String fileName = pathParts[pathParts.length - 1];

        // Skip non-image files
        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".jpg")
            && !lowerFileName.endsWith(".jpeg")
            && !lowerFileName.endsWith(".png")) {
          log.warn("Skipping non-image file: {}", entryName);
          continue;
        }

        // Get or create the class
        com.nxp.iemdm.model.landingai.ProjectClass projectClass =
            classCache.get(className.toLowerCase());
        if (projectClass == null) {
          // Create new class
          projectClass = new com.nxp.iemdm.model.landingai.ProjectClass();
          projectClass.setClassName(className);
          projectClass.setColorCode(generateRandomColor(usedColors));
          projectClass.setCreatedBy(userId);

          Project project = new Project();
          project.setId(projectId);
          projectClass.setProject(project);

          projectClass = projectClassRepository.save(projectClass);
          classCache.put(className.toLowerCase(), projectClass);
          usedColors.add(projectClass.getColorCode());
          classesCreated++;
          log.info("Created new class: {} with color: {}", className, projectClass.getColorCode());
        } else {
          classesReused++;
        }

        try {
          // Read the image data from the zip stream
          byte[] imageData = readZipEntryData(zipInputStream);

          // Create a mock MultipartFile for processing
          MockMultipartFile imageFile =
              new MockMultipartFile(fileName, fileName, "image/jpeg", imageData);

          // Validate file
          validateImageFile(imageFile);

          // Extract metadata
          ImageMetadata metadata = extractMetadata(imageFile);

          // Generate thumbnail
          byte[] thumbnail = generateThumbnail(imageFile);

          // Calculate thumbnail dimensions
          BufferedImage thumbnailImg = ImageIO.read(new java.io.ByteArrayInputStream(thumbnail));
          int thumbnailWidth = thumbnailImg.getWidth();
          int thumbnailHeight = thumbnailImg.getHeight();

          // Calculate scale ratios
          ThumbnailRatios ratios =
              calculateThumbnailRatios(imageFile, thumbnailWidth, thumbnailHeight);

          // Convert to JPG
          byte[] fileContent = convertToJpg(imageFile);
          String jpgFilename = convertFilenameToJpg(fileName);

          // Create image entity
          Image image = new Image();
          image.setFileName(jpgFilename);
          image.setFileSize((long) fileContent.length);
          image.setWidth(metadata.width);
          image.setHeight(metadata.height);
          image.setThumbnailImage(thumbnail);
          image.setThumbnailWidthRatio(ratios.widthRatio);
          image.setThumbnailHeightRatio(ratios.heightRatio);
          image.setCreatedBy(userId);

          Project project = new Project();
          project.setId(projectId);
          image.setProject(project);

          // Create and save image file FIRST to get the file ID
          String uniqueFilename = generateUniqueFilename(System.currentTimeMillis(), jpgFilename);
          ImageFile imgFile = new ImageFile();
          imgFile.setFileName(uniqueFilename);
          imgFile.setImageFileStream(fileContent);
          imgFile.setCreatedBy(userId);
          ImageFile savedImgFile = imageFileRepository.save(imgFile);

          // Set file_id reference in image
          image.setFileId(savedImgFile.getId());

          // Save image with file reference
          Image savedImage = imageRepository.save(image);

          // Create label for the image (ground truth)
          ImageLabel label = new ImageLabel();
          label.setImage(savedImage);
          label.setProjectClass(projectClass);
          label.setCreatedBy(userId);
          imageLabelRepository.save(label);

          totalImages++;
          log.info("Uploaded classified image: {} with class: {}", jpgFilename, className);

        } catch (Exception e) {
          log.error("Failed to process image {} from ZIP: {}", entryName, e.getMessage());
          errors.add("Failed to process " + entryName + ": " + e.getMessage());
        }
      }

    } catch (IOException e) {
      log.error("Failed to read ZIP file: {}", e.getMessage(), e);
      result.put("success", false);
      result.put("totalImages", 0);
      result.put("classesCreated", 0);
      result.put("classesReused", 0);
      errors.add("Failed to read ZIP file: " + e.getMessage());
      result.put("errors", errors);
      return result;
    }

    result.put("success", errors.isEmpty() || totalImages > 0);
    result.put("totalImages", totalImages);
    result.put("classesCreated", classesCreated);
    result.put("classesReused", classesReused);
    result.put("errors", errors);

    log.info(
        "Classified upload complete: {} images, {} classes created, {} classes reused, {} errors",
        totalImages,
        classesCreated,
        classesReused,
        errors.size());

    return result;
  }

  /** Read all data from a ZipInputStream entry. */
  private byte[] readZipEntryData(java.util.zip.ZipInputStream zipInputStream) throws IOException {
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int len;
    while ((len = zipInputStream.read(buffer)) > 0) {
      baos.write(buffer, 0, len);
    }
    return baos.toByteArray();
  }

  /** Generate a random hex color code that is not already used. */
  private String generateRandomColor(java.util.Set<String> usedColors) {
    // Predefined set of visually distinct colors
    String[] predefinedColors = {
      "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
      "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
      "#F8B500", "#00CED1", "#FF69B4", "#32CD32", "#FF7F50",
      "#9370DB", "#20B2AA", "#FFD700", "#87CEEB", "#FFA07A",
      "#8FBC8F", "#DEB887", "#5F9EA0", "#D2691E", "#6495ED",
      "#DC143C", "#00FFFF", "#008B8B", "#B8860B", "#A9A9A9"
    };

    // Try predefined colors first
    for (String color : predefinedColors) {
      if (!usedColors.contains(color)) {
        return color;
      }
    }

    // Generate random color if all predefined are used
    java.util.Random random = new java.util.Random();
    String color;
    int attempts = 0;
    do {
      int r = random.nextInt(200) + 55; // Avoid too dark colors
      int g = random.nextInt(200) + 55;
      int b = random.nextInt(200) + 55;
      color = String.format("#%02X%02X%02X", r, g, b);
      attempts++;
    } while (usedColors.contains(color) && attempts < 100);

    return color;
  }

  /**
   * Upload a ZIP file containing batch images. The ZIP should contain images directly at the root
   * level (no class folders). This is for Object Detection and Segmentation projects.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return map containing upload statistics (success, totalImages, errors)
   */
  @MethodLog
  @PostMapping(path = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public java.util.Map<String, Object> uploadBatchImagesZip(
      @RequestParam("file") MultipartFile file,
      @RequestParam("projectId") @NotNull Long projectId,
      @RequestParam("userId") String userId) {

    log.info(
        "Operational REST: Uploading batch images ZIP to project: {} by user: {}",
        projectId,
        userId);

    java.util.Map<String, Object> result = new java.util.HashMap<>();
    java.util.List<String> errors = new java.util.ArrayList<>();
    int totalImages = 0;

    try (java.util.zip.ZipInputStream zipInputStream =
        new java.util.zip.ZipInputStream(file.getInputStream())) {

      java.util.zip.ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        String entryName = entry.getName();

        // Skip directories and hidden files
        if (entry.isDirectory() || entryName.startsWith("__MACOSX") || entryName.startsWith(".")) {
          continue;
        }

        // Get just the filename (last part of the path)
        String fileName = entryName;
        if (entryName.contains("/")) {
          fileName = entryName.substring(entryName.lastIndexOf("/") + 1);
        }

        // Skip hidden files in subdirectories
        if (fileName.startsWith(".")) {
          continue;
        }

        // Skip non-image files
        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".jpg")
            && !lowerFileName.endsWith(".jpeg")
            && !lowerFileName.endsWith(".png")) {
          log.warn("Skipping non-image file: {}", entryName);
          continue;
        }

        try {
          // Read the image data from the zip stream
          byte[] imageData = readZipEntryData(zipInputStream);

          // Create a mock MultipartFile for processing
          MockMultipartFile imageFile =
              new MockMultipartFile(fileName, fileName, "image/jpeg", imageData);

          // Validate file
          validateImageFile(imageFile);

          // Extract metadata
          ImageMetadata metadata = extractMetadata(imageFile);

          // Generate thumbnail
          byte[] thumbnail = generateThumbnail(imageFile);

          // Calculate thumbnail dimensions
          BufferedImage thumbnailImg = ImageIO.read(new java.io.ByteArrayInputStream(thumbnail));
          int thumbnailWidth = thumbnailImg.getWidth();
          int thumbnailHeight = thumbnailImg.getHeight();

          // Calculate scale ratios
          ThumbnailRatios ratios =
              calculateThumbnailRatios(imageFile, thumbnailWidth, thumbnailHeight);

          // Convert to JPG
          byte[] fileContent = convertToJpg(imageFile);
          String jpgFilename = convertFilenameToJpg(fileName);

          // Create image entity
          Image image = new Image();
          image.setFileName(jpgFilename);
          image.setFileSize((long) fileContent.length);
          image.setWidth(metadata.width);
          image.setHeight(metadata.height);
          image.setThumbnailImage(thumbnail);
          image.setThumbnailWidthRatio(ratios.widthRatio);
          image.setThumbnailHeightRatio(ratios.heightRatio);
          image.setCreatedBy(userId);

          Project project = new Project();
          project.setId(projectId);
          image.setProject(project);

          // Create and save image file FIRST to get the file ID
          String uniqueFilename = generateUniqueFilename(System.currentTimeMillis(), jpgFilename);
          ImageFile imgFile = new ImageFile();
          imgFile.setFileName(uniqueFilename);
          imgFile.setImageFileStream(fileContent);
          imgFile.setCreatedBy(userId);
          ImageFile savedImgFile = imageFileRepository.save(imgFile);

          // Set file_id reference in image
          image.setFileId(savedImgFile.getId());

          // Save image with file reference
          Image savedImage = imageRepository.save(image);

          totalImages++;
          log.info("Uploaded batch image: {}", jpgFilename);

        } catch (Exception e) {
          log.error("Failed to process image {} from ZIP: {}", entryName, e.getMessage());
          errors.add("Failed to process " + entryName + ": " + e.getMessage());
        }
      }

    } catch (IOException e) {
      log.error("Failed to read ZIP file: {}", e.getMessage(), e);
      result.put("success", false);
      result.put("totalImages", 0);
      errors.add("Failed to read ZIP file: " + e.getMessage());
      result.put("errors", errors);
      return result;
    }

    result.put("success", errors.isEmpty() || totalImages > 0);
    result.put("totalImages", totalImages);
    result.put("errors", errors);

    log.info("Batch upload complete: {} images, {} errors", totalImages, errors.size());

    return result;
  }

  /** Simple mock MultipartFile implementation for processing images from ZIP. */
  private static class MockMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public MockMultipartFile(
        String name, String originalFilename, String contentType, byte[] content) {
      this.name = name;
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.content = content;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() {
      return content;
    }

    @Override
    public java.io.InputStream getInputStream() {
      return new java.io.ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException {
      try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
        fos.write(content);
      }
    }
  }

  /**
   * Get thumbnail for an image.
   *
   * @param id the image ID
   * @return the thumbnail image as byte array
   */
  @MethodLog
  @GetMapping(path = "/{id}/thumbnail")
  @Transactional(readOnly = true)
  public byte[] getThumbnail(@PathVariable("id") @NotNull Long id) {

    log.info("Operational REST: Getting thumbnail for image: {}", id);

    Image image =
        imageRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Image not found with id: " + id));

    return image.getThumbnailImage();
  }

  /**
   * Get full image file for an image. This retrieves the full binary data from la_images_file
   * table. Use thumbnail endpoint for better performance when displaying previews.
   *
   * @param id the image ID
   * @return the full image file as byte array
   */
  @MethodLog
  @GetMapping(path = "/{id}/file")
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> getImageFile(@PathVariable("id") @NotNull Long id) {

    log.info("Operational REST: Getting full image file for image: {}", id);

    // Get image to retrieve file_id
    Image image =
        imageRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Image not found with id: " + id));

    // Get image file using file_id
    if (image.getFileId() == null) {
      log.error("Image {} has no file_id reference", id);
      return ResponseEntity.notFound().build();
    }

    ImageFile imageFile = imageFileRepository.findById(image.getFileId()).orElse(null);

    if (imageFile == null || imageFile.getImageFileStream() == null) {
      log.error("Image file not found or empty for file_id: {}", image.getFileId());
      return ResponseEntity.notFound().build();
    }

    byte[] fileData = imageFile.getImageFileStream();
    log.info("Returning image file for image {}, size: {} bytes", id, fileData.length);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(fileData.length)
        .body(fileData);
  }

  /**
   * Convert image file to JPG format. If the image is already JPG, returns the original bytes. For
   * PNG and other formats, converts to JPG.
   *
   * @param file the image file to convert
   * @return the image as JPG byte array
   * @throws ImageProcessingException if conversion fails
   */
  private byte[] convertToJpg(MultipartFile file) {
    try {
      String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();

      // If already JPG, return original bytes
      if ("jpg".equals(extension)) {
        return file.getBytes();
      }

      // Read the image
      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      if (originalImage == null) {
        throw new ImageProcessingException(
            file.getOriginalFilename(), "convert to JPG", new IOException("Unable to read image"));
      }

      // Create a new BufferedImage with RGB color model (no alpha channel for JPG)
      BufferedImage rgbImage =
          new BufferedImage(
              originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

      // Draw the original image onto the RGB image (handles transparency by filling with white)
      Graphics2D graphics = rgbImage.createGraphics();
      graphics.setColor(java.awt.Color.WHITE);
      graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
      graphics.drawImage(originalImage, 0, 0, null);
      graphics.dispose();

      // Write as JPG
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(rgbImage, "jpg", outputStream);
      byte[] jpgBytes = outputStream.toByteArray();

      log.info(
          "Converted {} from {} to JPG, original size: {} bytes, converted size: {} bytes",
          file.getOriginalFilename(),
          extension.toUpperCase(),
          file.getSize(),
          jpgBytes.length);

      return jpgBytes;

    } catch (IOException e) {
      throw new ImageProcessingException(file.getOriginalFilename(), "convert to JPG", e);
    }
  }

  /**
   * Convert filename extension to .jpg. Handles .png, .jpeg, and .jpg extensions.
   *
   * @param originalFilename the original filename
   * @return filename with .jpg extension
   */
  private String convertFilenameToJpg(String originalFilename) {
    if (originalFilename == null || originalFilename.isEmpty()) {
      return "image.jpg";
    }

    int lastDotIndex = originalFilename.lastIndexOf('.');
    if (lastDotIndex > 0) {
      // Replace extension with .jpg
      return originalFilename.substring(0, lastDotIndex) + ".jpg";
    }

    // No extension found, append .jpg
    return originalFilename + ".jpg";
  }

  /**
   * Get paginated images for a project with label information and optional filters.
   *
   * @param projectId the project ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param viewMode the view mode (images or instances)
   * @param mediaStatus filter by media status (comma-separated)
   * @param groundTruthLabels filter by ground truth label class IDs (comma-separated)
   * @param predictionLabels filter by prediction label class IDs (comma-separated)
   * @param annotationType filter by annotation type (Ground truth or Prediction)
   * @param modelId filter by model ID (for prediction labels)
   * @param split filter by split (comma-separated)
   * @param tags filter by tag IDs (comma-separated)
   * @param mediaName filter by media name
   * @param labeler filter by labeler
   * @param mediaId filter by media ID
   * @param sortBy the sort method (optional)
   * @return paginated response with image list items
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}")
  @Transactional(readOnly = true)
  public PaginatedResponse<ImageListItemDTO> getImagesForProject(
      @PathVariable("projectId") @NotNull Long projectId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "viewMode", defaultValue = "images") String viewMode,
      @RequestParam(value = "mediaStatus", required = false) String mediaStatus,
      @RequestParam(value = "groundTruthLabels", required = false) String groundTruthLabels,
      @RequestParam(value = "predictionLabels", required = false) String predictionLabels,
      @RequestParam(value = "annotationType", required = false) String annotationType,
      @RequestParam(value = "modelId", required = false) Long modelId,
      @RequestParam(value = "split", required = false) String split,
      @RequestParam(value = "tags", required = false) String tags,
      @RequestParam(value = "mediaName", required = false) String mediaName,
      @RequestParam(value = "labeler", required = false) String labeler,
      @RequestParam(value = "mediaId", required = false) String mediaId,
      @RequestParam(value = "noClass", required = false) Boolean noClass,
      @RequestParam(value = "predictionNoClass", required = false) Boolean predictionNoClass,
      @RequestParam(value = "sortBy", defaultValue = "upload_time_desc") String sortBy,
      @RequestParam(value = "includeThumbnails", defaultValue = "true") boolean includeThumbnails) {

    log.info(
        "Operational REST: Getting images for project: {}, page: {}, size: {}, viewMode: {}, sortBy: {}, annotationType: {}, modelId: {}",
        projectId,
        page,
        size,
        viewMode,
        sortBy,
        annotationType,
        modelId);

    // Build filter request from query parameters
    ImageFilterRequest filters = new ImageFilterRequest();
    if (mediaStatus != null && !mediaStatus.isEmpty()) {
      filters.setMediaStatus(java.util.Arrays.asList(mediaStatus.split(",")));
    }
    if (groundTruthLabels != null && !groundTruthLabels.isEmpty()) {
      filters.setGroundTruthLabels(
          java.util.Arrays.stream(groundTruthLabels.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (predictionLabels != null && !predictionLabels.isEmpty()) {
      filters.setPredictionLabels(
          java.util.Arrays.stream(predictionLabels.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (annotationType != null && !annotationType.isEmpty()) {
      filters.setAnnotationType(annotationType);
    }
    if (modelId != null) {
      filters.setModelId(modelId);
    }
    if (split != null && !split.isEmpty()) {
      filters.setSplit(java.util.Arrays.asList(split.split(",")));
    }
    if (tags != null && !tags.isEmpty()) {
      filters.setTags(
          java.util.Arrays.stream(tags.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (mediaName != null && !mediaName.isEmpty()) {
      filters.setMediaName(mediaName);
    }
    if (labeler != null && !labeler.isEmpty()) {
      filters.setLabeler(labeler);
    }
    if (mediaId != null && !mediaId.isEmpty()) {
      filters.setMediaId(mediaId);
    }
    if (noClass != null && noClass) {
      filters.setNoClass(true);
    }
    if (predictionNoClass != null && predictionNoClass) {
      filters.setPredictionNoClass(true);
    }

    log.info("Filters: {}", filters);

    // Check if we need to sort by label time - this requires fetching all images first
    boolean sortByLabelTime =
        sortBy != null && (sortBy.equals("label_time_desc") || sortBy.equals("label_time_asc"));

    // Pre-filter image IDs at database level for label-based filters
    Set<Long> allowedImageIds = null;

    // Handle media status filter at database level for "labeled" and "unlabeled"
    // Logic:
    // - Labeled: is_labeled=true
    // - Unlabeled: is_labeled=false
    if (filters.getMediaStatus() != null && !filters.getMediaStatus().isEmpty()) {
      Set<Long> mediaStatusImageIds = new java.util.HashSet<>();
      boolean includeLabeled =
          filters.getMediaStatus().stream().anyMatch(s -> "labeled".equalsIgnoreCase(s));
      boolean includeUnlabeled =
          filters.getMediaStatus().stream().anyMatch(s -> "unlabeled".equalsIgnoreCase(s));

      if (includeLabeled) {
        List<Long> labeledIds = imageRepository.findLabeledImageIds(projectId);
        log.info("Found {} labeled images", labeledIds.size());
        mediaStatusImageIds.addAll(labeledIds);
      }
      if (includeUnlabeled) {
        List<Long> unlabeledIds = imageRepository.findUnlabeledImageIds(projectId);
        log.info("Found {} unlabeled images", unlabeledIds.size());
        mediaStatusImageIds.addAll(unlabeledIds);
      }

      log.info("Total {} images matching media status filter", mediaStatusImageIds.size());

      if (allowedImageIds == null) {
        allowedImageIds = mediaStatusImageIds;
      } else {
        allowedImageIds.retainAll(mediaStatusImageIds);
      }
    }

    // Filter by ground truth labels at database level
    if (filters.getGroundTruthLabels() != null && !filters.getGroundTruthLabels().isEmpty()) {
      List<Long> gtImageIds =
          imageLabelRepository.findImageIdsByGroundTruthClassIds(
              projectId, filters.getGroundTruthLabels());
      log.info("Found {} images with ground truth labels matching filter", gtImageIds.size());
      if (allowedImageIds == null) {
        allowedImageIds = new java.util.HashSet<>(gtImageIds);
      } else {
        // AND logic: keep only images that match both filters
        allowedImageIds.retainAll(gtImageIds);
      }
    }

    // Filter by prediction labels at database level (requires model_id)
    if (filters.getPredictionLabels() != null && !filters.getPredictionLabels().isEmpty()) {
      Long filterModelId = filters.getModelId();
      List<Long> predImageIds;

      if (filterModelId != null) {
        // Filter by both model_id and class_ids
        predImageIds =
            imagePredictionLabelRepository.findImageIdsByPredictionClassIdsAndModelId(
                projectId, filterModelId, filters.getPredictionLabels());
        log.info(
            "Found {} images with prediction labels matching filter for model {}",
            predImageIds.size(),
            filterModelId);
      } else {
        // Fallback: filter by class_ids only (all models)
        predImageIds =
            imagePredictionLabelRepository.findImageIdsByPredictionClassIds(
                projectId, filters.getPredictionLabels());
        log.info(
            "Found {} images with prediction labels matching filter (all models)",
            predImageIds.size());
      }

      if (allowedImageIds == null) {
        allowedImageIds = new java.util.HashSet<>(predImageIds);
      } else {
        // AND logic: keep only images that match both filters
        allowedImageIds.retainAll(predImageIds);
      }
    }

    // Filter by prediction "No Class" - images without any prediction labels for the selected model
    if (filters.getPredictionNoClass() != null && filters.getPredictionNoClass()) {
      Long filterModelId = filters.getModelId();
      if (filterModelId != null) {
        // Get all image IDs that have prediction labels for this model
        List<Long> imagesWithPredictions =
            imagePredictionLabelRepository.findImageIdsWithPredictionLabelsForModel(
                projectId, filterModelId);
        log.info(
            "Found {} images with prediction labels for model {}",
            imagesWithPredictions.size(),
            filterModelId);

        // Get all image IDs for the project
        List<Long> allProjectImageIds = imageRepository.findImageIdsByProjectId(projectId);
        log.info("Total {} images in project {}", allProjectImageIds.size(), projectId);

        // Images without predictions = all images - images with predictions
        Set<Long> imagesWithoutPredictions = new java.util.HashSet<>(allProjectImageIds);
        imagesWithoutPredictions.removeAll(imagesWithPredictions);
        log.info(
            "Found {} images without prediction labels for model {}",
            imagesWithoutPredictions.size(),
            filterModelId);

        if (allowedImageIds == null) {
          allowedImageIds = imagesWithoutPredictions;
        } else {
          // AND logic: keep only images that match both filters
          allowedImageIds.retainAll(imagesWithoutPredictions);
        }
      } else {
        log.warn("predictionNoClass filter requires modelId to be set");
      }
    }

    // Filter by labeler at database level
    if (filters.getLabeler() != null && !filters.getLabeler().isEmpty()) {
      List<Long> labelerImageIds =
          imageLabelRepository.findImageIdsByLabeler(projectId, filters.getLabeler());
      log.info(
          "Found {} images with labels by labeler '{}' matching filter",
          labelerImageIds.size(),
          filters.getLabeler());
      if (allowedImageIds == null) {
        allowedImageIds = new java.util.HashSet<>(labelerImageIds);
      } else {
        // AND logic: keep only images that match both filters
        allowedImageIds.retainAll(labelerImageIds);
      }
    }

    // Filter by split at database level
    if (filters.getSplit() != null && !filters.getSplit().isEmpty()) {
      // Check if "unassigned" is in the filter list
      boolean includeUnassigned =
          filters.getSplit().stream().anyMatch(s -> "unassigned".equalsIgnoreCase(s));

      // Get the non-unassigned splits (training, dev, test) in lowercase
      List<String> assignedSplits =
          filters.getSplit().stream()
              .filter(s -> !"unassigned".equalsIgnoreCase(s))
              .map(String::toLowerCase)
              .collect(java.util.stream.Collectors.toList());

      log.info(
          "Split filter - includeUnassigned: {}, assignedSplits: {}",
          includeUnassigned,
          assignedSplits);

      Set<Long> splitImageIds = new java.util.HashSet<>();

      // Get images with assigned splits (training, dev, test)
      if (!assignedSplits.isEmpty()) {
        List<Long> assignedIds =
            imageRepository.findImageIdsByAssignedSplits(projectId, assignedSplits);
        log.info("Found {} images with assigned splits: {}", assignedIds.size(), assignedSplits);
        splitImageIds.addAll(assignedIds);
      }

      // Get images with unassigned split (null or empty)
      if (includeUnassigned) {
        List<Long> unassignedIds = imageRepository.findImageIdsWithUnassignedSplit(projectId);
        log.info("Found {} images with unassigned split", unassignedIds.size());
        splitImageIds.addAll(unassignedIds);
      }

      log.info("Total {} images matching split filter", splitImageIds.size());

      if (allowedImageIds == null) {
        allowedImageIds = splitImageIds;
      } else {
        // AND logic: keep only images that match both filters
        allowedImageIds.retainAll(splitImageIds);
      }
    }

    // Filter by tags at database level
    if (filters.getTags() != null && !filters.getTags().isEmpty()) {
      List<Long> tagImageIds =
          imageTagRepository.findImageIdsByProjectIdAndTagIds(projectId, filters.getTags());
      log.info(
          "Found {} images with tags matching filter: {}", tagImageIds.size(), filters.getTags());
      if (allowedImageIds == null) {
        allowedImageIds = new java.util.HashSet<>(tagImageIds);
      } else {
        // AND logic: keep only images that match both filters
        allowedImageIds.retainAll(tagImageIds);
      }
    }

    // Create pageable with sort by created_at descending, then by id descending for stable ordering
    // This ensures consistent pagination even when timestamps are identical
    Sort sort;
    if ("upload_time_asc".equals(sortBy)) {
      sort = Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
    } else {
      // Default to upload_time_desc
      sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
    }
    Pageable pageable = PageRequest.of(page, size, sort);

    // Get images - either filtered by IDs or all for project
    List<Image> allImages;
    long totalElements;
    int totalPages;
    boolean isFirst;
    boolean isLast;

    // Determine if we need to fetch all images first
    boolean needsFetchAll = allowedImageIds != null || sortByLabelTime;

    if (needsFetchAll) {
      // If we have pre-filtered IDs OR need to sort by label time, fetch all images first
      if (allowedImageIds != null && allowedImageIds.isEmpty()) {
        // No images match the label filters
        allImages = new ArrayList<>();
        totalElements = 0;
        totalPages = 0;
        isFirst = true;
        isLast = true;
      } else if (allowedImageIds != null) {
        // Fetch images by IDs
        allImages =
            imageRepository.findByIdInAndProject_Id(new ArrayList<>(allowedImageIds), projectId);
        // Apply other filters in memory
        if (filters.hasFilters()) {
          allImages = filterService.applyFilters(allImages, filters);
        }
        totalElements = allImages.size();
        totalPages = (int) Math.ceil((double) totalElements / size);
        isFirst = page == 0;
        isLast = page >= totalPages - 1;
      } else {
        // sortByLabelTime is true - fetch all images for the project
        allImages = imageRepository.findByProject_Id(projectId);
        // Apply other filters in memory
        if (filters.hasFilters()) {
          log.info("Applying filters: {}", filters);
          allImages = filterService.applyFilters(allImages, filters);
        }
        totalElements = allImages.size();
        totalPages = (int) Math.ceil((double) totalElements / size);
        isFirst = page == 0;
        isLast = page >= totalPages - 1;
      }
    } else {
      // No label filters and not sorting by label time, use standard pagination
      // BUT we still need to handle other filters properly
      if (filters.hasFilters()) {
        // If we have any filters, we need to fetch all images first
        log.info("Filters present, fetching all images for project {}", projectId);
        allImages = imageRepository.findByProject_Id(projectId);

        // Apply filters in memory
        log.info("Applying filters: {}", filters);
        allImages = filterService.applyFilters(allImages, filters);

        totalElements = allImages.size();
        totalPages = (int) Math.ceil((double) totalElements / size);
        isFirst = page == 0;
        isLast = page >= totalPages - 1;
      } else {
        // No filters at all, use efficient database pagination
        Page<Image> imagePage = imageRepository.findByProject_Id(projectId, pageable);
        allImages = new ArrayList<>(imagePage.getContent());

        totalElements = imagePage.getTotalElements();
        totalPages = imagePage.getTotalPages();
        isFirst = imagePage.isFirst();
        isLast = imagePage.isLast();
      }
    }

    List<Image> filteredImages = allImages;

    // Get annotation type filter for label filtering
    final String annotationTypeFilter = filters.getAnnotationType();
    final Long modelIdFilter = filters.getModelId();

    log.info(
        "Label filtering - annotationTypeFilter: '{}' (length: {}), modelIdFilter: {}",
        annotationTypeFilter,
        annotationTypeFilter != null ? annotationTypeFilter.length() : 0,
        modelIdFilter);

    // Trim the annotation type filter to handle any whitespace issues
    final String trimmedAnnotationType =
        annotationTypeFilter != null ? annotationTypeFilter.trim() : null;

    // Convert to DTOs with label information (filtered by annotation type and model)
    List<ImageListItemDTO> imageListItems =
        filteredImages.stream()
            .map(
                image ->
                    convertToImageListItemDTO(
                        image, trimmedAnnotationType, modelIdFilter, includeThumbnails))
            .collect(Collectors.toList());

    // Expand to instances view if requested
    if ("instances".equalsIgnoreCase(viewMode)) {
      log.info("Expanding images to instances view");
      imageListItems = expandToInstances(imageListItems);
      log.info("Expanded to {} instances", imageListItems.size());

      // Recalculate pagination for instances
      totalElements = imageListItems.size();
      totalPages = (int) Math.ceil((double) totalElements / size);
      isFirst = page == 0;
      isLast = page >= totalPages - 1;
    }

    // Apply sorting if provided
    if (sortBy != null && !sortBy.isEmpty()) {
      log.info("Applying sort method: {}", sortBy);
      imageListItems = sortService.sortImages(imageListItems, sortBy);
    }

    // Apply pagination manually if we fetched all images (for label time sorting or label filters
    // or any filters)
    // OR if we're in instances view (which changes the total count)
    if (allowedImageIds != null
        || sortByLabelTime
        || filters.hasFilters()
        || "instances".equalsIgnoreCase(viewMode)) {
      int fromIndex = page * size;
      int toIndex = Math.min(fromIndex + size, imageListItems.size());
      if (fromIndex < imageListItems.size()) {
        imageListItems = imageListItems.subList(fromIndex, toIndex);
      } else {
        imageListItems = new ArrayList<>();
      }
    }

    // Build paginated response
    PaginatedResponse<ImageListItemDTO> response = new PaginatedResponse<>();
    response.setContent(imageListItems);
    response.setPage(page);
    response.setSize(size);
    response.setTotalElements(totalElements);
    response.setTotalPages(totalPages);
    response.setFirst(isFirst);
    response.setLast(isLast);

    log.info(
        "Returning {} images for project {}, total elements: {}",
        imageListItems.size(),
        projectId,
        totalElements);

    return response;
  }

  /**
   * Convert Image entity to ImageListItemDTO with label overlay information.
   *
   * @param image the image entity
   * @param annotationType the annotation type filter (null for all, "Ground truth" or "Prediction")
   * @param modelId the model ID filter (null for all models, specific ID for that model's
   *     predictions)
   * @return the image list item DTO
   */
  private ImageListItemDTO convertToImageListItemDTO(
      Image image, String annotationType, Long modelId, boolean includeThumbnails) {
    List<LabelOverlayDTO> labelOverlays = new ArrayList<>();

    log.debug(
        "Converting image {} to DTO with annotationType: '{}', modelId: {}",
        image.getId(),
        annotationType,
        modelId);

    // Query ground truth labels if not filtered to Prediction only
    // Accept both "Ground-Truth" (from frontend) and "Ground truth" for backwards compatibility
    boolean isGroundTruthFilter =
        annotationType == null
            || "Ground-Truth".equalsIgnoreCase(annotationType)
            || "Ground truth".equalsIgnoreCase(annotationType);

    if (isGroundTruthFilter) {
      log.debug("Querying ground truth labels for image {}", image.getId());
      TypedQuery<ImageLabel> gtQuery =
          entityManager.createQuery(
              "SELECT il FROM ImageLabel il "
                  + "JOIN FETCH il.projectClass "
                  + "WHERE il.image.id = :imageId",
              ImageLabel.class);
      gtQuery.setParameter("imageId", image.getId());
      List<ImageLabel> gtLabels = gtQuery.getResultList();
      log.debug("Found {} ground truth labels for image {}", gtLabels.size(), image.getId());

      // Convert ground truth labels to DTOs - use "Ground Truth" to match database value
      labelOverlays.addAll(
          gtLabels.stream()
              .map(label -> convertToLabelOverlayDTO(label, "Ground Truth"))
              .collect(Collectors.toList()));
    } else {
      log.debug(
          "Skipping ground truth labels for image {} because annotationType='{}' doesn't match 'Ground-Truth'",
          image.getId(),
          annotationType);
    }

    // Query prediction labels if not filtered to Ground-Truth only
    if (annotationType == null || "Prediction".equalsIgnoreCase(annotationType)) {
      log.info(
          "Querying prediction labels for image {} with modelId filter: {}",
          image.getId(),
          modelId);

      // Use unified repository method for consistency with labelling page
      List<ImagePredictionLabel> predLabels;
      if (modelId != null) {
        // Filter by both image_id and model_id (unified approach)
        predLabels =
            imagePredictionLabelRepository.findByImage_IdAndModel_Id(image.getId(), modelId);
      } else {
        // No model filter - get all predictions for this image
        predLabels = imagePredictionLabelRepository.findByImage_Id(image.getId());
      }

      log.info(
          "Found {} prediction labels for image {} with modelId filter: {}",
          predLabels.size(),
          image.getId(),
          modelId);

      // Convert prediction labels to DTOs
      labelOverlays.addAll(
          predLabels.stream()
              .map(label -> convertPredictionToLabelOverlayDTO(label))
              .collect(Collectors.toList()));
    } else {
      log.info(
          "Skipping prediction labels for image {} because annotationType='{}' is not 'Prediction'",
          image.getId(),
          annotationType);
    }

    // Calculate label count
    Integer labelCount = labelOverlays.size();

    // Create and populate DTO
    ImageListItemDTO dto = new ImageListItemDTO();
    dto.setId(image.getId());
    dto.setFileName(image.getFileName());
    dto.setFileSize(image.getFileSize());
    dto.setWidth(image.getWidth());
    dto.setHeight(image.getHeight());
    dto.setSplit(image.getSplit());
    dto.setIsLabeled(image.getIsLabeled());
    dto.setIsNoClass(image.getIsNoClass());
    dto.setLabelCount(labelCount);
    dto.setThumbnailImage(includeThumbnails ? image.getThumbnailImage() : null);
    dto.setThumbnailWidthRatio(image.getThumbnailWidthRatio());
    dto.setThumbnailHeightRatio(image.getThumbnailHeightRatio());
    dto.setCreatedAt(image.getCreatedAt());
    dto.setLabels(labelOverlays);

    return dto;
  }

  /**
   * Convert ImageLabel entity to LabelOverlayDTO.
   *
   * <p>Convert ImageLabel entity to LabelOverlayDTO. Ground truth labels always have
   * annotationType="Ground Truth" and confidenceRate=null
   *
   * @param label the image label entity (ground truth)
   * @param annotationType the annotation type to set (should always be "Ground Truth")
   * @return the label overlay DTO
   */
  private LabelOverlayDTO convertToLabelOverlayDTO(ImageLabel label, String annotationType) {
    LabelOverlayDTO dto = new LabelOverlayDTO();
    dto.setId(label.getId());
    dto.setClassId(label.getProjectClass().getId());
    dto.setClassName(label.getProjectClass().getClassName());
    dto.setColorCode(label.getProjectClass().getColorCode());
    dto.setPosition(label.getPosition());
    dto.setConfidenceRate(null); // Ground truth labels don't have confidence rate
    dto.setAnnotationType(annotationType); // Always "Ground Truth" for ImageLabel
    dto.setCreatedAt(label.getCreatedAt());
    return dto;
  }

  /**
   * Convert ImagePredictionLabel entity to LabelOverlayDTO.
   *
   * @param label the image prediction label entity
   * @return the label overlay DTO
   */
  private LabelOverlayDTO convertPredictionToLabelOverlayDTO(ImagePredictionLabel label) {
    LabelOverlayDTO dto = new LabelOverlayDTO();
    dto.setId(label.getId());
    dto.setClassId(label.getProjectClass().getId());
    dto.setClassName(label.getProjectClass().getClassName());
    dto.setColorCode(label.getProjectClass().getColorCode());
    dto.setPosition(label.getPosition());
    dto.setConfidenceRate(label.getConfidenceRate());
    dto.setAnnotationType("Prediction");
    dto.setCreatedAt(label.getCreatedAt());
    return dto;
  }

  /**
   * Expand images with multiple labels into separate instances (one per label). For instances view,
   * only ground truth labels are included. Images without ground truth labels are filtered out. For
   * images with 0 or 1 ground truth label, they remain as-is. For images with multiple ground truth
   * labels, create a separate DTO for each label.
   *
   * @param imageListItems the list of image DTOs
   * @return expanded list with one instance per ground truth label
   */
  private List<ImageListItemDTO> expandToInstances(List<ImageListItemDTO> imageListItems) {
    List<ImageListItemDTO> instances = new ArrayList<>();

    for (ImageListItemDTO image : imageListItems) {
      List<LabelOverlayDTO> labels = image.getLabels();

      // Filter to only ground truth labels
      List<LabelOverlayDTO> groundTruthLabels =
          labels != null
              ? labels.stream()
                  .filter(
                      label ->
                          "Ground Truth".equalsIgnoreCase(label.getAnnotationType())
                              || "Ground-Truth".equalsIgnoreCase(label.getAnnotationType()))
                  .collect(Collectors.toList())
              : new ArrayList<>();

      // Skip images without ground truth labels in instances view
      if (groundTruthLabels.isEmpty()) {
        continue;
      }

      if (groundTruthLabels.size() == 1) {
        // Single ground truth label - keep as-is but update labels list to only show ground truth
        // Set the focused label for instance view zoom
        image.setFocusedLabel(groundTruthLabels.get(0));
        image.setInstanceLabelId(groundTruthLabels.get(0).getId());
        image.setLabels(groundTruthLabels);
        image.setLabelCount(1);
        instances.add(image);
      } else {
        // Multiple ground truth labels - create one instance per label
        for (LabelOverlayDTO label : groundTruthLabels) {
          ImageListItemDTO instance = new ImageListItemDTO();

          // Copy all image properties
          instance.setId(image.getId());
          instance.setFileName(image.getFileName());
          instance.setFileSize(image.getFileSize());
          instance.setWidth(image.getWidth());
          instance.setHeight(image.getHeight());
          instance.setSplit(image.getSplit());
          instance.setIsLabeled(image.getIsLabeled());
          instance.setIsNoClass(image.getIsNoClass());
          instance.setLabelCount(1); // Each instance shows only one label
          instance.setThumbnailImage(image.getThumbnailImage());
          instance.setThumbnailWidthRatio(image.getThumbnailWidthRatio());
          instance.setThumbnailHeightRatio(image.getThumbnailHeightRatio());
          instance.setCreatedAt(image.getCreatedAt());

          // Set instance-specific fields
          instance.setInstanceLabelId(label.getId());
          instance.setFocusedLabel(label);

          // Set labels list to contain only this label
          instance.setLabels(java.util.Collections.singletonList(label));

          instances.add(instance);
        }
      }
    }

    return instances;
  }

  /**
   * Generate a compressed thumbnail for an image.
   *
   * @param file the image file
   * @return the thumbnail as byte array
   * @throws ImageProcessingException if thumbnail generation fails
   */
  private byte[] generateThumbnail(MultipartFile file) {
    try {
      // Read original image
      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      if (originalImage == null) {
        throw new ImageProcessingException(
            file.getOriginalFilename(), "read image", new IOException("Unable to read image"));
      }

      // Calculate thumbnail dimensions maintaining aspect ratio
      int originalWidth = originalImage.getWidth();
      int originalHeight = originalImage.getHeight();
      int thumbnailWidth;
      int thumbnailHeight;

      if (originalWidth > originalHeight) {
        thumbnailWidth = THUMBNAIL_MAX_SIZE;
        thumbnailHeight = (int) ((double) originalHeight / originalWidth * THUMBNAIL_MAX_SIZE);
      } else {
        thumbnailHeight = THUMBNAIL_MAX_SIZE;
        thumbnailWidth = (int) ((double) originalWidth / originalHeight * THUMBNAIL_MAX_SIZE);
      }

      // Create thumbnail image
      BufferedImage thumbnailImage =
          new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = thumbnailImage.createGraphics();
      graphics.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
      graphics.dispose();

      // Convert to byte array (JPEG format for compression)
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(thumbnailImage, "jpg", outputStream);
      byte[] thumbnailBytes = outputStream.toByteArray();

      log.debug(
          "Generated thumbnail for {}: {}x{} -> {}x{}, size: {} bytes",
          file.getOriginalFilename(),
          originalWidth,
          originalHeight,
          thumbnailWidth,
          thumbnailHeight,
          thumbnailBytes.length);

      return thumbnailBytes;

    } catch (IOException e) {
      throw new ImageProcessingException(file.getOriginalFilename(), "generate thumbnail", e);
    }
  }

  /**
   * Calculate the scale ratios between thumbnail and original image. These ratios are used to scale
   * label coordinates when displaying them on thumbnails.
   *
   * @param file the original image file
   * @param thumbnailWidth the thumbnail width
   * @param thumbnailHeight the thumbnail height
   * @return the thumbnail ratios (width and height ratios separately)
   * @throws ImageProcessingException if calculation fails
   */
  private ThumbnailRatios calculateThumbnailRatios(
      MultipartFile file, int thumbnailWidth, int thumbnailHeight) {
    try {
      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      if (originalImage == null) {
        throw new ImageProcessingException(
            file.getOriginalFilename(),
            "calculate scale ratios",
            new IOException("Unable to read image"));
      }

      int originalWidth = originalImage.getWidth();
      int originalHeight = originalImage.getHeight();

      // Calculate both width and height ratios separately
      double widthRatio = (double) thumbnailWidth / originalWidth;
      double heightRatio = (double) thumbnailHeight / originalHeight;

      log.debug(
          "Calculated scale ratios for {}: width={}, height={} (thumbnail: {}x{}, original: {}x{})",
          file.getOriginalFilename(),
          widthRatio,
          heightRatio,
          thumbnailWidth,
          thumbnailHeight,
          originalWidth,
          originalHeight);

      return new ThumbnailRatios(widthRatio, heightRatio);

    } catch (IOException e) {
      throw new ImageProcessingException(file.getOriginalFilename(), "calculate scale ratios", e);
    }
  }

  /**
   * Extract metadata (width and height) from an image file.
   *
   * @param file the image file
   * @return the image metadata
   * @throws ImageProcessingException if metadata extraction fails
   */
  private ImageMetadata extractMetadata(MultipartFile file) {
    try {
      BufferedImage image = ImageIO.read(file.getInputStream());
      if (image == null) {
        throw new ImageProcessingException(
            file.getOriginalFilename(),
            "extract metadata",
            new IOException("Unable to read image"));
      }

      int width = image.getWidth();
      int height = image.getHeight();

      log.debug("Extracted metadata for {}: {}x{}", file.getOriginalFilename(), width, height);

      return new ImageMetadata(width, height);

    } catch (IOException e) {
      throw new ImageProcessingException(file.getOriginalFilename(), "extract metadata", e);
    }
  }

  /**
   * Validate image file format and size.
   *
   * @param file the image file
   * @throws InvalidImageFormatException if file format is not supported
   * @throws ImageProcessingException if file size exceeds limit
   */
  private void validateImageFile(MultipartFile file) {
    // Check file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ImageProcessingException(
          String.format(
              "File size exceeds maximum allowed size of %d MB for file '%s'",
              MAX_FILE_SIZE / (1024 * 1024), file.getOriginalFilename()));
    }

    // Check file format
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isEmpty()) {
      throw new InvalidImageFormatException("File name is empty");
    }

    String extension = getFileExtension(originalFilename).toLowerCase();
    if (!ALLOWED_FORMATS.contains(extension)) {
      throw new InvalidImageFormatException(originalFilename, extension);
    }

    log.debug("Validated image file: {}, size: {} bytes", originalFilename, file.getSize());
  }

  /**
   * Get file extension from filename.
   *
   * @param filename the filename
   * @return the file extension (without dot)
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
      return filename.substring(lastDotIndex + 1);
    }
    return "";
  }

  /**
   * Generate a unique filename to prevent overwrites during export. Format:
   * {imageId}_{timestamp}_{originalFilename}
   *
   * @param imageId the image ID
   * @param originalFilename the original filename
   * @return unique filename
   */
  private String generateUniqueFilename(Long imageId, String originalFilename) {
    if (originalFilename == null || originalFilename.isEmpty()) {
      originalFilename = "image.jpg";
    }

    // Sanitize original filename (remove path separators and special chars)
    String sanitized = originalFilename.replaceAll("[/\\\\:*?\"<>|]", "_");

    // Generate unique filename: {imageId}_{timestamp}_{originalFilename}
    String timestamp = String.valueOf(System.currentTimeMillis());
    String uniqueFilename = imageId + "_" + timestamp + "_" + sanitized;

    log.debug("Generated unique filename: {} from original: {}", uniqueFilename, originalFilename);

    return uniqueFilename;
  }

  /**
   * Batch set metadata for multiple images
   *
   * @param requestBody map containing imageIds, metadata, and createdBy
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(path = "/batch-set-metadata", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Map<String, Object>> batchSetMetadata(
      @RequestBody Map<String, Object> requestBody) {

    // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
    @SuppressWarnings("unchecked")
    List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
    List<Long> imageIds =
        imageIdsRaw.stream()
            .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
            .collect(Collectors.toList());

    @SuppressWarnings("unchecked")
    Map<String, String> metadata = (Map<String, String>) requestBody.get("metadata");
    String userId = (String) requestBody.get("userId");

    log.info(
        "Operational REST: Batch setting metadata for {} images by user: {}",
        imageIds.size(),
        userId);

    // Get the project ID from the first image to find project metadata
    Image firstImage =
        imageRepository
            .findById(imageIds.get(0))
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Image not found with id: " + imageIds.get(0)));
    Long projectId = firstImage.getProject().getId();

    // First, remove ALL existing metadata for these images
    for (Long imageId : imageIds) {
      List<com.nxp.iemdm.model.landingai.ImageMetadata> existingMetadata =
          imageMetadataRepository.findByImage_Id(imageId);
      imageMetadataRepository.deleteAll(existingMetadata);
    }

    // Process each metadata field and add new metadata
    for (Map.Entry<String, String> entry : metadata.entrySet()) {
      String metadataName = entry.getKey();
      String metadataValue = entry.getValue();

      // Skip empty values
      if (metadataValue == null || metadataValue.trim().isEmpty()) {
        continue;
      }

      // Find the project metadata by name
      com.nxp.iemdm.model.landingai.ProjectMetadata projectMetadata =
          projectMetadataRepository
              .findByProject_IdAndName(projectId, metadataName)
              .orElseThrow(
                  () ->
                      new jakarta.persistence.EntityNotFoundException(
                          "Project metadata not found with name: " + metadataName));

      // Create new metadata for each image
      for (Long imageId : imageIds) {
        com.nxp.iemdm.model.landingai.ImageMetadata imageMetadata =
            new com.nxp.iemdm.model.landingai.ImageMetadata();
        Image image = new Image();
        image.setId(imageId);
        imageMetadata.setImage(image);
        imageMetadata.setProjectMetadata(projectMetadata);
        imageMetadata.setValue(metadataValue);
        imageMetadata.setCreatedBy(userId);
        imageMetadataRepository.save(imageMetadata);
      }
    }

    log.info("Batch set metadata complete for {} images", imageIds.size());

    Map<String, Object> response = new java.util.HashMap<>();
    response.put("success", true);
    response.put("updatedCount", imageIds.size());
    return ResponseEntity.ok(response);
  }

  /**
   * Batch set tags for multiple images
   *
   * @param requestBody map containing imageIds, tagIds, and userId
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(path = "/batch-set-tags", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Map<String, Object>> batchSetTags(
      @RequestBody Map<String, Object> requestBody) {

    // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
    @SuppressWarnings("unchecked")
    List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
    List<Long> imageIds =
        imageIdsRaw.stream()
            .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
            .collect(Collectors.toList());

    @SuppressWarnings("unchecked")
    List<?> tagIdsRaw = (List<?>) requestBody.get("tagIds");
    List<Long> tagIds =
        tagIdsRaw.stream()
            .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
            .collect(Collectors.toList());

    String userId = (String) requestBody.get("userId");

    log.info(
        "Operational REST: Batch setting tags for {} images with {} tags by user: {}",
        imageIds.size(),
        tagIds.size(),
        userId);

    // Get the project ID from the first image to validate tags belong to the same project
    Image firstImage =
        imageRepository
            .findById(imageIds.get(0))
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Image not found with id: " + imageIds.get(0)));
    Long projectId = firstImage.getProject().getId();

    // Validate all tags belong to the project
    // Note: Tag validation is skipped here as we trust the frontend sends valid tag IDs
    // The foreign key constraint in the database will prevent invalid tag IDs

    // Update tags for each image
    for (Long imageId : imageIds) {
      // Remove existing tags for this image
      List<com.nxp.iemdm.model.landingai.ImageTag> existingTags =
          imageTagRepository.findByImage_Id(imageId);
      imageTagRepository.deleteAll(existingTags);

      // Add new tags
      for (Long tagId : tagIds) {
        com.nxp.iemdm.model.landingai.ImageTag imageTag =
            new com.nxp.iemdm.model.landingai.ImageTag();
        Image image = new Image();
        image.setId(imageId);
        imageTag.setImage(image);

        com.nxp.iemdm.model.landingai.ProjectTag projectTag =
            new com.nxp.iemdm.model.landingai.ProjectTag();
        projectTag.setId(tagId);
        imageTag.setProjectTag(projectTag);

        imageTag.setCreatedBy(userId);
        imageTagRepository.save(imageTag);
      }
    }

    log.info("Batch set tags complete for {} images", imageIds.size());

    Map<String, Object> response = new java.util.HashMap<>();
    response.put("success", true);
    response.put("updatedCount", imageIds.size());
    return ResponseEntity.ok(response);
  }

  /**
   * Batch set class for multiple images (Classification projects)
   *
   * @param requestBody map containing imageIds, classId, and userId
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(path = "/batch-set-class", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Map<String, Object>> batchSetClass(
      @RequestBody Map<String, Object> requestBody) {

    // JSON deserialization produces Integer for numeric values, need to convert to Long
    @SuppressWarnings("unchecked")
    List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
    List<Long> imageIds =
        imageIdsRaw.stream()
            .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
            .collect(Collectors.toList());

    Object classIdRaw = requestBody.get("classId");
    Long classId =
        classIdRaw instanceof Integer ? ((Integer) classIdRaw).longValue() : (Long) classIdRaw;

    String userId = (String) requestBody.get("userId");

    log.info(
        "Operational REST: Batch setting class {} for {} images by user: {}",
        classId,
        imageIds.size(),
        userId);

    // Validate the class exists
    com.nxp.iemdm.model.landingai.ProjectClass projectClass =
        projectClassRepository
            .findById(classId)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Project class not found with id: " + classId));

    // STEP 1: Delete ALL existing labels for these images in one query
    log.info("Deleting existing labels for {} images", imageIds.size());
    log.info("Image IDs to process: {}", imageIds);

    // Use direct DELETE query for efficiency and to avoid lazy loading issues
    int deletedCount = imageLabelRepository.deleteByImageIds(imageIds);
    log.info("Deleted {} labels using direct DELETE query", deletedCount);

    // Flush to ensure deletions are committed before inserts
    imageLabelRepository.flush();

    // Verify deletion was successful (debug mode only)
    if (log.isDebugEnabled()) {
      List<ImageLabel> remainingLabels = imageLabelRepository.findByImageIds(imageIds);
      if (!remainingLabels.isEmpty()) {
        log.error("DELETION FAILED: Still found {} labels after deletion!", remainingLabels.size());
        remainingLabels.forEach(
            label ->
                log.error(
                    "Remaining label ID {} for image ID {} with class ID {}",
                    label.getId(),
                    label.getImage().getId(),
                    label.getProjectClass().getId()));
      } else {
        log.debug("Deletion verified: No Ground Truth labels remain");
      }
    }

    // STEP 2: Create new labels for all images in one batch
    log.info("Creating new labels for {} images with class {}", imageIds.size(), classId);
    List<ImageLabel> newLabels = new ArrayList<>();
    for (Long imageId : imageIds) {
      ImageLabel label = new ImageLabel();
      Image image = new Image();
      image.setId(imageId);
      label.setImage(image);
      label.setProjectClass(projectClass);
      label.setCreatedBy(userId);
      label.setPosition(null); // Classification projects don't use position
      newLabels.add(label);
    }

    // Save all labels at once
    imageLabelRepository.saveAll(newLabels);
    imageLabelRepository.flush();

    // STEP 3: Validate that each image has exactly one label (debug logging)
    if (log.isDebugEnabled()) {
      for (Long imageId : imageIds) {
        long labelCount = imageLabelRepository.countByImage_Id(imageId);
        if (labelCount != 1) {
          log.error(
              "VALIDATION FAILED: Image {} has {} labels after batch set class!",
              imageId,
              labelCount);
        }
      }
    }

    log.info("Batch set class complete for {} images", imageIds.size());

    Map<String, Object> response = new java.util.HashMap<>();
    response.put("success", true);
    response.put("updatedCount", imageIds.size());
    return ResponseEntity.ok(response);
  }

  /**
   * Delete multiple images in batch
   *
   * @param requestBody map containing the list of image IDs to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @PostMapping(path = "/delete-batch", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Void> deleteImagesBatch(@RequestBody Map<String, List<Long>> requestBody) {
    List<Long> imageIds = requestBody.get("imageIds");

    if (imageIds == null || imageIds.isEmpty()) {
      log.warn("Delete batch called with empty or null imageIds");
      return ResponseEntity.badRequest().build();
    }

    log.info("Batch deleting {} images", imageIds.size());

    for (Long imageId : imageIds) {
      try {
        if (!imageRepository.existsById(imageId)) {
          log.warn("Image not found during batch delete: {}", imageId);
          continue;
        }

        // Cascade delete associated data
        imageLabelRepository.deleteByImage_Id(imageId);
        imagePredictionLabelRepository.deleteByImage_Id(imageId);
        imageTagRepository.deleteByImage_Id(imageId);
        imageMetadataRepository.deleteByImage_Id(imageId);

        // Delete the image record
        imageRepository.deleteById(imageId);

        log.debug("Deleted image {} and all associated data", imageId);
      } catch (Exception e) {
        log.error("Error deleting image {} during batch delete: {}", imageId, e.getMessage());
        // Continue with other images
      }
    }

    log.info("Batch delete complete for {} images", imageIds.size());
    return ResponseEntity.noContent().build();
  }

  // ==================== CRUD Endpoints (merged from ImageController) ====================

  /**
   * Create a new image record
   *
   * @param image the image to create
   * @return the created image
   */
  @MethodLog
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Image> createImage(@RequestBody Image image) {
    try {
      Image savedImage = imageService.saveImage(image);
      return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(savedImage);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating image: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating image", e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Get an image by ID
   *
   * @param imageId the image ID
   * @return the image
   */
  @MethodLog
  @GetMapping(path = "/{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  public ResponseEntity<Image> getImageById(@PathVariable("imageId") Long imageId) {
    try {
      Image image = imageService.getImageById(imageId);
      return ResponseEntity.ok(image);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Get image file content from file system
   *
   * @param fileName the file name
   * @return the file content as byte array
   */
  @MethodLog
  @GetMapping(path = "/file/{fileName}", produces = "image/*")
  public ResponseEntity<byte[]> getImageFileByName(@PathVariable("fileName") String fileName) {
    try {
      byte[] fileContent = imageService.getImageFromFileSystem(fileName);
      return ResponseEntity.ok()
          .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
          .body(fileContent);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("File not found: {}", fileName);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Invalid file name: {}", fileName);
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error retrieving file {}", fileName, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Update the split value for an image
   *
   * @param imageId the image ID
   * @param request the request body containing the split value
   * @return the updated image
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/split",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Image> updateImageSplit(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, String> request) {
    try {
      String split = request.get("split");
      Image updatedImage = imageService.updateImageSplit(imageId, split);
      return ResponseEntity.ok(updatedImage);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error updating image split: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error updating image split for {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Update the isNoClass flag for an image
   *
   * @param imageId the image ID
   * @param request the request body containing the isNoClass value
   * @return the updated image
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/is-no-class",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<Image> updateIsNoClass(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Boolean> request) {
    try {
      Boolean isNoClass = request.get("isNoClass");
      if (isNoClass == null) {
        isNoClass = false;
      }
      Image updatedImage = imageService.updateIsNoClass(imageId, isNoClass);
      return ResponseEntity.ok(updatedImage);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error updating isNoClass for image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Delete an image and all associated data
   *
   * @param imageId the image ID to delete
   * @return no content on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}")
  @Transactional
  public ResponseEntity<Void> deleteImage(@PathVariable("imageId") Long imageId) {
    try {
      imageService.deleteImage(imageId);
      return ResponseEntity.noContent().build();
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error deleting image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  // ==================== Image Tags Endpoints ====================

  /**
   * Get all tags for an image
   *
   * @param imageId the image ID
   * @return list of image tags
   */
  @MethodLog
  @GetMapping(path = "/{imageId}/tags", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  public ResponseEntity<List<com.nxp.iemdm.model.landingai.ImageTag>> getImageTags(
      @PathVariable("imageId") Long imageId) {
    try {
      List<com.nxp.iemdm.model.landingai.ImageTag> tags = imageService.getImageTags(imageId);
      return ResponseEntity.ok(tags);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving tags for image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Add a tag to an image
   *
   * @param imageId the image ID
   * @param request the request body containing tagId and createdBy
   * @return the created image tag
   */
  @MethodLog
  @PostMapping(
      path = "/{imageId}/tags",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<com.nxp.iemdm.model.landingai.ImageTag> addImageTag(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Object> request) {
    try {
      Long tagId = Long.valueOf(request.get("tagId").toString());
      String createdBy = (String) request.get("createdBy");
      com.nxp.iemdm.model.landingai.ImageTag imageTag =
          imageService.addImageTag(imageId, tagId, createdBy);
      return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(imageTag);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image or tag not found: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error adding tag to image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Remove a tag from an image
   *
   * @param imageId the image ID
   * @param tagId the image tag ID
   * @return no content on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}/tags/{tagId}")
  @Transactional
  public ResponseEntity<Void> removeImageTag(
      @PathVariable("imageId") Long imageId, @PathVariable("tagId") Long tagId) {
    try {
      imageService.removeImageTag(imageId, tagId);
      return ResponseEntity.noContent().build();
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image tag not found: {}", tagId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error removing tag {} from image {}", tagId, imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Update image tags in batch (replace all tags)
   *
   * @param imageId the image ID
   * @param request the request body containing tagIds and createdBy
   * @return list of updated image tags
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/tags",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<List<com.nxp.iemdm.model.landingai.ImageTag>> updateImageTags(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Object> request) {
    try {
      // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
      @SuppressWarnings("unchecked")
      List<?> tagIdsRaw = (List<?>) request.get("tagIds");
      List<Long> tagIds =
          tagIdsRaw.stream()
              .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
              .collect(Collectors.toList());

      String createdBy = (String) request.get("createdBy");
      List<com.nxp.iemdm.model.landingai.ImageTag> tags =
          imageService.updateImageTags(imageId, tagIds, createdBy);
      return ResponseEntity.ok(tags);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image or tag not found: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error updating tags for image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  // ==================== Image Metadata Endpoints ====================

  /**
   * Get all metadata for an image
   *
   * @param imageId the image ID
   * @return list of image metadata
   */
  @MethodLog
  @GetMapping(path = "/{imageId}/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional(readOnly = true)
  public ResponseEntity<List<com.nxp.iemdm.model.landingai.ImageMetadata>> getImageMetadata(
      @PathVariable("imageId") Long imageId) {
    try {
      List<com.nxp.iemdm.model.landingai.ImageMetadata> metadata =
          imageService.getImageMetadata(imageId);
      return ResponseEntity.ok(metadata);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image not found: {}", imageId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving metadata for image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Add metadata to an image
   *
   * @param imageId the image ID
   * @param request the request body containing metadataId, value, and createdBy
   * @return the created image metadata
   */
  @MethodLog
  @PostMapping(
      path = "/{imageId}/metadata",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<com.nxp.iemdm.model.landingai.ImageMetadata> addImageMetadata(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Object> request) {
    try {
      Long metadataId = Long.valueOf(request.get("metadataId").toString());
      String value = (String) request.get("value");
      String createdBy = (String) request.get("createdBy");
      com.nxp.iemdm.model.landingai.ImageMetadata imageMetadata =
          imageService.addImageMetadata(imageId, metadataId, value, createdBy);
      return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(imageMetadata);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image or metadata not found: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error adding metadata to image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Update an image metadata value
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @param request the request body containing the new value
   * @return the updated image metadata
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/metadata/{metadataId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<com.nxp.iemdm.model.landingai.ImageMetadata> updateImageMetadata(
      @PathVariable("imageId") Long imageId,
      @PathVariable("metadataId") Long metadataId,
      @RequestBody Map<String, String> request) {
    try {
      String value = request.get("value");
      com.nxp.iemdm.model.landingai.ImageMetadata imageMetadata =
          imageService.updateImageMetadata(imageId, metadataId, value);
      return ResponseEntity.ok(imageMetadata);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image metadata not found: {}", metadataId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error updating metadata {} for image {}", metadataId, imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Remove metadata from an image
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @return no content on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}/metadata/{metadataId}")
  @Transactional
  public ResponseEntity<Void> removeImageMetadata(
      @PathVariable("imageId") Long imageId, @PathVariable("metadataId") Long metadataId) {
    try {
      imageService.removeImageMetadata(imageId, metadataId);
      return ResponseEntity.noContent().build();
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image metadata not found: {}", metadataId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error removing metadata {} from image {}", metadataId, imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Update image metadata in batch (replace all metadata)
   *
   * @param imageId the image ID
   * @param request the request body containing metadataList and createdBy
   * @return list of updated image metadata
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/metadata",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<List<com.nxp.iemdm.model.landingai.ImageMetadata>> updateImageMetadataBatch(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Object> request) {
    try {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> metadataListRaw =
          (List<Map<String, Object>>) request.get("metadataList");
      String createdBy = (String) request.get("createdBy");

      // Convert to MetadataInput list
      List<com.nxp.iemdm.operational.service.landingai.ImageService.MetadataInput> metadataList =
          metadataListRaw.stream()
              .map(
                  m ->
                      new com.nxp.iemdm.operational.service.landingai.ImageService.MetadataInput(
                          Long.valueOf(m.get("metadataId").toString()), (String) m.get("value")))
              .toList();

      List<com.nxp.iemdm.model.landingai.ImageMetadata> metadata =
          imageService.updateImageMetadataBatch(imageId, metadataList, createdBy);
      return ResponseEntity.ok(metadata);
    } catch (com.nxp.iemdm.exception.NotFoundException e) {
      log.error("Image or metadata not found: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error updating metadata for image {}", imageId, e);
      return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Determine content type from file name extension
   *
   * @param fileName the file name
   * @return the MIME type string
   */
  private String getContentTypeFromFileName(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "image/jpeg"; // default
    }

    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    switch (extension) {
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "bmp":
        return "image/bmp";
      case "webp":
        return "image/webp";
      case "svg":
        return "image/svg+xml";
      default:
        return "image/jpeg"; // default fallback
    }
  }

  /** Inner class to hold image metadata. */
  private static class ImageMetadata {
    public final int width;
    public final int height;

    public ImageMetadata(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  /** Inner class to hold thumbnail scale ratios. */
  private static class ThumbnailRatios {
    public final double widthRatio;
    public final double heightRatio;

    public ThumbnailRatios(double widthRatio, double heightRatio) {
      this.widthRatio = widthRatio;
      this.heightRatio = heightRatio;
    }
  }
}
