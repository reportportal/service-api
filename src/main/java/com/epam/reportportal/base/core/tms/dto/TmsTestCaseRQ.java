package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class TmsTestCaseRQ {

  private String name;

  private String description;

  private String priority;

  private String externalId;

  private Long testFolderId;

  private NewTestFolderRQ testFolder;

  private List<TmsTestCaseAttributeRQ> attributes;

  @Valid
  private TmsManualScenarioRQ manualScenario;

  /**
   * Ignored on create (always forced to {@code READY}). Honored on patch:
   * changes status only when present.
   */
  private TmsTestCaseStatus status;

  /**
   * Per-criterion scores against the project's Quality Standard, submitted by
   * the AI agent for the version being created/patched.
   */
  @Valid
  private List<TmsTestCaseQualityScoreRQ> qualityScores;

  /**
   * How this version was generated, submitted by the AI agent.
   */
  @Valid
  private TmsTestCaseGenerationMetadataRQ generation;
}
