package com.nxp.iemdm.service;

import com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.SnapshotDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotPreviewStatsDTO;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Service interface for Landing AI snapshot operations. */
public interface SnapshotService {

  /**
   * Create a new snapshot for a project.
   *
   * @param request the snapshot creation request
   * @param userId the user identifier
   * @return the created snapshot
   */
  SnapshotDTO createSnapshot(SnapshotCreateRequest request, String userId);

  /**
   * Get all snapshots for a project.
   *
   * @param projectId the project ID
   * @return list of snapshots
   */
  List<SnapshotDTO> getSnapshotsForProject(Long projectId);

  /**
   * Get snapshot by ID.
   *
   * @param id the snapshot ID
   * @return the snapshot details
   */
  SnapshotDTO getSnapshotById(Long id);

  /**
   * Delete snapshot by ID.
   *
   * @param id the snapshot ID
   * @param userId the user identifier
   */
  void deleteSnapshot(Long id, String userId);

  /**
   * Get snapshot preview stats for a project.
   *
   * @param projectId the project ID
   * @return preview stats including image status and split distribution
   */
  SnapshotPreviewStatsDTO getSnapshotPreviewStats(Long projectId);

  /**
   * Get paginated images for a snapshot with optional sorting and filtering. Requirements: 1.1,
   * 3.1, 3.2
   *
   * @param snapshotId the snapshot ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param sortBy the sort method
   * @param filterRequest the filter criteria (optional)
   * @return paginated response with image list items
   */
  Object getSnapshotImages(
      @NotNull Long snapshotId,
      int page,
      int size,
      String sortBy,
      ImageFilterRequest filterRequest);

  /**
   * Create a new project from a snapshot.
   *
   * @param snapshotId the snapshot ID to create project from
   * @param projectName the name for the new project
   * @param userId the user identifier
   * @return the created project DTO
   */
  ProjectDTO createProjectFromSnapshot(Long snapshotId, String projectName, String userId);

  /**
   * Revert a project to a snapshot state.
   *
   * @param snapshotId the snapshot ID to revert to
   * @param projectId the project ID to revert
   * @param userId the user identifier
   */
  void revertProjectToSnapshot(Long snapshotId, Long projectId, String userId);

  /**
   * Download snapshot dataset as a byte array.
   *
   * @param snapshotId the snapshot ID
   * @return byte array containing the snapshot data
   */
  byte[] downloadSnapshotDataset(Long snapshotId);

  /**
   * Get project classes from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of project classes from snapshot
   */
  Object getSnapshotClasses(Long snapshotId);

  /**
   * Get project tags from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of project tags from snapshot
   */
  Object getSnapshotTags(Long snapshotId);

  /**
   * Get project metadata from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of project metadata from snapshot
   */
  Object getSnapshotMetadata(Long snapshotId);

  /**
   * Get project splits from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of project splits from snapshot
   */
  Object getSnapshotSplits(Long snapshotId);
}
