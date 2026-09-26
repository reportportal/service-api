package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.PipelineGraderType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineGraderRQ {
  @NotNull
  private String name;
  @NotNull
  private PipelineGraderType type;
  private String result;
  private boolean pass;
}
