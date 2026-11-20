package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The part of a pipeline's CI connection every project member may see: no credential, repository or job names.
 * <p>
 * Example: {@code {"state": "CONNECTION_FAILED", "canStartJobs": false}} - the UI disables Push to agent, Retry,
 * Re-run and Automate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineCiConnectionSummaryRS {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private CiConnectionState state;
  @Schema(description = "True only when state is CONNECTED", requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean canStartJobs;
}
