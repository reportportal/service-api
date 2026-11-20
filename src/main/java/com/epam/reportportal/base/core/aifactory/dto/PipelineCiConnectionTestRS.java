package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The verdict of "Test connection"; the call always answers 200, a failed test is a verdict, not an error.
 * <p>
 * Example: {@code {"state": "CONNECTION_FAILED", "canStartJobs": false, "reasonCode":
 * "REPOSITORY_OR_CREDENTIAL_INVALID", "reason": "GitLab does not know this project ...", "checkedAt": "..."}}.
 * The jobs named in the connection are not verified.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineCiConnectionTestRS {

  private CiConnectionState state;
  private boolean canStartJobs;
  private CiConnectionReasonCode reasonCode;
  private String reason;
  private Instant checkedAt;
}
