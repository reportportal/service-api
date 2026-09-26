package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

  @NotBlank
  @Size(max = 256)
  private String pipelineName;

  @Size(max = 64)
  @Schema(description = "Kind of the pipeline (e.g. TEST_CASE_GENERATION); recorded when the pipeline is first reported. "
      + "Reporting a different type for an existing pipeline is rejected")
  private String pipelineType;

  @Valid
  @Schema(description = "Typed facts about the pipeline itself (e.g. its repository); added to or replacing the "
      + "pipeline's attributes by key")
  private List<PipelineAttribute> pipelineAttributes;

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
