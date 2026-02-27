package com.nxp.iemdm.service.rest;

import com.nxp.iemdm.model.RestResponsePage;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.Manufacturer;
import com.nxp.iemdm.model.search.GenericSearchArguments;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.service.GenericSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GenericSearchServiceRest implements GenericSearchService {

  private final RestTemplate restTemplate;
  private final String servicesUri;

  public GenericSearchServiceRest(
      RestTemplate restTemplate, @Value("${rest.iemdm-services.uri}") String servicesUri) {
    this.restTemplate = restTemplate;
    this.servicesUri = servicesUri;
  }

  @Override
  public Page<Person> searchPerson(GenericSearchArguments searchArgs, int page, int size) {
    ParameterizedTypeReference<RestResponsePage<Person>> parameterizedTypeReference =
        new ParameterizedTypeReference<>() {};
    ResponseEntity<RestResponsePage<Person>> responseEntity =
        this.getTypedResponsePage(parameterizedTypeReference, "person", searchArgs, page, size);
    return responseEntity.getBody();
  }

  @Override
  public Page<Location> searchLocation(GenericSearchArguments searchArgs, int page, int size) {
    ParameterizedTypeReference<RestResponsePage<Location>> parameterizedTypeReference =
        new ParameterizedTypeReference<>() {};
    ResponseEntity<RestResponsePage<Location>> responseEntity =
        this.getTypedResponsePage(parameterizedTypeReference, "location", searchArgs, page, size);
    return responseEntity.getBody();
  }

  @Override
  public Page<Manufacturer> searchManufacturer(
      GenericSearchArguments searchArgs, int page, int size) {
    ParameterizedTypeReference<RestResponsePage<Manufacturer>> parameterizedTypeReference =
        new ParameterizedTypeReference<>() {};
    ResponseEntity<RestResponsePage<Manufacturer>> responseEntity =
        this.getTypedResponsePage(
            parameterizedTypeReference, "manufacturer", searchArgs, page, size);
    return responseEntity.getBody();
  }

  /**
   * The method getResponsePage returns data with the raw type RestResponsePage without taking into
   * account the generic type that it should have.<br>
   * What this means is that, instead of a Page with the generic typed data that is desired, you
   * will receive a Page of LinkedHashMap.<br>
   * When used in the frontend, this does not cause any issues (as far as we noticed), because the
   * map structure ends up in the expected TypeScript type instead of in a generic Map. When you
   * want to use the data in the proper generic type in the Java code, the LinkedHashMap had better
   * be avoided.
   *
   * <p>If the desired result is a Page of NC12SitePartWithYield, the parameterized type reference
   * needs to have the correct complete generic type spelled out literally in the code, which could
   * be done as follows: <code>
   * ParameterizedTypeReference<RestResponsePage<NC12SitePartWithYield>> parameterizedTypeReference = new ParameterizedTypeReference<>() {};
   * </code>
   *
   * @param parameterizedTypeReference
   * @param urlPostfix
   * @param searchArgs
   * @param page
   * @param size
   * @return a response entity of the response type expressed as {@code parameterizedTypeReference}
   * @param <T> type of the data for the response of the REST template exchange
   */
  private <T> ResponseEntity<T> getTypedResponsePage(
      ParameterizedTypeReference<T> parameterizedTypeReference,
      String urlPostfix,
      GenericSearchArguments searchArgs,
      int page,
      int size) {

    String url =
        this.servicesUri
            + String.format("/genericSearch/%s?page=%d&size=%d", urlPostfix, page, size);

    return this.restTemplate.exchange(
        url, HttpMethod.POST, new HttpEntity<>(searchArgs), parameterizedTypeReference);
  }
}
