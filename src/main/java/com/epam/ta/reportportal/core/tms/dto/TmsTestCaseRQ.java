package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseRQ {

  private String name;

  private String description;

  private String priority;

  private Long testFolderId;

  private List<TmsTestCaseAttributeRQ> tags;

  private TmsTestCaseVersionRQ testCaseVersion;
}
