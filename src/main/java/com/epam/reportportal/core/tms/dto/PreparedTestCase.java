package com.epam.reportportal.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparedTestCase {

  private TmsTestCaseImportRQ testCase;

  private Long folderId;

  private int rowNumber;

  /**
   * Pre-computed path key for folder lookup. Avoids recomputing String.join().
   */
  private String pathKey;
}
