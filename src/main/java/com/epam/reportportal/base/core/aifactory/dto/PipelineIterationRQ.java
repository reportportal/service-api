package com.epam.reportportal.base.core.aifactory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ingest payload (A.3), called by CI/the agent after a pipeline run finishes.
 * Resolves-or-creates the {@code Pipeline} definition by {@code pipelineName}.
 * {@code rerun=true} + {@code rerunOfIterationId} upserts onto that iteration
 * instead of creating a new one (idempotent retry keying).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIterationRQ {
  @NotBlank
  private String pipelineName;
  private String trigger;
  private Instant startedAt;
  private Instant finishedAt;
  private boolean rerun;
  private Long rerunOfIterationId;
  private Map<String, Object> metrics;
  @Valid
  @NotEmpty
  private List<PipelineStageRQ> stages;
}
