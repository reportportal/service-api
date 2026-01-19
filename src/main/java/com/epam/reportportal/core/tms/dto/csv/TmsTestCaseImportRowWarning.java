package com.epam.reportportal.core.tms.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a warning that occurred during import of a specific CSV row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseImportRowWarning {

  private int rowNumber;
  private String summary;
  private String field;
  private String warningMessage;

  public static TmsTestCaseImportRowWarning of(int rowNumber, String summary, String field,
      String warningMessage) {
    return TmsTestCaseImportRowWarning.builder()
        .rowNumber(rowNumber)
        .summary(summary)
        .field(field)
        .warningMessage(warningMessage)
        .build();
  }
}
