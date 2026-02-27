package com.nxp.iemdm.services.spring.configuration.audit.revision.listener;

import com.nxp.iemdm.services.spring.configuration.audit.revision.model.CustomRevision;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomRevisionListener implements RevisionListener {

  @Override
  public void newRevision(Object revisionEntity) {
    CustomRevision customRevision = (CustomRevision) revisionEntity;

    customRevision.setUpdatedBy(getWbiForAuthenticatedUser());
  }

  private String getWbiForAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof UsernamePasswordAuthenticationToken
        && authentication.getPrincipal() instanceof String) {
      return (String) authentication.getPrincipal();
    }
    return null;
  }
}
