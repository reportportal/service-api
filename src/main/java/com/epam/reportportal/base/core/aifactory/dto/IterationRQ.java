package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The run being reported: the iteration of the pipeline named next to it in {@link PipelineIterationRQ}.
 * <p>
 * Example: {@code {"startedAt": "2026-09-19T10:13:00Z",
 * "attributes": [{"key": "requirement", "value": "US-TMS-MIG-001", "type": "META"}],
 * "stages": [{"stageKey": "create", "status": "Running"}]}}. Everything here belongs to this one iteration;
 * facts about the pipeline itself (its repository) are sent beside it, not inside it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IterationRQ {

  @Valid
  @Schema(description = "Typed facts about this iteration: requirement, trigger, model, environment, links, tags, ...")
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
