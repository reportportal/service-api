package com.epam.ta.reportportal.core.tms.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestFolderIdWithCountOfTestCases {

  private Long testFolderId;
  private Long countOfTestCases;
}
