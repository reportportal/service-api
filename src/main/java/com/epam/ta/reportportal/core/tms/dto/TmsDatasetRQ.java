package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmsDatasetRQ {

  private String name;
  private List<TmsDatasetDataRQ> attributes;
}
