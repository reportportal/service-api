package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.event.PipelineStageRetryRequestedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PipelineStageRetryServiceImpl implements PipelineStageRetryService {

  static final String PENDING_STATUS = "Pending";

  private final PipelineStageRepository pipelineStageRepository;
  private final PipelineStageAttemptRepository pipelineStageAttemptRepository;
  private final PipelineIterationRefresher iterationRefresher;
  private final IntegrationRepository integrationRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Map<String, CiTriggerConnector> connectorsByType;
  private final TransactionTemplate transactionTemplate;

  public PipelineStageRetryServiceImpl(PipelineStageRepository pipelineStageRepository,
      PipelineStageAttemptRepository pipelineStageAttemptRepository,
      PipelineIterationRefresher iterationRefresher,
      IntegrationRepository integrationRepository,
      ApplicationEventPublisher eventPublisher,
      List<CiTriggerConnector> connectors,
      PlatformTransactionManager transactionManager) {
    this.pipelineStageRepository = pipelineStageRepository;
    this.pipelineStageAttemptRepository = pipelineStageAttemptRepository;
    this.iterationRefresher = iterationRefresher;
    this.integrationRepository = integrationRepository;
    this.eventPublisher = eventPublisher;
    this.connectorsByType = connectors
        .stream()
        .collect(Collectors.toMap(CiTriggerConnector::getSupportedExecutorType, Function.identity()));
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Deliberately not {@code @Transactional}: the external CI call in the middle must not run
   * inside a DB transaction (it can take up to the connector's response timeout, and holding a
   * connection/transaction open for a slow third-party call risks exhausting the pool and leaves
   * an inconsistent state if the later save fails after the trigger already succeeded). The DB
   * steps each get their own short, explicit transaction instead: the stage is claimed (locked,
   * validated and flipped to pending) before the CI call so a concurrent retry sees it in flight,
   * and the claim is reverted if the CI call fails.
   */
  @Override
  public PipelineStageRetryRS retryPipelineIterationStage(Long projectId, Long userId, Long iterationId, Long stageId, PipelineStageRetryRQ rq) {
    var resolved = transactionTemplate.execute(status -> claimStageForRetry(projectId, iterationId, stageId));

    String triggeredRunUrl;
    try {
      triggeredRunUrl = resolved.connector().triggerStageRerun(resolved.integration(), resolved.stage());
    } catch (RuntimeException e) {
      transactionTemplate.executeWithoutResult(
          status -> releaseClaim(stageId, iterationId, projectId, resolved.previousStatus(),
              resolved.previousCategory()));
      throw e;
    }

    var comment = rq == null ? null : rq.getComment();
    return transactionTemplate.execute(
        status -> persistRetryResult(stageId, iterationId, projectId, userId, comment, triggeredRunUrl,
            resolved.finishedAttempt()));
  }

  private ResolvedStageForRetry claimStageForRetry(Long projectId, Long iterationId, Long stageId) {
    var stage = findStageOrThrow(projectId, iterationId, stageId, true);

    var category = stage.getStatusCategory();
    if (category == StatusCategory.PENDING || category == StatusCategory.RUNNING) {
      throw new ReportPortalException(ErrorType.PIPELINE_STAGE_RETRY_CONFLICT,
          "Stage '" + stageId + "' is already running or being retried");
    }
    if (category == StatusCategory.PASSED || category == StatusCategory.SKIPPED) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Stage '" + stageId + "' has passed and cannot be retried");
    }
    var execution = stage.getExecution() == null ? null : stage.getExecution().getContent();
    if (category == StatusCategory.UNKNOWN || stage.getOwnedBy() == StageOwner.REPORTPORTAL
        || execution == null || !execution.isRetryable()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage '" + stageId + "' is not retryable");
    }

    var connector = connectorsByType.get(execution.getExecutorType());
    if (connector == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Nothing can run a stage of execution type '" + execution.getExecutorType() + "' again");
    }

    var project = stage.getIteration().getPipeline().getProject();
    var integration = resolveIntegration(project, connector);

    var previousStatus = stage.getStatus();
    var finishedAttempt = PipelineStageAttempt.snapshotOf(stage);
    setStageStatus(stage, PENDING_STATUS, StatusCategory.PENDING);
    return new ResolvedStageForRetry(stage, integration, connector, previousStatus, category, finishedAttempt);
  }

  private void releaseClaim(Long stageId, Long iterationId, Long projectId, String previousStatus,
      StatusCategory previousCategory) {
    setStageStatus(findStageOrThrow(projectId, iterationId, stageId, true), previousStatus, previousCategory);
  }

  /**
   * Saves the stage's new status and keeps the iteration's derived status in step with it.
   */
  private void setStageStatus(PipelineStage stage, String status, StatusCategory category) {
    stage.setStatus(status);
    stage.setStatusCategory(category);
    pipelineStageRepository.save(stage);
    iterationRefresher.refresh(stage.getIteration());
  }

  private PipelineStageRetryRS persistRetryResult(Long stageId, Long iterationId, Long projectId, Long userId,
      String comment, String triggeredRunUrl, PipelineStageAttempt finishedAttempt) {
    var stage = findStageOrThrow(projectId, iterationId, stageId, false);

    // the attempt that just ended goes to history; the stage starts a fresh one
    pipelineStageAttemptRepository.save(finishedAttempt);
    stage.setAttempt(finishedAttempt.getAttempt() + 1);
    stage.setStartedAt(null);
    stage.setFinishedAt(null);
    stage.setLastRetriedAt(Instant.now());
    stage.setLastRetriedBy(userId);
    pipelineStageRepository.save(stage);

    eventPublisher.publishEvent(new PipelineStageRetryRequestedEvent(stage.getId(), iterationId, projectId, userId, comment));

    return PipelineStageRetryRS.builder()
        .stageId(stage.getId())
        .status(stage.getStatus())
        .statusCategory(stage.getStatusCategory())
        .triggeredRunUrl(triggeredRunUrl)
        .build();
  }

  private PipelineStage findStageOrThrow(Long projectId, Long iterationId, Long stageId, boolean forUpdate) {
    return (forUpdate ? pipelineStageRepository.findByIdForUpdate(stageId) : pipelineStageRepository.findById(stageId))
        .filter(s -> s.getIteration().getId().equals(iterationId))
        .filter(s -> s.getIteration().getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline stage '" + stageId + "'"));
  }

  private Integration resolveIntegration(Project project, CiTriggerConnector connector) {
    var typeName = connector.getIntegrationTypeName();
    return integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION).stream()
        .filter(i -> i.isEnabled() && i.getType() != null && typeName.equalsIgnoreCase(i.getType().getName()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "No enabled '" + typeName + "' automation integration configured for this project"));
  }

  private record ResolvedStageForRetry(PipelineStage stage, Integration integration, CiTriggerConnector connector,
                                       String previousStatus, StatusCategory previousCategory,
                                       PipelineStageAttempt finishedAttempt) {}
}
