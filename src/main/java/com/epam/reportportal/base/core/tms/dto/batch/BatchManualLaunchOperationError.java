package com.epam.reportportal.base.core.tms.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchManualLaunchOperationError {
  private Long launchId;
  private String errorMessage;
}
