package com.epam.ta.reportportal.core.tms.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmsTestFolderWithCountOfSubfolders {

  private TmsTestFolder testFolder;
  private Long countOfSubfolders;
}
