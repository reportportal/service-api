package com.epam.reportportal.base.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTestCaseOperationError {
  private Long testCaseId;
  private String errorMessage;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BatchTestCaseOperationErrorReason reason;

  public BatchTestCaseOperationError(Long testCaseId, String errorMessage) {
    this(testCaseId, errorMessage, null);
  }
}
