package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmsDatasetDataRQ {

  @Size(max = 512)
  private String key;
  
  @Size(max = 512)
  private String value;
}