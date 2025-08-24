package com.epam.ta.reportportal.core.tms.dto.batch;

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
public class BatchRemoveTestCasesFromPlanRQ {

  @NotEmpty(message = "Test case IDs list cannot be empty")
  private List<Long> testCaseIds;
}
