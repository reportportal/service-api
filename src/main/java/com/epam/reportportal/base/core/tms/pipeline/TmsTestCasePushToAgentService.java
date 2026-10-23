package com.epam.reportportal.base.core.tms.pipeline;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.TEST_CASE_REVIEW_CONFLICT;

import com.epam.reportportal.base.core.aifactory.connector.CiRunRequest;
import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.tms.dto.TmsFixPushRS;
import com.epam.reportportal.base.core.tms.mapper.TmsReviewCommentMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseFixPushRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseReviewCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixPush;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
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
 * Example: TC103 came out of iteration #4 and has two unsent comments. The push records round 1 as pending,
 * marks the comments as sent and starts the fix workflow of the repository of iteration #4 with the inputs
 * {@code test_case_id=103}, {@code push_id=7} and {@code previous_iteration_id=41}. The agent reads the comments
 * of push 7 from ReportPortal, fixes the case and reports its work as a new iteration, a re-run of #4. If the job
 * cannot be started, nothing is recorded and the comments stay "not sent".
 * <p>
 * Deliberately not one transaction: the CI call in the middle must not run inside a database transaction. The
 * round is recorded first (so a second push sees it in flight), then the job is started, and the record is
 * undone if starting fails.
 */
@Service
public class TmsTestCasePushToAgentService {

  static final String INPUT_TEST_CASE_ID = "test_case_id";
  static final String INPUT_PUSH_ID = "push_id";
  static final String INPUT_PREVIOUS_ITERATION_ID = "previous_iteration_id";

  private final TmsTestCaseRepository testCaseRepository;
  private final TmsTestCaseReviewCommentRepository commentRepository;
  private final TmsTestCaseFixPushRepository fixPushRepository;
  private final PipelineStageTestCaseRepository stageTestCaseRepository;
  private final PipelineIterationRepository iterationRepository;
  private final PipelineStageRepository stageRepository;
  private final IntegrationRepository integrationRepository;
  private final Map<String, CiTriggerConnector> connectorsByType;
  private final TransactionTemplate transactionTemplate;

