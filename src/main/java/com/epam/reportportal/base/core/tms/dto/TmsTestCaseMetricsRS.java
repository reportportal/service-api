package com.epam.reportportal.base.core.tms.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server-computed AI quality score (against the project's Quality Standard) and
 * generation cost for a test case's current default version. {@code null} when
 * the test case has no recorded scores/generation data for that version
 * (typically a manual test case, or an AI one whose content changed since it
 * was last scored).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseMetricsRS {

  private Integer overallScore;

  private Long evaluatedAt;

  /**
   * {@code true} when the version's manual scenario was modified after
   * {@code evaluatedAt} — the recorded score no longer reflects the current
   * content.
   */
  private boolean obsolete;

  private List<CriterionScore> criteria;

  private Integer tokensIn;

  private Integer tokensOut;

  private String model;

  private String skill;

  private BigDecimal costUsd;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CriterionScore {
    private Long criterionId;
    private String name;
    private Integer maxPoints;
    private Integer score;
  }
}
