package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One fix round of a test case: what was pushed to the agent and where the round stands.
 * <p>
 * Example: {@code {"id": 7, "testCaseId": 103, "roundNumber": 1, "status": "PENDING", "previousIterationId": 41,
 * "comments": [...]}} is what the editor gets back from "Push to agent", and what the job reads to know which
 * comments to apply. When the round ends, {@code status} is {@code FIXED} (with {@code resultIterationId}) or
 * {@code FAILED} (with {@code failureReason}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsFixPushRS {

  private Long id;

  private Long testCaseId;

  private Integer roundNumber;

  private TmsFixPushStatus status;

  private Long previousIterationId;

  private Long resultIterationId;

  private Instant requestedAt;

  private Instant finishedAt;

  private String workflowUrl;

  private String failureReason;

  private List<TmsReviewCommentRS> comments;
}
