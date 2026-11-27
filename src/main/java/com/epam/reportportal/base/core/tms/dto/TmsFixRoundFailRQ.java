package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tells ReportPortal that the fix job of a round failed, so the round does not wait for the timeout.
 * <p>
 * Example: the job crashed at start and its {@code after_script} calls
 * {@code PUT .../review/fix-round/7/fail} with {@code {"reason": "The fix job failed, see https://git/jobs/28537725"}};
 * the round ends as failed at once and the editor can push the comments again. The body may be left out.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsFixRoundFailRQ {

  @Size(max = 2000)
  @Schema(description = "Why the job failed, shown with the round; without it \"The fix job reported that it "
      + "failed\" is stored")
  private String reason;
}
