package com.nxp.iemdm.spring.configuration;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.nxp.iemdm.spring.security.authentication.CustomSaml2AuthenticationFailureHandler;
import com.nxp.iemdm.spring.security.cache.CustomRequestCache;
import jakarta.servlet.DispatcherType;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@CrossOrigin
public class SAML2SecurityConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SAML2SecurityConfiguration.class);
  private static final AntPathRequestMatcher[] AUTHENTICATION_WHITELIST = {
    antMatcher("/error/**"),
    antMatcher("/login/saml2/SSO/adfs/**"),
    antMatcher("/monitoring/**"),
    antMatcher("/images/**"),
    antMatcher("/assets/**"),
    antMatcher("/*.js"),
    antMatcher("/*.css"),
    antMatcher("/*.ico"),
  };
  private final UserDetailsService userDetailsService;
  private final String environment;
  private static final String SCREEN_NAME_ATTRIBUTE_NAME =
      "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/screenName";

  public SAML2SecurityConfiguration(
      UserDetailsService userDetailsService, @Value("${security.environment}") String environment) {
    this.userDetailsService = userDetailsService;
    this.environment = environment;
  }

  /**
   * Defines the web based security configuration.
   *
   * @param httpSecurity It allows configuring web based security for specific http requests.
   */
  @Bean
  protected SecurityFilterChain configure(
      HttpSecurity httpSecurity,
      AuthenticationProvider authenticationProvider,
      CorsConfigurationSource corsConfigurationSource) {
    try {
      if (this.environment.equals(LOCAL_DEVELOPMENT_ENVIRONMENT)) {
        // Enable CORS with credentials for DEV/QA to support session cookies
        httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource));
      } else {
        // Production: keep CORS disabled as before
        httpSecurity.csrf(AbstractHttpConfigurer::disable).cors(AbstractHttpConfigurer::disable);
      }
    } catch (Exception exception) {
      log.error("Could not configure csrf.", exception);
    }

    if (this.environment.equals(LOCAL_DEVELOPMENT_ENVIRONMENT)) {
      try {
        httpSecurity
            .addFilterBefore(
                new AuthenticateIemdmUserOnDevAuthenticationFilter(),
                BasicAuthenticationFilter.class)
            .authorizeHttpRequests(
                oauthorizationManagerRequestMatcherRegistry ->
                    oauthorizationManagerRequestMatcherRegistry.anyRequest().permitAll());

      } catch (Exception exception) {
        log.error("Could not configure httpSecurity for LOCAL_DEVELOPMENT_ENVIRONMENT", exception);
      }
    } else {
      try {
        httpSecurity
            .authorizeHttpRequests(
                authorize ->
                    authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD)
                        .permitAll()
                        .requestMatchers(AUTHENTICATION_WHITELIST)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
            .saml2Login(
                saml2 ->
                    saml2
                        .loginProcessingUrl("/login/saml2/SSO/adfs")
                        .defaultSuccessUrl("/", false)
                        .authenticationManager(new ProviderManager(authenticationProvider))
                        .failureHandler(new CustomSaml2AuthenticationFailureHandler()))
            .requestCache(cacheConfigurer -> cacheConfigurer.requestCache(customRequestCache()));
      } catch (Exception exception) {
        log.error("Could not configure httpSecurity for SAML2", exception);
      }
    }
    try {
      return httpSecurity.build();
    } catch (Exception exception) {
      log.error("Could not build httpSecurity and return it.", exception);
      return null;
    }
  }

  @Bean
  public RequestCache customRequestCache() {
    return new CustomRequestCache();
  }

  @Bean
  AuthenticationProvider authenticationProvider() {
    OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();

    provider.setResponseAuthenticationConverter(groupsConverter());

    return provider;
  }

  private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication>
      groupsConverter() {
    Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
        OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();
    return (responseToken) -> {
      Saml2Authentication authentication = delegate.convert(responseToken);

      Saml2AuthenticatedPrincipal principal;
      try {
        assert authentication != null;
        principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
      } catch (NullPointerException nullPointerException) {
        log.info(
            "Could not get saml principal of the authentication object.", nullPointerException);
        throw nullPointerException;
      }

      List<String> screenNameAttr = principal.getAttribute(SCREEN_NAME_ATTRIBUTE_NAME);
      if (screenNameAttr == null || screenNameAttr.isEmpty()) {
        var errorMessage = "The ScreenName attribute is null or empty.";
        log.info(errorMessage); // Contact azure team if this occurs!
        throw new IllegalStateException(errorMessage);
      }

      String wbi = screenNameAttr.get(0);
      UserDetails iemdmPrincipal = this.userDetailsService.loadUserByUsername(wbi);

      // UserDetails implements UserDetails interface as well as AuthenticatedPrincipal
      // We keep track of roles in a different way, therefore we pass an empty list, because we keep
      // them in the userDetails (iemdmPrincipal).
      return new Saml2Authentication(
          ((AuthenticatedPrincipal) iemdmPrincipal), authentication.getSaml2Response(), List.of());
    };
  }
}
