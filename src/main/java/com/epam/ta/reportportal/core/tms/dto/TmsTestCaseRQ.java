package com.epam.ta.reportportal.core.tms.dto;

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

  @Valid
  private TmsTestCaseTestFolderRQ testFolder;

  private List<TmsTestCaseAttributeRQ> tags;

  private TmsTestCaseDefaultVersionRQ testCaseDefaultVersion;
}
