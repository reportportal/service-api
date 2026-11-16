package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response for test case import operation. Returns only IDs of created test cases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTestCaseImportRS {

  @Builder.Default
  private List<Long> createdTestCaseIds = new ArrayList<>();

  private Long testFolderId;

  private int totalRows;

  private int successCount;

  public static TmsTestCaseImportRS of(List<Long> ids, Long testFolderId, int totalRows) {
    return TmsTestCaseImportRS.builder()
        .createdTestCaseIds(ids)
        .testFolderId(testFolderId)
        .totalRows(totalRows)
        .successCount(ids.size())
        .build();
  }
}
