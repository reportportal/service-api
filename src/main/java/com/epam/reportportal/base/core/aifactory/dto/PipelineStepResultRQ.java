package com.epam.reportportal.base.core.aifactory.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepResultRQ {
  @Size(max = 64)
  private String resultType;
  @Size(max = 255)
  private String resultRef;
}
