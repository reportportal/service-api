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
 * One stage of an ingested iteration (A.3). {@code stageKey} is free-form — a
 * stage may be "any activity", not a fixed enum of stage kinds. May nest other
 * stages via {@code parentStageKey}, which references another stage's
 * {@code stageKey} within this same payload (child stage ids don't exist yet
 * at ingest time, so a same-batch parent needs a payload-local reference
 * rather than a real id).
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
  private String parentStageKey;
  @NotNull
  private PipelineRunStatus status;
  private Map<String, Object> metrics;
  private Map<String, String> attributes;
  private PipelineStepResultRQ result;
  private List<String> testCaseIds;
  @Valid
  private PipelineStageCiRQ ci;
}
