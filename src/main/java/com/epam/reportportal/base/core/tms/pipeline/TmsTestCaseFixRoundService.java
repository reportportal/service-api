package com.epam.reportportal.base.core.tms.pipeline;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_CASE_REVIEW_CONFLICT;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseFixedVersionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.event.TmsTestCasesLifecycleChangedEvent;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseQualityService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixPush;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
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
 * Ends a fix round of a test case: either with the fixed version the agent sends, or as failed.
 * <p>
 * Example: TC103 was pushed (round 1) with two comments. The agent sends the fixed steps, a new grade of 88 and
 * the answers: the case gets a new default version, becomes Draft ("Modified by agent") with the evaluation 88,
 * the applied comment is Addressed (round 1) and the other is Not addressed with the reason of the agent. Then
 * Auto-Ready may promote the case. If instead the job fails or never answers, {@link #failRound} returns the
 * comments to "not sent" and the case keeps its previous version.
 */
@Service
@RequiredArgsConstructor
public class TmsTestCaseFixRoundService {

  static final String UNANSWERED_REASON = "The agent did not answer this comment";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseFixPushRepository fixPushRepository;
  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final PipelineIterationRepository iterationRepository;
  private final TmsTestCaseVersionService versionService;
  private final TmsTestCaseQualityService qualityService;
  private final TmsTestCaseService testCaseService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Replaces the content of the pushed test case with the version fixed by the agent and records the outcome of
   * the round, all in one transaction.
   *
   * @return the test case as it is after the fix
   */
  @Transactional
  public TmsTestCaseRS applyFixedVersion(long projectId, Long testCaseId, TmsTestCaseFixedVersionRQ rq) {
    var push = fixPushRepository.findByIdForUpdate(rq.getFixPushId())
        .filter(found -> found.getProjectId().equals(projectId) && found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Fix round '" + rq.getFixPushId() + "'"));
    if (push.getStatus() != TmsFixPushStatus.PENDING) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT,
          "Fix round '" + push.getId() + "' has already ended as " + push.getStatus());
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

    markCommentsAnswered(push, rq.getCommentResults());

    push.setStatus(graded ? TmsFixPushStatus.FIXED : TmsFixPushStatus.FIXED_GRADE_FAILED);
    push.setResultIterationId(rq.getPipelineIterationId());
    push.setFinishedAt(Instant.now());
    fixPushRepository.save(push);

    if (graded) {
      promoteWhenAutoReadyAllows(projectId, testCaseId, push);
    }
    eventPublisher.publishEvent(new TmsTestCasesLifecycleChangedEvent(List.of(testCaseId)));
    return testCaseService.getById(projectId, testCaseId);
  }

  /**
   * Ends the round as failed: the case keeps its previous version and its comments return to "not sent", so
   * the editor can push again or discard them. Does nothing when the round has already ended.
   */
  @Transactional
  public void failRound(Long fixPushId, String reason) {
    var push = fixPushRepository.findByIdForUpdate(fixPushId).orElse(null);
    if (push == null || push.getStatus() != TmsFixPushStatus.PENDING) {
      return;
    }
    for (var comment : commentRepository.findByFixPushId(push.getId())) {
      comment.setState(TmsReviewCommentState.NOT_SENT);
      comment.setFixPushId(null);
      comment.setRoundNumber(null);
      commentRepository.save(comment);
    }
    push.setStatus(TmsFixPushStatus.FAILED);
    push.setFailureReason(reason);
    push.setFinishedAt(Instant.now());
    fixPushRepository.save(push);
    eventPublisher.publishEvent(new TmsTestCasesLifecycleChangedEvent(List.of(push.getTestCaseId())));
  }

  private void markCommentsAnswered(TmsTestCaseFixPush push, List<TmsTestCaseFixedVersionRQ.CommentResult> results) {
    Map<Long, TmsTestCaseFixedVersionRQ.CommentResult> resultsByCommentId = results == null ? Map.of()
        : results.stream().collect(Collectors.toMap(TmsTestCaseFixedVersionRQ.CommentResult::getCommentId,
            Function.identity(), (first, second) -> second));
    for (TmsTestCaseReviewComment comment : commentRepository.findByFixPushId(push.getId())) {
      var result = resultsByCommentId.get(comment.getId());
      var addressed = result != null && result.isAddressed();
      comment.setState(addressed ? TmsReviewCommentState.ADDRESSED : TmsReviewCommentState.NOT_ADDRESSED);
      comment.setRoundNumber(push.getRoundNumber());
      comment.setAgentReason(addressed ? null : (result != null && result.getReason() != null
          ? result.getReason() : UNANSWERED_REASON));
      commentRepository.save(comment);
    }
  }

  /**
   * After the update Auto-Ready runs with the settings of the pipeline that produced the case.
   */
  private void promoteWhenAutoReadyAllows(long projectId, Long testCaseId, TmsTestCaseFixPush push) {
    iterationRepository.findById(push.getPreviousIterationId())
        .flatMap(iteration -> AutoReadySettings.of(iteration.getPipeline()))
        .filter(AutoReadySettings::active)
        .ifPresent(settings -> testCaseService.applyAutoReady(projectId, testCaseId, settings.threshold()));
  }
}
