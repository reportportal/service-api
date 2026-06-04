package com.epam.reportportal.base.core.tms.dto.batch;

import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.validation.ValidTestFolderIdForBatchDuplicateTestCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
@ValidTestFolderIdForBatchDuplicateTestCase
public class BatchDuplicateTestCasesRQ {

  @NotEmpty(message = "Test case IDs list cannot be empty")
  private List<Long> testCaseIds;

  private Long testFolderId;

  private NewTestFolderRQ testFolder;
}
