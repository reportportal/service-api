package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
  private String description;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<TmsQualityStandardCriterionRS> criteria;
  @Schema(description = "Creation time, epoch milliseconds", example = "1767225600000")
  private Long createdAt;
  @Schema(description = "Last update time, epoch milliseconds", example = "1767225600000")
  private Long updatedAt;
}
