package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageDeltaRS {
  private String stageKey;
  private Entry current;
  private Entry previous;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Entry {
    private PipelineRunStatus status;
    private Map<String, Object> metrics;
  }
}
