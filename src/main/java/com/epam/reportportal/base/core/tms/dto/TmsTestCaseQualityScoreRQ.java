package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseQualityScoreRQ {

  @NotNull
  private Long criterionId;

  @NotNull
  @Min(0)
  private Integer score;
}
