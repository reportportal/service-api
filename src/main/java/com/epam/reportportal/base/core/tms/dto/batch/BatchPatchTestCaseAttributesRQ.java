package com.epam.reportportal.base.core.tms.dto.batch;

import com.epam.reportportal.base.core.tms.validation.ValidBatchPatchTestCaseAttributesRQ;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;
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

  private Set<String> attributeKeysToRemove;

  private Set<String> attributeKeysToAdd;
}
