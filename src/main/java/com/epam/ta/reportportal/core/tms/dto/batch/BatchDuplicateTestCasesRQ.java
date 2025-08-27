package com.epam.ta.reportportal.core.tms.dto.batch;

import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.validation.ValidTestFolderIdentifier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
@ValidTestFolderIdentifier
public class BatchDuplicateTestCasesRQ {

  @NotEmpty(message = "Test case IDs list cannot be empty")
  private List<Long> testCaseIds;

  private Long testFolderId;

  private NewTestFolderRQ testFolder;
}
