package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nxp.iemdm.model.location.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@SequenceGenerator(
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Table(name = "la_model_param")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ModelParam implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  @NotNull
  private Location location;

  @Column(name = "model_name", nullable = false, length = 50)
  @NotBlank
  private String modelName;

  @Column(name = "model_type", nullable = false, length = 50)
  @NotBlank
  private String modelType; // Object Detection, Classification, Segmentation

  @Column(name = "parameters", columnDefinition = "TEXT")
  private String parameters; // JSON string: {"param1": "value1", "param2": "value2"}

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 96)
  private String createdBy;
}
