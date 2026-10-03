package com.epam.reportportal.base.core.aifactory.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compare response (A.5). The UI diffs {@code current.metrics} vs
 * {@code previous.metrics} itself for anything beyond the per-stage status/metric
 * deltas already broken out here — {@code metrics} is a free-form map, so there is
 * no fixed set of keys to compute a generic top-level delta for.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineCompareRS {
  private PipelineIterationDetailRS current;
  private PipelineIterationDetailRS previous;
  private List<PipelineStageDeltaRS> stageDeltas;
}
