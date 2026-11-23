package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies the Auto-Ready of the pipeline that produced a test case as soon as the case's quality scores are recorded.
 * <p>
 * Without it the promotion depends on the order of the producer's calls: {@link AutoReadyOnStagePassed} runs when the
 * Upload stage passes, and a producer that records the scores afterwards (the grade usually finishes after the upload)
 * would leave every case Draft. Example: Upload passed at 12:00 with TC129 linked, the scores of TC129 (100) are
 * recorded through {@code POST .../test-case/129/generation} at 12:04, the pipeline has
 * {@code {"autoReady": {"enabled": true, "threshold": 95}}}: TC129 becomes Ready, "Auto-Ready (100 >= 95)".
 * <p>
 * The pipeline is the one of the earliest iteration that lists the case. A case no iteration lists (a manual case, an
 * AI case posted by hand) is left to a user.
 */
@Component
@RequiredArgsConstructor
public class AutoReadyOnScoresRecorded {

  private final PipelineStageTestCaseRepository stageTestCaseRepository;
  private final TmsTestCaseService tmsTestCaseService;

  /**
   * Promotes the case if its pipeline has Auto-Ready on and its stored score reaches the threshold.
   *
   * @return whether the case was promoted to Ready by this call
   */
  public boolean evaluate(Long projectId, Long testCaseId) {
    var pipelines = stageTestCaseRepository.findPipelinesByTestCaseIdOldestIterationFirst(testCaseId);
    if (pipelines.isEmpty()) {
      return false;
    }
    var autoReady = AutoReadySettings.of(pipelines.get(0)).filter(AutoReadySettings::active).orElse(null);
    if (autoReady == null) {
      return false;
    }
    return tmsTestCaseService.applyAutoReady(projectId, testCaseId, autoReady.threshold());
  }
}
