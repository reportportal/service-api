package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A comment of a reviewer on the precondition or a step of an AI-generated test case, written for the agent.
 * <p>
 * Example: the editor comments step 2 of TC103 with "Name the exact toast text". It is a {@code STEP} comment
 * with step number 2, {@code NOT_SENT} until the editor pushes the case to the agent. When the fix round ends
 * the comment is {@code ADDRESSED} (round 1), or {@code NOT_ADDRESSED} with the reason given by the agent, e.g.
 * "Needs a new case; a fix round cannot create cases".
 */
@Entity
@Table(name = "tms_test_case_review_comment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseReviewComment implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "test_case_id", nullable = false)
  private Long testCaseId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private TmsReviewCommentTarget targetType;

  /**
   * 1-based position of the commented step; only for {@link TmsReviewCommentTarget#STEP}.
   */
  @Column(name = "step_number")
  private Integer stepNumber;

  @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
  private String commentText;

  @Column(name = "author_id", nullable = false)
  private Long authorId;

  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  private TmsReviewCommentState state = TmsReviewCommentState.NOT_SENT;

  /**
   * The fix round that carried the comment to the agent.
   */
  @Column(name = "fix_round_id")
  private Long fixRoundId;

  @Column(name = "round_number")
  private Integer roundNumber;

  /**
   * The answer of the agent to a comment it could not apply.
   */
  @Column(name = "agent_reason", columnDefinition = "TEXT")
  private String agentReason;
}
