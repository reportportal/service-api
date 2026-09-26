package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardCriterionRQ {

  @NotBlank
  private String name;

  @NotNull
  @Min(1)
  private Integer maxPoints;

  @NotNull
  private Integer sequence;
}
