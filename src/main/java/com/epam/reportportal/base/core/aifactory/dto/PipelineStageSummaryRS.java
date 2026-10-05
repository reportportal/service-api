package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight stage row for the iteration summary (list view / {@code latestIteration}):
 * deliberately lighter than {@link PipelineStageRS} — no {@code attributes}, {@code result},
 * {@code testCaseIds}, {@code ci}, {@code lastRetriedAt}/{@code lastRetriedBy}, which are only
 * needed once an iteration is opened (detail view). Carries {@code parentStageId} so the list
 * view can still indicate nesting exists without pulling full detail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineStageSummaryRS {
  private Long id;
  private String stageKey;
  private String name;
  private String shortName;
  private Integer sequence;
  private Long parentStageId;
  private PipelineRunStatus status;
  private Map<String, Object> metrics;
}
