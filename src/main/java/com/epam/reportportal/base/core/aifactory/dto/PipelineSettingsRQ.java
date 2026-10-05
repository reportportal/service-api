package com.epam.reportportal.base.core.aifactory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pipeline Auto-Ready settings (A.1's {@code autoReadyEnabled}/{@code autoReadyThreshold}).
 * {@code autoReadyThreshold} is required (0-100) when {@code autoReadyEnabled} is {@code true}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineSettingsRQ {
  @NotNull
  private Boolean autoReadyEnabled;
  private Integer autoReadyThreshold;
}
