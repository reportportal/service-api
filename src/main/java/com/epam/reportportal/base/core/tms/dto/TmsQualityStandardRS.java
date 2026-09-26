package com.epam.reportportal.base.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardRS {
  private Long id;
  private String name;
  private String description;
  private List<TmsQualityStandardCriterionRS> criteria;
  private Long createdAt;
  private Long updatedAt;
}
