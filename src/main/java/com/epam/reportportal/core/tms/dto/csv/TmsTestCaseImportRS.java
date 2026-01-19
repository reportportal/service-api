package com.epam.reportportal.core.tms.dto.csv;

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response for test case import operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTestCaseImportRS {

  private List<TmsTestCaseRS> importedTestCases;
  private int totalRows;
  private int successCount;
  private int failedCount;
  private int warningCount;
  private List<TmsTestCaseImportRowError> errors;
  private List<TmsTestCaseImportRowWarning> warnings;

  public static TmsTestCaseImportRS from(TmsTestCaseImportResult result) {
    return TmsTestCaseImportRS.builder()
        .importedTestCases(result.getImportedTestCases())
        .totalRows(result.getTotalRows())
        .successCount(result.getSuccessCount())
        .failedCount(result.getFailedCount())
        .warningCount(result.getWarningCount())
        .errors(result.getErrors().isEmpty() ? null : result.getErrors())
        .warnings(result.getWarnings().isEmpty() ? null : result.getWarnings())
        .build();
  }
}
