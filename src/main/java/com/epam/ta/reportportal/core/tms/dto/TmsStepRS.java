package com.epam.ta.reportportal.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmsStepRS {
  private Long id;

  private String instructions;

  private String expectedResult;
}
