package com.nxp.iemdm.model.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GLOBAL_GLOSSARY")
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class GlossaryItem {
  @Id
  @Column(name = "TERM")
  @NotBlank
  private String term;

  @Column(name = "SHORT_DESCRIPTION")
  @NotBlank
  private String shortDescription;

  @Column(name = "LONG_DESCRIPTION")
  @NotBlank
  private String longDescription;
}
