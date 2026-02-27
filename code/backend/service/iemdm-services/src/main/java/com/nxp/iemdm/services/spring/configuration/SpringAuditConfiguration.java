package com.nxp.iemdm.services.spring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class,
    basePackages = {
      "com.nxp.iemdm.operational.repository.audit",
      // "com.nxp.iemdm.planning.repository.audit",
      // "com.nxp.iemdm.approval.repository.audit",
      // "com.nxp.iemdm.consparams.repository.audit",
      // "com.nxp.iemdm.approval.repository.audit",
      // "com.nxp.iemdm.shared.repository.audit"
    })
public class SpringAuditConfiguration {}
