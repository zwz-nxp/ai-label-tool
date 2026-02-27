package com.nxp.iemdm.model.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nxp.iemdm.adapter.InstantXmlAdapter;
import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.user.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GLOBAL_LOCATION")
@Audited
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "site")
public class Location implements Serializable, Comparable<Location> {
  @Serial private static final long serialVersionUID = -3388432137204085941L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ID")
  @XmlElement(required = true)
  private Integer id;

  @Column(name = "ACRONYM", unique = true)
  @NotBlank
  @XmlElement(required = true)
  private String acronym;

  @Column(name = "CITY")
  private String city;

  @Column(name = "COUNTRY")
  private String country;

  @Column(name = "SAP")
  private String sapCode;

  @Column(name = "TMDB")
  private String tmdbCode;

  @Column(name = "PLANNING_ENGINE")
  private String planningEngine;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private LocationStatus status;

  @Column(name = "LAST_UPDATE")
  @XmlJavaTypeAdapter(InstantXmlAdapter.class)
  @XmlSchemaType(name = "dateTime")
  private Instant lastUpdated;

  @Column(name = "UPDATED_BY")
  private String updatedBy;

  @ManyToOne
  @JoinColumn(
      name = "UPDATED_BY",
      referencedColumnName = "WBI",
      insertable = false,
      updatable = false)
  @XmlTransient
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @JsonIgnore
  private Person updater;

  @Column(name = "EXTENDED_SUFFIX")
  private String extendedSuffix;

  @Column(name = "MENU_GROUPING")
  @XmlTransient
  private String menuGrouping;

  @Column(name = "IS_SUBCONTRACTOR")
  @XmlTransient
  private boolean isSubContractor;

  @Column(name = "VENDOR_CODE")
  @XmlTransient
  private String vendorCode;

  // @LazyCollection should not be necessary anymore with new hibernate version (included in Spring
  // upgrade)
  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
  @JsonManagedReference
  @XmlTransient
  private List<Manufacturer> manufacturers = new ArrayList<>();

  @Transient private int equipmentCount; // will be set in the locationService.
  @Transient @XmlTransient private int activeEventsCount;
  @Transient @XmlTransient private int activeEquipmentCount;

  public Integer getId() {
    return this.id;
  }

  public String getAcronym() {
    return this.acronym;
  }

  //  this method needs to exist because of the GenericSearchLogic
  public boolean getIsSubContractor() {
    return this.isSubContractor;
  }

  @JsonIgnore
  public boolean isGlobalLocation() {
    return this.id == 0;
  }

  @JsonIgnore
  public boolean isNotGlobalLocation() {
    return this.id != 0;
  }

  public String getVendorCodeFormatted() {
    return StringUtils.leftPad(vendorCode, 10, "0");
  }

  /**
   * In reporting either the plant code is used or the vendor code. With NXP owned locations the
   * plant code is used. For subcontractor locations, the vendor code is used.
   *
   * @return vendor code if location is a subcontractor, else plant code
   */
  @XmlTransient
  public String getLocationCode() {
    return this.isSubContractor ? this.vendorCode : this.sapCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location location = (Location) o;
    return acronym.equals(location.acronym);
  }

  @Override
  public int hashCode() {
    return Objects.hash(acronym);
  }

  @Override
  public int compareTo(Location other) {
    return this.acronym.compareTo(other.acronym);
  }
}
