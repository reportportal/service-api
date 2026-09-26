package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseGenerationMetadataRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseMetricsRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseQualityScoreRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import java.util.List;

/**
 * Persists and computes the AI quality score / generation cost data a test
 * case version carries, per the project's {@code TmsQualityStandard}.
 */
public interface TmsTestCaseQualityService {

  /**
   * {@code true} when the request carries AI-shaped data (quality scores or
   * generation metadata) — the signal used to infer {@code origin = AI} on
   * create, since the client never sets {@code origin} directly.
   */
  boolean hasAiSignal(List<TmsTestCaseQualityScoreRQ> qualityScores, TmsTestCaseGenerationMetadataRQ generation);

  /**
   * Replaces the quality scores recorded for a version. No-op when {@code
   * qualityScores} is null (patch semantics: absent means "leave as is").
   *
   * @param projectId project the criteria must belong to
   */
  void applyQualityScores(Long projectId, TmsTestCaseVersion version, List<TmsTestCaseQualityScoreRQ> qualityScores);

  /**
   * Replaces the generation metadata recorded for a version. No-op when
   * {@code generation} is null.
   */
  void applyGenerationMetadata(TmsTestCaseVersion version, TmsTestCaseGenerationMetadataRQ generation);

  /**
   * Builds the read-only {@code metrics} block for a version: {@code null} if
   * the version has neither quality scores nor generation metadata recorded.
   */
  TmsTestCaseMetricsRS buildMetrics(TmsTestCaseVersion version);
}
