package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for manual scenario step operations.
 * Contains information about a step in a step-based manual scenario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsManualScenarioStepRQ {

  /**
   * Instructions for the step.
   */
  private String instructions;

  /**
   * Expected result of the step.
   */
  private String expectedResult;

  /**
   * Attachments for the step.
   */
  @Valid
  private List<TmsManualScenarioAttachmentRQ> attachments;
}
