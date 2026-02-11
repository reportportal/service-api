package com.epam.reportportal.base.core.tms.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTestCaseOperationError {

  private Long testCaseIds;
  private String errorMessage;
}
