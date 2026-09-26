package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.StageOrigin;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A stage as shown in a chip row: status plus its attributes (the MAIN one is the chip metric).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineStageSummaryRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer sequence;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "1-based number of the stage's current attempt; bumped by a retry or a new report")
  private int attempt;
  private Long parentStageId;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Status text exactly as reported")
  private String status;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StatusCategory statusCategory;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StageOrigin origin;
  private Instant startedAt;
  private Instant finishedAt;
  private Long durationMillis;
  private List<PipelineAttribute> attributes;
  @Schema(description = "What the stage is doing or waiting for right now")
  private String message;
}
