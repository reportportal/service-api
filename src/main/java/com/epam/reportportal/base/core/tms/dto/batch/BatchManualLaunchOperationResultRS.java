package com.epam.reportportal.base.core.tms.dto.batch;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchManualLaunchOperationResultRS {
  private Integer totalCount;
  private Integer successCount;
  private Integer failureCount;
  private List<Long> successLaunchIds;
  private List<BatchManualLaunchOperationError> errors;
}
