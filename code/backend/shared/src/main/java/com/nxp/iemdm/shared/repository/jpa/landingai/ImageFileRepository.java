package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ImageFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile, Long> {

  /**
   * Find image file by file ID without loading the full binary data. Useful for checking existence.
   *
   * @param fileId the file ID
   * @return true if image file exists
   */
  boolean existsById(Long fileId);

  /**
   * Get only the file stream for a file (optimized query).
   *
   * @param fileId the file ID
   * @return the image file stream as byte array
   */
  @Query("SELECT imf.imageFileStream FROM ImageFile imf WHERE imf.id = :fileId")
  Optional<byte[]> findImageFileStreamById(@Param("fileId") Long fileId);

  /**
   * Find image file by legacy image ID (for migration support).
   *
   * @param legacyImageId the legacy image ID
   * @return the image file if found
   */
  Optional<ImageFile> findByLegacyImageId(Long legacyImageId);
}
