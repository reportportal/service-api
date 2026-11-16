package com.epam.reportportal.base.core.tms.dto.batch;

import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.validation.ValidBatchPatchTestCasesRQ;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchPatchTestCasesRS {

  @NotNull
  @NotEmpty
  private List<Long> testCaseIds;

  private Long testFolderId;

  private String priority;
}
