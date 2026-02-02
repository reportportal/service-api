package com.epam.reportportal.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseExecutionTestFolderRS {

  private Long id;

  private Long testItemId;

}
