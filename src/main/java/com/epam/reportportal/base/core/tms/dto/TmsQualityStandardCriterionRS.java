package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardCriterionRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
  @Schema(description = "Stable machine-readable key; null when not assigned")
  private String key;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer maxPoints;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer sequence;
}
