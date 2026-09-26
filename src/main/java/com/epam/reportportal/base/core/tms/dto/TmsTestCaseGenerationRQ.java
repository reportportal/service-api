package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for {@code POST .../tms/test-case/{testCaseId}/generation} — the dedicated
 * endpoint an AI agent uses to report quality scores and generation cost for the test
 * case's current default version, separate from editing its content
 * ({@link TmsTestCaseRQ}). Submitting this is what flips {@code origin} to {@code AI}
 * and applies the {@code status} ratchet — see {@code PIPELINE_AGENT_INTEGRATION_GUIDE.md}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseGenerationRQ {

  @Valid
  private List<TmsTestCaseQualityScoreRQ> qualityScores;

  @Valid
  private TmsTestCaseGenerationMetadataRQ generation;
}
