package com.epam.reportportal.base.core.tms.pipeline;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_CASE_REVIEW_CONFLICT;

import com.epam.reportportal.base.core.aifactory.connector.CiRunRequest;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.tms.dto.TmsFixRoundRS;
import com.epam.reportportal.base.core.tms.mapper.TmsReviewCommentMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixRoundRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixRound;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixRoundStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseOrigin;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * "Push to agent": sends the unsent review comments of an AI test case to the factory by starting its fix job.
 * <p>
 * Example: TC103 came out of iteration #4 and has two unsent comments. The call records round 1 as pending,
 * marks the comments as sent and starts the fix workflow of the repository of iteration #4 with the inputs
 * {@code test_case_id=103}, {@code fix_round_id=7} and {@code previous_iteration_id=41}. The agent reads the comments
 * of round 7 from ReportPortal, fixes the case and reports its work as a new iteration, a re-run of #4. If the job
 * cannot be started, nothing is recorded and the comments stay "not sent".
 * <p>
 * Deliberately not one transaction: the CI call in the middle must not run inside a database transaction. The
 * round is recorded first (so a second "Push to agent" sees it in flight), then the job is started, and the record is
 * undone if starting fails.
 */
@Service
public class TmsTestCaseFixRoundStartService {

  static final String INPUT_TEST_CASE_ID = "test_case_id";
  static final String INPUT_FIX_ROUND_ID = "fix_round_id";
  static final String INPUT_PREVIOUS_ITERATION_ID = "previous_iteration_id";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final TmsTestCaseFixRoundRepository fixRoundRepository;
  private final PipelineStageTestCaseRepository stageTestCaseRepository;
  private final PipelineIterationRepository iterationRepository;
  private final PipelineStageRepository stageRepository;
  private final IntegrationRepository integrationRepository;
  private final Map<String, CiTriggerConnector> connectorsByType;
  private final TransactionTemplate transactionTemplate;

  public TmsTestCaseFixRoundStartService(TmsTestCaseRepository testCaseRepository,
      TmsTestCaseReviewCommentRepository commentRepository,
      TmsTestCaseFixRoundRepository fixRoundRepository,
      PipelineStageTestCaseRepository stageTestCaseRepository,
      PipelineIterationRepository iterationRepository,
      PipelineStageRepository stageRepository,
      IntegrationRepository integrationRepository,
      List<CiTriggerConnector> connectors,
      PlatformTransactionManager transactionManager) {
    this.testCaseRepository = testCaseRepository;
    this.commentRepository = commentRepository;
    this.fixRoundRepository = fixRoundRepository;
    this.stageTestCaseRepository = stageTestCaseRepository;
    this.iterationRepository = iterationRepository;
    this.stageRepository = stageRepository;
    this.integrationRepository = integrationRepository;
    this.connectorsByType = connectors.stream()
        .collect(Collectors.toMap(CiTriggerConnector::getSupportedExecutorType, Function.identity()));
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Starts a fix round for the test case with all its unsent comments.
   *
   * @return the round that was started
   */
  public TmsFixRoundRS start(long projectId, Long testCaseId, Long userId) {
    var claimed = transactionTemplate.execute(status -> claim(projectId, testCaseId, userId));

    String workflowUrl;
    try {
      workflowUrl = claimed.connector().triggerRun(claimed.integration(), claimed.runRequest());
    } catch (RuntimeException e) {
      transactionTemplate.executeWithoutResult(status -> release(claimed.roundId()));
      throw e;
    }
    return transactionTemplate.execute(status -> recordStarted(claimed.roundId(), workflowUrl));
  }

  /**
   * The round and the comments it carries; this is what the fix job reads.
   */
  public TmsFixRoundRS getRound(long projectId, Long testCaseId, Long roundId) {
    var round = fixRoundRepository.findById(roundId)
        .filter(found -> found.getProjectId().equals(projectId) && found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Fix round '" + roundId + "'"));
    return toRS(round, commentRepository.findByFixRoundId(roundId));
  }

  private Claimed claim(long projectId, Long testCaseId, Long userId) {
    var testCase = testCaseRepository.findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Test Case '" + testCaseId + "'"));
    assertCanStartRound(testCase);
    var unsent = commentRepository.findByTestCaseIdAndState(testCaseId, TmsReviewCommentState.NOT_SENT);
    if (unsent.isEmpty()) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Add at least one review comment before pushing to the agent");
    }

    var previousIterationId = findIterationThatProduced(testCaseId);
    var execution = findCiExecution(previousIterationId);
    var connector = connectorsByType.get(execution.getExecutorType());
    var project = iterationRepository.findById(previousIterationId).orElseThrow().getPipeline().getProject();
    var integration = resolveIntegration(project, connector);

    var round = new TmsTestCaseFixRound();
    round.setTestCaseId(testCaseId);
    round.setProjectId(projectId);
    round.setRoundNumber(fixRoundRepository.findLastRoundNumber(testCaseId) + 1);
    round.setStatus(TmsFixRoundStatus.PENDING);
    round.setPreviousIterationId(previousIterationId);
    round.setRequestedBy(userId);
    round.setRequestedAt(Instant.now());
    try {
      round = fixRoundRepository.saveAndFlush(round);
    } catch (DataIntegrityViolationException e) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT, "The agent is already fixing this Test Case");
    }
    for (var comment : unsent) {
      comment.setState(TmsReviewCommentState.SENT);
      comment.setFixRoundId(round.getId());
      commentRepository.save(comment);
    }

