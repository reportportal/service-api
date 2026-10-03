package com.epam.reportportal.base.core.aifactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepResultRS {
  private String resultType;
  private String resultRef;
}
