package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class PipelineIterationRQ {

  @NotNull
  @Valid
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The pipeline this iteration belongs to")
  private PipelineDefinitionRQ pipeline;

  @Valid
  @Schema(description = "Typed facts about the iteration: requirement, trigger, model, environment, links, tags, ...")
  private List<PipelineAttribute> attributes;

  private Instant startedAt;

  private Instant finishedAt;

  @Schema(description = "Marks the iteration as a re-run of the given iteration of the same pipeline; "
      + "a new iteration is created either way")
  private Long rerunOfIterationId;

  @Valid
  @Schema(description = "Stages known at creation time; may be empty - stages can be reported one by one afterwards")
  private List<PipelineStageRQ> stages;
}
