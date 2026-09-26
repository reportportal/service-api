package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineStageRS {
  private Long id;
  private String stageKey;
  private String name;
  private String shortName;
  private Integer sequence;
  private String agent;
  private PipelineRunStatus status;
  private Map<String, Object> metrics;
  private String note;
  private List<PipelineGraderRS> graders;
  private List<String> testCaseIds;
  private PipelineStageCiRS ci;
  private Instant lastRetriedAt;
  private Long lastRetriedBy;
}
