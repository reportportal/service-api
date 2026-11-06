package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsReviewCommentRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;

/**
 * Turns a stored review comment into what the API returns.
 * <p>
 * Example: a comment on step 2 that the agent could not apply comes back with {@code state NOT_ADDRESSED},
 * its {@code roundNumber} and the reason of the agent.
 */
public final class TmsReviewCommentMapper {

  private TmsReviewCommentMapper() {
  }

  public static TmsReviewCommentRS toRS(TmsTestCaseReviewComment comment) {
    return TmsReviewCommentRS.builder()
        .id(comment.getId())
        .targetType(comment.getTargetType())
        .stepNumber(comment.getStepNumber())
        .text(comment.getCommentText())
        .authorId(comment.getAuthorId())
        .createdAt(comment.getCreatedAt())
        .state(comment.getState())
        .roundNumber(comment.getRoundNumber())
        .agentReason(comment.getAgentReason())
        .fixRoundId(comment.getFixRoundId())
        .build();
  }
}
