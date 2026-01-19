package com.epam.reportportal.core.tms.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Result of parsing import file. Contains parsed test cases and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseImportParseResult {

  @Builder.Default
  private List<TmsTestCaseImportRQ> testCases = new ArrayList<>();

  private int totalRows;

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(testCases);
  }
}
