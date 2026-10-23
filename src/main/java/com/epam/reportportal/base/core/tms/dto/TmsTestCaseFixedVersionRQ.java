package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What the agent sends when it has fixed a test case in a fix round: the fixed content, its fresh grade and the
 * answer to each comment, in one call, so the case, its scores and its comments never disagree.
 * <p>
 * Example: {@code {"fixPushId": 7, "pipelineIterationId": 52, "manualScenario": {...the fixed steps...},
 * "qualityScores": [{"criterionKey": "atomicity", "score": 15}], "generation": {"costUsd": 0.22},
 * "commentResults": [{"commentId": 11, "addressed": true}, {"commentId": 12, "addressed": false,
 * "reason": "Needs a new case; a fix round cannot create cases"}]}}. Without {@code qualityScores} the case is
 * still updated, but the round ends as "Fixed - Grade failed" and Auto-Ready does not act.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseFixedVersionRQ {

  @NotNull
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
      description = "The push (fix round) this answers; the job received it as an input")
  private Long fixPushId;

  @Schema(description = "The iteration the agent reported for this round (a re-run of the one that produced the case)")
  private Long pipelineIterationId;

  @NotNull
  @Valid
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The full content of the fixed case; it becomes "
      + "the new default version, the previous version stays as history")
  private TmsManualScenarioRQ manualScenario;

  @Size(max = 255)
  @Schema(description = "New title of the case, when the agent changed it")
  private String name;

  @Valid
  @Schema(description = "The grade of the fixed version; empty when the grader failed")
  private List<TmsTestCaseQualityScoreRQ> qualityScores;

  @Valid
  @Schema(description = "Tokens and cost of the round")
  private TmsTestCaseGenerationMetadataRQ generation;

  @Valid
  @Schema(description = "The answer to each pushed comment; a comment without an answer counts as not addressed")
  private List<CommentResult> commentResults;

  /**
   * The answer of the agent to one pushed comment.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentResult {

    @NotNull
    private Long commentId;

    @Schema(description = "Whether the comment was applied")
    private boolean addressed;

    @Size(max = 2000)
    @Schema(description = "Why a comment could not be applied")
    private String reason;
  }
}
