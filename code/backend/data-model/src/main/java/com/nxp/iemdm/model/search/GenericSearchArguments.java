package com.nxp.iemdm.model.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POJO used in GenericSearch */
@AllArgsConstructor
@NoArgsConstructor
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericSearchArguments implements Serializable {

  @Serial private static final long serialVersionUID = 4748256056400354781L;

  private List<GenericSearchArg> searchArgs = new ArrayList<>();
  private List<GenericSortArg> sortArgs = new ArrayList<>();
}
