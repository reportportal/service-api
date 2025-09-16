package com.epam.ta.reportportal.core.tms.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationError {
  private Long testCaseId;
  private String errorMessage;
}
