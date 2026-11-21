package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One attempt of a stage as the attempt switcher of the Open trace page lists it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineTraceAttemptRS {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private int attempt;
  @Schema(description = "Status of the stage in this attempt, as reported")
  private String stageStatus;
  private StatusCategory stageStatusCategory;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "NOT_REPORTED also when no trace request "
      + "arrived for the attempt at all")
  private TraceStatus traceStatus;
  private long eventCount;
  private Instant reportedAt;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Whether this is the stage's current attempt, "
      + "the default of the switcher")
  private boolean current;
}
