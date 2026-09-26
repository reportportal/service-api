package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One stage of an ingested iteration (A.3). {@code stageKey}/{@code agent} are
 * free-form — a stage may be "any activity", not a fixed enum of stage kinds.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageRQ {
  @NotNull
  private String stageKey;
  private String name;
  private String shortName;
  @NotNull
  private Integer sequence;
  private String agent;
  @NotNull
  private PipelineRunStatus status;
  private Map<String, Object> metrics;
  @Valid
  private List<PipelineGraderRQ> graders;
  private List<String> testCaseIds;
  @Valid
  private PipelineStageCiRQ ci;
}
