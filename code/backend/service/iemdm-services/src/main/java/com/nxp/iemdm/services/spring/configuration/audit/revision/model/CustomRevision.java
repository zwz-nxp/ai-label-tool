package com.nxp.iemdm.services.spring.configuration.audit.revision.model;

import com.nxp.iemdm.services.spring.configuration.audit.revision.listener.CustomRevisionListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Getter
@Setter
@Entity
@RevisionEntity(CustomRevisionListener.class)
@Table(name = "REVINFO")
public class CustomRevision {

  @Id
  @GeneratedValue
  @RevisionNumber
  @Column(name = "REV")
  private long id;

  @RevisionTimestamp
  @Column(name = "REVTSTMP")
  private long timestamp;

  @Column(name = "UPDATED_BY")
  private String updatedBy;
}
