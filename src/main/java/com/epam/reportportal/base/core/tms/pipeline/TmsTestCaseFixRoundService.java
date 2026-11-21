package com.epam.reportportal.base.core.tms.pipeline;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_CASE_REVIEW_CONFLICT;

import com.epam.reportportal.base.core.tms.dto.TmsFixRoundResultRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseQualityService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ends a fix round of a test case: either with the result the agent sends, or as failed.
 * <p>
 * Example: TC103 was pushed to the agent (round 1) with two comments. The agent sends the fixed steps, a new grade of 88 and
 * the answers: the case gets a new default version, becomes Draft ("Modified by agent") with the evaluation 88,
 * the applied comment is Addressed (round 1) and the other is Not addressed with the reason of the agent. Then
 * Auto-Ready may promote the case. If instead the job fails or never answers, {@link #failRound} returns the
 * comments to "not sent" and the case keeps its previous version.
 */
@Service
@RequiredArgsConstructor
public class TmsTestCaseFixRoundService {

  static final String UNANSWERED_REASON = "The agent did not answer this comment";
  static final String JOB_FAILED_REASON = "The fix job reported that it failed";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseFixRoundRepository fixRoundRepository;
  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final PipelineIterationRepository iterationRepository;
  private final TmsTestCaseVersionService versionService;
  private final TmsTestCaseQualityService qualityService;
  private final TmsTestCaseService testCaseService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Ends the round with the result of the agent: replaces the content of the test case with the version fixed by
   * the agent and records the outcome of the round, all in one transaction.
   *
   * @return the test case as it is after the fix
   */
  @Transactional
  public TmsTestCaseRS applyResult(long projectId, Long testCaseId, Long fixRoundId, TmsFixRoundResultRQ rq) {
    var round = fixRoundRepository.findByIdForUpdate(fixRoundId)
        .filter(found -> found.getProjectId().equals(projectId) && found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Fix round '" + fixRoundId + "'"));
    if (round.getStatus() != TmsFixRoundStatus.PENDING) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT,
          "Fix round '" + round.getId() + "' has already ended as " + round.getStatus());
    }
    var testCase = testCaseRepository.findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Test Case '" + testCaseId + "'"));

    var newVersion = versionService.createNewDefaultVersion(projectId, testCase, rq.getManualScenario(),
        rq.getPipelineIterationId());
    qualityService.applyQualityScores(projectId, newVersion, rq.getQualityScores());
    qualityService.applyGenerationMetadata(newVersion, rq.getGeneration());
    var graded = CollectionUtils.isNotEmpty(rq.getQualityScores());

    if (rq.getName() != null && !rq.getName().isBlank()) {
      testCase.setName(rq.getName());
    }
    // a fixed case always goes back to review, also when it was Ready
    testCase.setStatus(TmsTestCaseStatus.DRAFT);
    testCase.setModifiedByAgent(true);
    testCaseRepository.save(testCase);

    markCommentsAnswered(round, rq.getCommentResults());

    round.setStatus(graded ? TmsFixRoundStatus.FIXED : TmsFixRoundStatus.FIXED_GRADE_FAILED);
    round.setResultIterationId(rq.getPipelineIterationId());
    round.setFinishedAt(Instant.now());
    fixRoundRepository.save(round);

    if (graded) {
      promoteWhenAutoReadyAllows(projectId, testCaseId, round);
    }
    eventPublisher.publishEvent(new TmsTestCasesLifecycleChangedEvent(List.of(testCaseId)));
    return testCaseService.getById(projectId, testCaseId);
  }

  /**
   * Ends the round as failed because the fix job itself says it failed, without waiting for the timeout. A round
   * that has already ended is left as it is, so a job that fails after it sent its result changes nothing.
   * <p>
   * Example: round 7 of TC103 is pending and the job's {@code after_script} reports "The fix job failed"; the round
   * ends as failed, the comments are "not sent" again and the editor can push. Called again, or after the result,
   * it does nothing.
   *
   * @param reason why the job failed; a default text is stored when it is empty
   */
  @Transactional
  public void failRoundReportedByJob(long projectId, Long testCaseId, Long fixRoundId, String reason) {
    fixRoundRepository.findById(fixRoundId)
        .filter(found -> found.getProjectId().equals(projectId) && found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Fix round '" + fixRoundId + "'"));
    failRound(fixRoundId, reason == null || reason.isBlank() ? JOB_FAILED_REASON : reason);
  }

  /**
   * Ends the round as failed: the case keeps its previous version and its comments return to "not sent", so
   * the editor can push again or discard them. Does nothing when the round has already ended.
   */
  @Transactional
  public void failRound(Long fixRoundId, String reason) {
    failRound(fixRoundId, reason, null);
  }

  /**
   * Ends the round as failed like {@link #failRound(Long, String)} and remembers the iteration the agent reported
   * for it, so the trace of its Fix stage can still be opened from the round.
   * <p>
   * Example: the agent reported iteration #5 for round 7 and its Fix stage failed; the round ends as failed with
   * {@code resultIterationId = 5}. A round that timed out has no iteration, so it is called with {@code null}.
   * An iteration already recorded on the round is kept.
   *
   * @param resultIterationId the iteration the agent reported for the round, or {@code null} when there is none
   */
  @Transactional
  public void failRound(Long fixRoundId, String reason, Long resultIterationId) {
    var round = fixRoundRepository.findByIdForUpdate(fixRoundId).orElse(null);
    if (round == null || round.getStatus() != TmsFixRoundStatus.PENDING) {
      return;
    }
    if (round.getResultIterationId() == null) {
      round.setResultIterationId(resultIterationId);
    }
    for (var comment : commentRepository.findByFixRoundId(round.getId())) {
      comment.setState(TmsReviewCommentState.NOT_SENT);
      comment.setFixRoundId(null);
      comment.setRoundNumber(null);
      commentRepository.save(comment);
    }
    round.setStatus(TmsFixRoundStatus.FAILED);
    round.setFailureReason(reason);
    round.setFinishedAt(Instant.now());
    fixRoundRepository.save(round);
    eventPublisher.publishEvent(new TmsTestCasesLifecycleChangedEvent(List.of(round.getTestCaseId())));
  }

  private void markCommentsAnswered(TmsTestCaseFixRound round, List<TmsFixRoundResultRQ.CommentResult> results) {
    Map<Long, TmsFixRoundResultRQ.CommentResult> resultsByCommentId = results == null ? Map.of()
        : results.stream().collect(Collectors.toMap(TmsFixRoundResultRQ.CommentResult::getCommentId,
            Function.identity(), (first, second) -> second));
    for (TmsTestCaseReviewComment comment : commentRepository.findByFixRoundId(round.getId())) {
      var result = resultsByCommentId.get(comment.getId());
      var addressed = result != null && result.isAddressed();
      comment.setState(addressed ? TmsReviewCommentState.ADDRESSED : TmsReviewCommentState.NOT_ADDRESSED);
      comment.setRoundNumber(round.getRoundNumber());
      comment.setAgentReason(addressed ? null : (result != null && result.getReason() != null
          ? result.getReason() : UNANSWERED_REASON));
      commentRepository.save(comment);
    }
  }

  /**
   * After the update Auto-Ready runs with the settings of the pipeline that produced the case.
   */
  private void promoteWhenAutoReadyAllows(long projectId, Long testCaseId, TmsTestCaseFixRound round) {
    iterationRepository.findById(round.getPreviousIterationId())
        .flatMap(iteration -> AutoReadySettings.of(iteration.getPipeline()))
        .filter(AutoReadySettings::active)
        .ifPresent(settings -> testCaseService.applyAutoReady(projectId, testCaseId, settings.threshold()));
  }
}
