package com.epam.ta.reportportal.core.tms.dto.batch;

import com.epam.ta.reportportal.core.tms.validation.ValidBatchPatchTestCaseAttributesRQ;
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
@ValidBatchPatchTestCaseAttributesRQ
public class BatchPatchTestCaseAttributesRQ {

  @NotEmpty(message = "Test case IDs cannot be empty")
  private List<Long> testCaseIds;

  private List<Long> attributesToRemove;

  private List<Long> attributeIdsToAdd;
}
