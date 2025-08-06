package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Response DTO for text-based manual scenario operations.
 * Contains information about instructions and expected result as text.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTextManualScenarioRS extends TmsManualScenarioRS {

  /**
   * Instructions for the test case.
   */
  private String instructions;

  /**
   * Expected result of the test case.
   */
  private String expectedResult;

  /**
   * Attachments for the manual scenario.
   */
  @Valid
  private List<TmsManualScenarioAttachmentRS> attachments;
}
