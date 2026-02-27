package com.nxp.iemdm.spring.configuration;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allow all origins in development (you can restrict this to specific origins in production)
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));

    // Allow credentials (cookies, authorization headers, etc.)
    configuration.setAllowCredentials(true);

    // Allow all HTTP methods
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Allow all headers
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // Expose headers that the client can access
    configuration.setExposedHeaders(Arrays.asList("Set-Cookie"));

    // How long the response from a pre-flight request can be cached
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
