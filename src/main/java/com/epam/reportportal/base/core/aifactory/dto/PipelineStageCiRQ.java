package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageCiRQ {
  @NotNull
  private CiProvider provider;
  private String repo;
  private String workflowRef;
  private String runId;
  private String jobId;
  private String runUrl;
  private boolean retryable;
}
