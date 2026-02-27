package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Table(name = "la_loss_chart")
public class LossChart implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_id", nullable = false)
  private Model model;

  @Column(name = "loss", precision = 5, scale = 2)
  private java.math.BigDecimal loss;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 50)
  private String createdBy;
}
