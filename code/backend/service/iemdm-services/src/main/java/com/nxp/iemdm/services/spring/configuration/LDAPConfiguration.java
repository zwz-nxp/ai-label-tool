package com.nxp.iemdm.services.spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LDAPConfiguration {
  private final String ldapUrl;
  private final String ldapUsername;
  private final String ldapPassword;
  private final String ldapBase;

  public LDAPConfiguration(
      @Value("${security.ldap.url}") String ldapUrl,
      @Value("${security.ldap.username}") String ldapUsername,
      @Value("${security.ldap.password}") String ldapPassword,
      @Value("${security.ldap.base}") String ldapBase) {
    this.ldapUrl = ldapUrl;
    this.ldapUsername = ldapUsername;
    this.ldapPassword = ldapPassword;
    this.ldapBase = ldapBase;
  }

  @Bean
  public LdapContextSource contextSource() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(ldapUrl);
    contextSource.setBase(ldapBase);
    contextSource.setUserDn(ldapUsername);
    contextSource.setPassword(ldapPassword);
    contextSource.setReferral("follow");
    return contextSource;
  }

  @Bean
  public LdapTemplate ldapTemplate() {
    return new LdapTemplate(contextSource());
  }
}
