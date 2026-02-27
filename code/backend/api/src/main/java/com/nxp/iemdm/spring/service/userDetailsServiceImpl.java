package com.nxp.iemdm.spring.service;

import com.nxp.iemdm.exception.InternalServerException;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.service.PersonService;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class userDetailsServiceImpl implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(userDetailsServiceImpl.class);

  private final PersonService personService;

  public userDetailsServiceImpl(PersonService personService) {
    this.personService = personService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    List<GrantedAuthority> authorities = new ArrayList<>();

    try {
      Person person = personService.getPersonByWBI(username, true);

      if (Boolean.TRUE.equals(person.getLoginAllowed())) {
        personService.processLoginForPerson(person);

        log.info("Returning new iemdm principal");
        return new IEMDMPrincipal(
            new User(person.getWbi(), "<abc123>", authorities), person.getRoles());
      } else {
        throw new UsernameNotFoundException("Login is currently not allowed.");
      }
    } catch (NotFoundException e) {
      log.info(
          "Login attempt received for wbi: {}, user is not authorized for IE-MDM usage", username);
      throw new UsernameNotFoundException(
          "User with wbi: " + username + " does not exist in the IEMDM user store");
    } catch (InternalServerException e) {
      log.info("Error occurred during user lookup", e);
      throw e;
    }
  }
}
