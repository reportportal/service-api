package com.epam.ta.reportportal.core.tms.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTestCaseOperationResultRS {
  private Integer totalCount;
  private Integer successCount;
  private Integer failureCount;
  private List<Long> successTestCaseIds;
  private List<BatchTestCaseOperationError> errors;
}
