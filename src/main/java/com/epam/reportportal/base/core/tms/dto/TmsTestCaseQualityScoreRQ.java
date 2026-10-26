package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

  @Schema(description = "Id of the criterion; exactly one of criterionId and criterionKey is required")
  private Long criterionId;

  @Schema(description = "Stable key of the criterion; exactly one of criterionId and criterionKey is required")
  private String criterionKey;

  @NotNull
  @Min(0)
  private Integer score;
}
