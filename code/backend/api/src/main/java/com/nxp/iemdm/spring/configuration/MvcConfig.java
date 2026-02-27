package com.nxp.iemdm.spring.configuration;

import static com.nxp.iemdm.spring.constant.ApiConstants.LOCAL_DEVELOPMENT_ENVIRONMENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.spring.resolver.CurrentUserHandlerMethodArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  private final CurrentUserHandlerMethodArgumentResolver currentUserHandlerMethodArgumentResolver;
  private final String environment;
  private final int cachePeriod;
  private final ObjectMapper objectMapper;

  @Autowired
  public MvcConfig(
      CurrentUserHandlerMethodArgumentResolver currentUserHandlerMethodArgumentResolver,
      @Value("${security.environment}") String environment,
      @Value("${spring.cache.period:31536000}") int cachePeriod,
      ObjectMapper objectMapper) {
    this.currentUserHandlerMethodArgumentResolver = currentUserHandlerMethodArgumentResolver;
    this.environment = environment;
    this.cachePeriod = cachePeriod;
    this.objectMapper = objectMapper;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(currentUserHandlerMethodArgumentResolver);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new LandingAiAuthorizationInterceptor(environment, objectMapper))
        .addPathPatterns("/api/landingai/**");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/*.js", "/*.css", "/*.woff2", "/*.woff", "/*.ttf", "/*.eot", "/*.gz")
        .addResourceLocations("/public", "classpath:/static/")
        .setCachePeriod(cachePeriod);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    if (LOCAL_DEVELOPMENT_ENVIRONMENT.equals(environment)) {
      registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE");
    }
  }
}
