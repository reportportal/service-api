package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A report of one iteration: the root describes the pipeline, {@link #iteration} describes the run. The
 * pipeline is created by the first report that names it.
 * <p>
 * Example: {@code {"name": "Test case generation", "type": "TEST_CASE_GENERATION",
 * "attributes": [{"key": "repository", "value": "EPM-RPP/rp-tests", "type": "META"}],
 * "iteration": {"startedAt": "2026-09-19T10:13:00Z",
 * "attributes": [{"key": "requirement", "value": "US-TMS-MIG-001", "type": "META"}],
 * "stages": [{"stageKey": "create", "status": "Running"}]}}}. The root {@code attributes} describe the pipeline
 * (its repository, shown once in the group header); {@code iteration.attributes} describe this run
 * (requirement, model, environment).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIterationRQ {

  @NotBlank
  @Size(max = 256)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Pipeline name, unique within the project; "
      + "the pipeline is created by the first report that names it")
  private String name;

  @Size(max = 64)
  @Schema(description = "Kind of the pipeline (e.g. TEST_CASE_GENERATION); recorded when the pipeline is first "
      + "reported and selects the behaviour plugged into that kind. Reporting a different type for an "
      + "existing pipeline is rejected")
  private String type;

  @Valid
  @Schema(description = "Typed facts about the pipeline itself (e.g. its repository); added to or replacing the "
      + "pipeline's attributes by key")
  private List<PipelineAttribute> attributes;

  @NotNull
  @Valid
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The iteration being reported")
  private IterationRQ iteration;
}
