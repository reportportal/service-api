package com.epam.ta.reportportal.core.tms.db.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TmsTestFolderWithSubfolderCounts {
  private TmsTestFolderWithCountOfTestCases folderWithCount;
  private Map<Long, Long> subFolderTestCaseCounts;

  public Long getSubFolderTestCaseCount(Long folderId) {
    return subFolderTestCaseCounts.getOrDefault(folderId, 0L);
  }
}
