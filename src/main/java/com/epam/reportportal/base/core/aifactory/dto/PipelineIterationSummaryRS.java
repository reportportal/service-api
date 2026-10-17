package com.epam.reportportal.base.core.aifactory.dto;

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
 * An iteration as shown on a list card: header facts as attributes (META in the meta line, TAG as chips),
 * stage chips and timing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineIterationSummaryRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long pipelineId;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String pipelineName;
  private String pipelineType;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer iterationNumber;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Derived status text of the iteration")
  private String status;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StatusCategory statusCategory;
  private List<PipelineAttribute> attributes;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<PipelineStageSummaryRS> stages;
  private Instant startedAt;
  private Instant finishedAt;
  private Long durationMillis;
  private PipelineUserRS startedBy;
  @Schema(description = "The iteration this one is a re-run of")
  private PipelineIterationRefRS rerunOf;
  private Instant createdAt;
}
