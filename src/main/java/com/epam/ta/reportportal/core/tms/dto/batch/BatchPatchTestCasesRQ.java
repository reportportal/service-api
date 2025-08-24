package com.epam.ta.reportportal.core.tms.dto.batch;

import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.validation.ValidBatchPatchTestCasesRQ;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for batch patch operations on test cases.
 * Contains information about what fields should be updated for the selected test cases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidBatchPatchTestCasesRQ
public class BatchPatchTestCasesRQ {

  @NotNull
  @NotEmpty
  private List<Long> testCaseIds;

  private Long testFolderId;

  private String priority;

  @Valid
  private List<TmsAttributeRQ> tags;
}
