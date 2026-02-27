package com.nxp.iemdm.operational.service.ldap;

import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class PersonLdapService {

  private static final String NXP_BASE_DN = "OU=NXP";

  private static final String FREE_SCALE_BASE_DN = "OU=CODE";
  private static final String ATTRIBUTE_CN = "CN";
  private final LdapTemplate ldapTemplate;

  public PersonLdapService(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }

  @MethodLog
  public List<Person> searchForPersons(String wbi) {
    List<Person> persons =
        ldapTemplate.search(
            LdapQueryBuilder.query().base(NXP_BASE_DN).where(ATTRIBUTE_CN).like(wbi + "*"),
            this::mapLdapDataToPerson);

    // Legacy freescale
    persons.addAll(
        ldapTemplate.search(
            LdapQueryBuilder.query().base(FREE_SCALE_BASE_DN).where(ATTRIBUTE_CN).like(wbi + "*"),
            this::mapLdapDataToPerson));

    return persons;
  }

  private Person mapLdapDataToPerson(Attributes attributes) throws NamingException {
    Person person = new Person();

    Attribute wbi = attributes.get(ATTRIBUTE_CN);
    Attribute description = attributes.get("description");
    Attribute mail = attributes.get("mail");

    person.setWbi((String) wbi.get());
    person.setName(description != null ? (String) description.get() : "");
    person.setEmail(mail != null ? (String) mail.get() : "");

    return person;
  }
}
