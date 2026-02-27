package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.model.landingai.SnapshotProjectClass;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotProjectClassRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Component for mapping class sequence numbers to actual class IDs. Handles the conversion between
 * API sequence numbers and database class IDs.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClassIdMapper {

  private final SnapshotProjectClassRepository snapshotProjectClassRepository;

  /**
   * Build mapping from sequence to class ID for a specific project and snapshot.
   *
   * @param projectId Project ID
   * @param snapshotId Snapshot ID
   * @return Map of sequence number to class ID
   */
  public Map<Integer, Long> buildSequenceToClassIdMap(Long projectId, Long snapshotId) {
    log.debug("Building class ID mapping for projectId: {}, snapshotId: {}", projectId, snapshotId);

    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findByProjectIdAndSnapshotId(projectId, snapshotId);

    if (classes.isEmpty()) {
      log.warn(
          "No classes found for projectId: {}, snapshotId: {}. Mapping will be empty.",
          projectId,
          snapshotId);
      return new HashMap<>();
    }

    Map<Integer, Long> mapping =
        classes.stream()
            .collect(
                Collectors.toMap(
                    SnapshotProjectClass::getSequence,
                    SnapshotProjectClass::getId,
                    (existing, replacement) -> {
                      log.warn("Duplicate sequence found: {}. Using first occurrence.", existing);
                      return existing;
                    }));

    log.debug("Built class ID mapping with {} entries", mapping.size());
    return mapping;
  }

  /**
   * Map sequence number to class ID using the provided mapping.
   *
   * @param sequence Sequence number from API
   * @param mapping Sequence to class ID mapping
   * @return Class ID or null if not found
   */
  public Long mapSequenceToClassId(Integer sequence, Map<Integer, Long> mapping) {
    if (sequence == null) {
      log.warn("Sequence is null, cannot map to class ID");
      return null;
    }

    Long classId = mapping.get(sequence);

    if (classId == null) {
      log.warn("No class ID found for sequence: {}", sequence);
    }

    return classId;
  }
}
