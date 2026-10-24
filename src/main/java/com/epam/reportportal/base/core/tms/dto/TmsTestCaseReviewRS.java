package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Everything the review strip of a test case needs: its comments and where the review stands.
 * <p>
 * Example: {@code {"comments": [...], "unsentCount": 2, "fixInProgress": false}} shows "Review comments - 2 not
 * sent"; after the push {@code "fixInProgress": true, "fixRound": 1} shows "Agent is fixing - Fix round 1" and
 * the comments are read-only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTestCaseReviewRS {

  private List<TmsReviewCommentRS> comments;

  private int unsentCount;

  private boolean fixInProgress;

  private Integer fixRound;
}
