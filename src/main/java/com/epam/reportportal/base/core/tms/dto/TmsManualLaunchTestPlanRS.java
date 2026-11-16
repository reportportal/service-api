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
@Schema(description = "Test Plan information")
public class TmsManualLaunchTestPlanRS {

  @Schema(description = "Test plan ID", example = "123")
  private Long id;

  @Schema(description = "Test plan name", example = "test plan")
  private String name;
}
