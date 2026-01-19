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
public class TmsTestCaseImportRQ {

  private String name;

  private String description;

  private String priority;

  private String externalId;

  /**
   * Folder path hierarchy from root. Example: ["folder1", "folder2", "folder3"] Will be resolved
   * relative to base folder specified in API.
   */
  private List<String> folderPath;

  /**
   * Attributes to be resolved by key. If attribute with key doesn't exist, it will be created.
   */
  private List<TmsTestCaseAttributeImportRQ> attributes;

  @Valid
  private TmsManualScenarioRQ manualScenario;
}
