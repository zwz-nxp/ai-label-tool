package com.nxp.iemdm.model.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** POJO used in GenericSearch */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericSearchArg extends GenericBaseArg {

  @Serial private static final long serialVersionUID = -3071713812931864970L;

  private String value;

  public GenericSearchArg(GenericSearchSortField field, String value) {
    this.value = value;
    this.setField(field);
  }

  public void setValue(String value) {
    this.value = value;
  }
}
