package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.core.tms.dto.batch.BatchFolderOperationResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DuplicateTmsTestFolderRS {

  private Long id;
  private String name;
  private String description;
  private Long countOfTestCases;
  private Long parentFolderId;
  private Integer index;
  private BatchTestCaseOperationResultRS testCaseDuplicationStatistic;
  private BatchFolderOperationResultRS folderDuplicationStatistic;
}
