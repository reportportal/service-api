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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineIterationDetailRS {
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
  @Schema(description = "Typed facts about the iteration: META ones form the meta line and totals, TAG ones are chips")
  private List<PipelineAttribute> attributes;
  private Instant startedAt;
  private Instant finishedAt;
  private Long durationMillis;
  private PipelineUserRS startedBy;
  @Schema(description = "The iteration this one is a re-run of")
  private PipelineIterationRefRS rerunOf;
  private Instant createdAt;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private List<PipelineStageRS> stages;
  @Schema(description = "Only set in the response to a report: what was accepted but not fully applied")
  private List<PipelineWarningRS> warnings;
}