    var inputs = new LinkedHashMap<String, String>();
    inputs.put(INPUT_TEST_CASE_ID, String.valueOf(testCaseId));
    inputs.put(INPUT_FIX_ROUND_ID, String.valueOf(round.getId()));
    inputs.put(INPUT_PREVIOUS_ITERATION_ID, String.valueOf(previousIterationId));
    var parameters = execution.getRetryParameters();
    var runRequest = new CiRunRequest(parameters.get("repo"), parameters.get("workflowRef"), inputs);
    return new Claimed(round.getId(), connector, integration, runRequest);
  }

  private void assertCanStartRound(TmsTestCase testCase) {
    if (testCase.getOrigin() != TmsTestCaseOrigin.AI) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Only AI test cases can be pushed to the agent");
    }
    if (fixRoundRepository.existsByTestCaseIdAndStatus(testCase.getId(), TmsFixRoundStatus.PENDING)) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT, "The agent is already fixing this Test Case");
    }
  }

  /**
   * The latest iteration that has the test case linked to one of its stages, i.e. the one that created it or
   * that last fixed it.
   */
  private Long findIterationThatProduced(Long testCaseId) {
    var iterationIds = stageTestCaseRepository.findIterationIdsByTestCaseIdIn(List.of(testCaseId));
    if (iterationIds.isEmpty()) {
      throw new ReportPortalException(BAD_REQUEST_ERROR,
          "This Test Case was not produced by a pipeline iteration, so there is no agent to round it to");
    }
    return Collections.max(iterationIds);
  }

  /**
   * The first stage of the iteration that tells how to reach its CI system: it has a kind of CI that ReportPortal
   * can start runs in, and the repository and workflow it ran in.
   */
  private PipelineExecution findCiExecution(Long iterationId) {
    return stageRepository.findByIterationIdOrderBySequenceAsc(iterationId).stream()
        .map(stage -> stage.getExecution() == null ? null : stage.getExecution().getContent())
        .filter(execution -> execution != null && connectorsByType.containsKey(execution.getExecutorType())
            && execution.getRetryParameters() != null && execution.getRetryParameters().get("repo") != null)
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            "The pipeline is not connected to CI: none of the stages of its iteration tells where it ran"));
  }

  private Integration resolveIntegration(Project project, CiTriggerConnector connector) {
    var typeName = connector.getIntegrationTypeName();
    return integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION).stream()
        .filter(i -> i.isEnabled() && i.getType() != null && typeName.equalsIgnoreCase(i.getType().getName()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            "No enabled '" + typeName + "' automation integration configured for this project"));
  }

  /**
   * The job could not be started: the round never existed, and the comments are still "not sent".
   */
  private void release(Long roundId) {
    for (TmsTestCaseReviewComment comment : commentRepository.findByFixRoundId(roundId)) {
      comment.setState(TmsReviewCommentState.NOT_SENT);
      comment.setFixRoundId(null);
      commentRepository.save(comment);
    }
    fixRoundRepository.deleteById(roundId);
  }

  private TmsFixRoundRS recordStarted(Long roundId, String workflowUrl) {
    var round = fixRoundRepository.findById(roundId).orElseThrow();
    round.setWorkflowUrl(workflowUrl);
    fixRoundRepository.save(round);
    return toRS(round, commentRepository.findByFixRoundId(roundId));
  }

  private TmsFixRoundRS toRS(TmsTestCaseFixRound round, List<TmsTestCaseReviewComment> comments) {
    return TmsFixRoundRS.builder()
        .id(round.getId())
        .testCaseId(round.getTestCaseId())
        .roundNumber(round.getRoundNumber())
        .status(round.getStatus())
        .previousIterationId(round.getPreviousIterationId())
        .resultIterationId(round.getResultIterationId())
        .requestedAt(round.getRequestedAt())
        .finishedAt(round.getFinishedAt())
        .workflowUrl(round.getWorkflowUrl())
        .failureReason(round.getFailureReason())
        .comments(comments.stream().map(TmsReviewCommentMapper::toRS).toList())
        .build();
  }

  private record Claimed(Long roundId, CiTriggerConnector connector, Integration integration, CiRunRequest runRequest) {
  }
}
