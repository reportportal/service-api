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
public class BatchFolderOperationResultRS {

  private Integer totalCount;
  private Integer successCount;
  private Integer failureCount;
  private List<Long> successFolderIds;
  private List<BatchFolderOperationError> errors;
}
