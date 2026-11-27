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
   * On create: defaults to {@code READY} for manual test cases; defaults to
   * {@code DRAFT} when the AI agent reports this as an AI-authored case via
   * {@code POST .../generation} (see {@link TmsTestCaseGenerationRQ}), unless explicitly
   * set here. Honored on patch: changes status only when present.
   */
  private TmsTestCaseStatus status;
}
