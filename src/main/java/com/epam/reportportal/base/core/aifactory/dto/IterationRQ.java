package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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

  @Size(max = 512)
  @Schema(description = "One line outcome of the run, shown on its card. Example: \"Passed · $17.82 · 15.4M tokens\"")
  private String summary;

  @Size(max = 4096)
  @Schema(description = "What the run is for. Example: \"Implement test case · EPMRPP-105554: Invite user modal\"")
  private String description;

  @Size(max = 4096)
  @Schema(description = "Information about the run as a whole that applies to all its stages, as text. "
      + "Example: \"orchestrator $1.81 + 6 stages $16.01\"")
  private String details;

  @Valid
  @Schema(description = "Typed facts about this iteration: requirement, trigger, model, environment, links, tags, ...")
  private List<PipelineAttribute> attributes;

  private Instant startedAt;

  private Instant finishedAt;

  @Schema(description = "Marks the iteration as a re-run of the given iteration; a new iteration is created either "
      + "way. The pipeline (name and type) is taken from the given iteration, and its attributes marked "
      + "comparisonKey (e.g. the requirement) are inherited unless this report sends one with the same key. "
      + "Example: {\"iteration\": {\"rerunOfIterationId\": 41, \"attributes\": [{\"key\": \"fixRoundId\", "
      + "\"value\": \"7\"}]}} needs no name, type or requirement")
  private Long rerunOfIterationId;

  @Valid
  @Schema(description = "Stages known at creation time; may be empty - stages can be reported one by one afterwards")
  private List<PipelineStageRQ> stages;
}