  public TmsTestCasePushToAgentService(TmsTestCaseRepository testCaseRepository,
      TmsTestCaseReviewCommentRepository commentRepository,
      TmsTestCaseFixPushRepository fixPushRepository,
      PipelineStageTestCaseRepository stageTestCaseRepository,
      PipelineIterationRepository iterationRepository,
      PipelineStageRepository stageRepository,
      IntegrationRepository integrationRepository,
      List<CiTriggerConnector> connectors,
      PlatformTransactionManager transactionManager) {
    this.testCaseRepository = testCaseRepository;
    this.commentRepository = commentRepository;
    this.fixPushRepository = fixPushRepository;
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
  public TmsFixPushRS push(long projectId, Long testCaseId, Long userId) {
    var claimed = transactionTemplate.execute(status -> claim(projectId, testCaseId, userId));

    String workflowUrl;
    try {
      workflowUrl = claimed.connector().triggerRun(claimed.integration(), claimed.runRequest());
    } catch (RuntimeException e) {
      transactionTemplate.executeWithoutResult(status -> release(claimed.pushId()));
      throw e;
    }
    return transactionTemplate.execute(status -> recordStarted(claimed.pushId(), workflowUrl));
  }

  /**
   * The round and the comments it carries; this is what the fix job reads.
   */
  public TmsFixPushRS getPush(long projectId, Long testCaseId, Long pushId) {
    var push = fixPushRepository.findById(pushId)
        .filter(found -> found.getProjectId().equals(projectId) && found.getTestCaseId().equals(testCaseId))
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Fix round '" + pushId + "'"));
    return toRS(push, commentRepository.findByFixPushId(pushId));
  }

  private Claimed claim(long projectId, Long testCaseId, Long userId) {
    var testCase = testCaseRepository.findByProjectIdAndId(projectId, testCaseId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Test Case '" + testCaseId + "'"));
    assertCanBePushed(testCase);
    var unsent = commentRepository.findByTestCaseIdAndState(testCaseId, TmsReviewCommentState.NOT_SENT);
    if (unsent.isEmpty()) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Add at least one review comment before pushing to the agent");
    }

    var previousIterationId = findIterationThatProduced(testCaseId);
    var execution = findCiExecution(previousIterationId);
    var connector = connectorsByType.get(execution.getExecutorType());
    var project = iterationRepository.findById(previousIterationId).orElseThrow().getPipeline().getProject();
    var integration = resolveIntegration(project, connector);

    var push = new TmsTestCaseFixPush();
    push.setTestCaseId(testCaseId);
    push.setProjectId(projectId);
    push.setRoundNumber(fixPushRepository.findLastRoundNumber(testCaseId) + 1);
    push.setStatus(TmsFixPushStatus.PENDING);
    push.setPreviousIterationId(previousIterationId);
    push.setRequestedBy(userId);
    push.setRequestedAt(Instant.now());
    try {
      push = fixPushRepository.saveAndFlush(push);
    } catch (DataIntegrityViolationException e) {
      throw new ReportPortalException(TEST_CASE_REVIEW_CONFLICT, "The agent is already fixing this Test Case");
    }
    for (var comment : unsent) {
      comment.setState(TmsReviewCommentState.SENT);
      comment.setFixPushId(push.getId());
      commentRepository.save(comment);
    }

    var inputs = new LinkedHashMap<String, String>();
    inputs.put(INPUT_TEST_CASE_ID, String.valueOf(testCaseId));
    inputs.put(INPUT_PUSH_ID, String.valueOf(push.getId()));
    inputs.put(INPUT_PREVIOUS_ITERATION_ID, String.valueOf(previousIterationId));
    var parameters = execution.getRetryParameters();
    var runRequest = new CiRunRequest(parameters.get("repo"), parameters.get("workflowRef"), inputs);
    return new Claimed(push.getId(), connector, integration, runRequest);
  }

  private void assertCanBePushed(TmsTestCase testCase) {
    if (testCase.getOrigin() != TmsTestCaseOrigin.AI) {
      throw new ReportPortalException(BAD_REQUEST_ERROR, "Only AI test cases can be pushed to the agent");
    }
    if (fixPushRepository.existsByTestCaseIdAndStatus(testCase.getId(), TmsFixPushStatus.PENDING)) {
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
          "This Test Case was not produced by a pipeline iteration, so there is no agent to push it to");
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
  private void release(Long pushId) {
    for (TmsTestCaseReviewComment comment : commentRepository.findByFixPushId(pushId)) {
      comment.setState(TmsReviewCommentState.NOT_SENT);
      comment.setFixPushId(null);
      commentRepository.save(comment);
    }
    fixPushRepository.deleteById(pushId);
  }

  private TmsFixPushRS recordStarted(Long pushId, String workflowUrl) {
    var push = fixPushRepository.findById(pushId).orElseThrow();
    push.setWorkflowUrl(workflowUrl);
    fixPushRepository.save(push);
    return toRS(push, commentRepository.findByFixPushId(pushId));
  }

  private TmsFixPushRS toRS(TmsTestCaseFixPush push, List<TmsTestCaseReviewComment> comments) {
    return TmsFixPushRS.builder()
        .id(push.getId())
        .testCaseId(push.getTestCaseId())
        .roundNumber(push.getRoundNumber())
        .status(push.getStatus())
        .previousIterationId(push.getPreviousIterationId())
        .resultIterationId(push.getResultIterationId())
        .requestedAt(push.getRequestedAt())
        .finishedAt(push.getFinishedAt())
        .workflowUrl(push.getWorkflowUrl())
        .failureReason(push.getFailureReason())
        .comments(comments.stream().map(TmsReviewCommentMapper::toRS).toList())
        .build();
  }

  private record Claimed(Long pushId, CiTriggerConnector connector, Integration integration, CiRunRequest runRequest) {
  }
}
