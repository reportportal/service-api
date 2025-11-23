package com.epam.reportportal.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Test Plan Owner")
public class TmsManualLaunchOwnerRS {

  @Schema(description = "Owner ID", example = "123")
  private Long id;

  @Schema(description = "Test plan owner name", example = "test plan")
  private String email;
}
