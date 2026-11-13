package com.epam.reportportal.base.core.tms.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchDeleteTestCaseExecutionsResultRS {

  private int totalCount;
  private int successCount;
  private int failureCount;
  private List<Long> successExecutionIds;
  private List<BatchDeleteTestCaseExecutionError> errors;
}
