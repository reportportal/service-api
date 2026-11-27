package com.epam.reportportal.base.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A submitted Test Case that was not sent, with the reason.
 * <p>
 * Example: {@code {"id": 43, "displayId": "TC43", "reason": "AUTOMATION_IN_PROGRESS"}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAutomationSkippedRS {

  private Long id;
  private String displayId;
  private TmsAutomationSkipReason reason;
}
