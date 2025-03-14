package com.epam.ta.reportportal.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmsDatasetDataRS {

  private Long id;
  private String key;
  private String value;
}
