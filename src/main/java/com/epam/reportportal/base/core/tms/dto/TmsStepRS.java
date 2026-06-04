package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmsStepRS {
  private Long id;

  private String instructions;

  private String expectedResult;

  @Valid
  private List<TmsManualScenarioAttachmentRS> attachments;
}
