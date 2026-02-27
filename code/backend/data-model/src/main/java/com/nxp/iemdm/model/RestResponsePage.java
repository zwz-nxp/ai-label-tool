package com.nxp.iemdm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class RestResponsePage<T> extends PageImpl<T> {
  @Serial private static final long serialVersionUID = -5090952556027717365L;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public RestResponsePage(
      @JsonProperty("content") List<T> content,
      @JsonProperty("number") int number,
      @JsonProperty("size") int size,
      @JsonProperty("totalElements") Long totalElements,
      @JsonProperty("pageable") JsonNode pageable,
      @JsonProperty("last") boolean last,
      @JsonProperty("totalPages") int totalPages,
      @JsonProperty("sort") JsonNode sort,
      @JsonProperty("first") boolean first,
      @JsonProperty("numberOfElements") int numberOfElements) {

    super(content, PageRequest.of(number, size), totalElements);
  }

  public RestResponsePage(List<T> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }

  public RestResponsePage(List<T> content) {
    super(content);
  }

  public RestResponsePage() {
    super(new ArrayList<>());
  }
}
