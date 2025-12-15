package com.epam.reportportal.core.tms.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchFolderOperationError {
  private Long folderId;
  private String errorMessage;
}
