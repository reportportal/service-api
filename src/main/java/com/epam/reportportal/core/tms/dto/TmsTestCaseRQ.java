package com.epam.reportportal.core.tms.dto;

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
}
