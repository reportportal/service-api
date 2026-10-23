package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentTarget;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A review comment of a test case with its state.
 * <p>
 * Example: {@code {"id": 11, "targetType": "STEP", "stepNumber": 2, "text": "Name the exact toast text",
 * "state": "ADDRESSED", "roundNumber": 1}}; a comment the agent could not apply is {@code "state":
 * "NOT_ADDRESSED"} with an {@code agentReason}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsReviewCommentRS {

  private Long id;

  private TmsReviewCommentTarget targetType;

  private Integer stepNumber;

  private String text;

  private Long authorId;

  private Instant createdAt;

  private TmsReviewCommentState state;

  private Integer roundNumber;

  private String agentReason;

  /**
   * The push that carried the comment to the agent; the agent reads the comments of its job by it.
   */
  private Long fixPushId;
}
