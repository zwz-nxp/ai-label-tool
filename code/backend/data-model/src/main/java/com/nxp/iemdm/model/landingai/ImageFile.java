package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@ToString(exclude = "imageFileStream")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(sequenceName = "image_sequence", allocationSize = 1, name = "image_sequence")
@Table(name = "la_images_file")
public class ImageFile implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_sequence")
  @Column(name = "id")
  private Long id;

  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "image_file_stream", nullable = false, columnDefinition = "bytea")
  private byte[] imageFileStream;

  @Column(name = "legacy_image_id")
  private Long legacyImageId;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
