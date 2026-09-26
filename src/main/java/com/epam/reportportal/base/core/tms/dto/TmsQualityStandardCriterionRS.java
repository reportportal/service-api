package com.epam.reportportal.base.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardCriterionRS {
  private Long id;
  private String name;
  private Integer maxPoints;
  private Integer sequence;
}
