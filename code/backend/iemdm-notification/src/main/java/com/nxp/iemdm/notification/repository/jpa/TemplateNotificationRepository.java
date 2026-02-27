package com.nxp.iemdm.notification.repository.jpa;

import com.nxp.iemdm.model.configuration.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateNotificationRepository extends CrudRepository<Template, String> {}
