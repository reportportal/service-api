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
public class BatchOperationResultRS {
  private int totalCount;
  private int successCount;
  private int failureCount;
  private List<BatchOperationError> errors;
}
