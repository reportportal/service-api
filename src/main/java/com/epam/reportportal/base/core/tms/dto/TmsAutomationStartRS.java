package com.epam.reportportal.base.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The answer to a started automation: one iteration for all accepted cases. {@code accepted} and {@code skipped}
 * are disjoint and together are exactly the submitted ids, each once.
 * <p>
 * Example: {@code {"iteration": {"pipelineId": 17, "iterationId": 452, "number": 8}, "accepted": [42],
 * "skipped": [{"id": 43, "displayId": "TC43", "reason": "AUTOMATION_IN_PROGRESS"}]}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAutomationStartRS {

  private TmsAutomationIterationRefRS iteration;
  private List<Long> accepted;
  private List<TmsAutomationSkippedRS> skipped;
}
