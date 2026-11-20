package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The automation block of a Test Case: whether it can be sent now and where it stands.
 * <p>
 * Example: {@code {"ciConnection": "CONNECTED", "fixInProgress": false, "status": "IN_PROGRESS",
 * "scenarioChangedAfterAutomation": false, "iteration": {"pipelineId": 17, "iterationId": 452, "number": 8}}}.
 * Launch, merge request and last result are added when the automation report carries them (not yet).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsAutomationStateRS {

  private CiConnectionState ciConnection;
  private boolean fixInProgress;
  private TmsAutomationStatus status;
  private boolean scenarioChangedAfterAutomation;
  private TmsAutomationIterationRefRS iteration;
}
