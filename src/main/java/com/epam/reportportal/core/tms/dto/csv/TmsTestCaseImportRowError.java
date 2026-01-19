package com.epam.reportportal.core.tms.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an error that occurred during import of a specific CSV row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseImportRowError {

  private int rowNumber;
  private String summary;
  private String errorMessage;

  public static TmsTestCaseImportRowError of(int rowNumber, String summary, String errorMessage) {
    return TmsTestCaseImportRowError.builder()
        .rowNumber(rowNumber)
        .summary(summary)
        .errorMessage(errorMessage)
        .build();
  }
}
