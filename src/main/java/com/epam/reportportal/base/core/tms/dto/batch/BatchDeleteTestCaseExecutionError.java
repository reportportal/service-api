package com.epam.reportportal.base.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDeleteTestCaseExecutionError {

  private Long executionId;
  private String errorMessage;
}
