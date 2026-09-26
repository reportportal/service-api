package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.connector.CiTriggerConnector;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;
import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.core.aifactory.event.PipelineStageRetryRequestedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
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

  private static final Map<CiProvider, String> INTEGRATION_TYPE_NAME_BY_PROVIDER = Map.of(
      CiProvider.GITHUB_ACTIONS, "github-actions",
      CiProvider.GITLAB_CI, "gitlab-ci"
  );

  private final PipelineStageRepository pipelineStageRepository;
  private final IntegrationRepository integrationRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Map<CiProvider, CiTriggerConnector> connectorsByProvider;
  private final TransactionTemplate transactionTemplate;

  public PipelineStageRetryServiceImpl(PipelineStageRepository pipelineStageRepository,
      IntegrationRepository integrationRepository,
      ApplicationEventPublisher eventPublisher,
      List<CiTriggerConnector> connectors,
      PlatformTransactionManager transactionManager) {
    this.pipelineStageRepository = pipelineStageRepository;
    this.integrationRepository = integrationRepository;
    this.eventPublisher = eventPublisher;
    this.connectorsByProvider = connectors
        .stream()
        .collect(Collectors.toMap(CiTriggerConnector::getSupportedProvider, Function.identity()));
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Deliberately not {@code @Transactional}: the external CI call in the middle must not run
   * inside a DB transaction (it can take up to the connector's response timeout, and holding a
   * connection/transaction open for a slow third-party call risks exhausting the pool and leaves
   * an inconsistent state if the later save fails after the trigger already succeeded). The two
   * DB steps each get their own short, explicit transaction instead.
   */
  @Override
  public PipelineStageRetryRS retryPipelineIterationStage(Long projectId, Long userId, Long iterationId, Long stageId, PipelineStageRetryRQ rq) {
    var resolved = transactionTemplate.execute(status -> resolveStageForRetry(projectId, iterationId, stageId));

    var triggeredRunUrl = resolved.connector().triggerStageRerun(resolved.integration(), resolved.stage());

    var comment = rq == null ? null : rq.getComment();
    return transactionTemplate.execute(
        status -> persistRetryResult(stageId, iterationId, projectId, userId, comment, triggeredRunUrl));
  }

  private ResolvedStageForRetry resolveStageForRetry(Long projectId, Long iterationId, Long stageId) {
    var stage = findStageOrThrow(projectId, iterationId, stageId);

    if (!stage.isRetryable() || stage.getCiProvider() == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage '" + stageId + "' is not retryable");
    }

    var project = stage.getIteration().getPipeline().getProject();
    var integration = resolveIntegration(project, stage.getCiProvider());

    var connector = connectorsByProvider.get(stage.getCiProvider());
    if (connector == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unsupported CI provider: " + stage.getCiProvider());
    }

    return new ResolvedStageForRetry(stage, integration, connector);
  }

  private PipelineStageRetryRS persistRetryResult(Long stageId, Long iterationId, Long projectId, Long userId,
      String comment, String triggeredRunUrl) {
    var stage = findStageOrThrow(projectId, iterationId, stageId);

    stage.setStatus(PipelineRunStatus.PENDING);
    stage.setLastRetriedAt(Instant.now());
    stage.setLastRetriedBy(userId);
    pipelineStageRepository.save(stage);

    eventPublisher.publishEvent(new PipelineStageRetryRequestedEvent(stage.getId(), iterationId, projectId, userId, comment));

    return PipelineStageRetryRS.builder()
        .stageId(stage.getId())
        .status(stage.getStatus())
        .triggeredRunUrl(triggeredRunUrl)
        .build();
  }

  private PipelineStage findStageOrThrow(Long projectId, Long iterationId, Long stageId) {
    return pipelineStageRepository.findById(stageId)
        .filter(s -> s.getIteration().getId().equals(iterationId))
        .filter(s -> s.getIteration().getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline stage '" + stageId + "'"));
  }

  private Integration resolveIntegration(Project project, CiProvider provider) {
    var typeName = INTEGRATION_TYPE_NAME_BY_PROVIDER.get(provider);
    return integrationRepository.findAllProjectByGroup(project, IntegrationGroupEnum.AUTOMATION).stream()
        .filter(i -> i.isEnabled() && i.getType() != null && typeName.equalsIgnoreCase(i.getType().getName()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "No enabled '" + typeName + "' automation integration configured for this project"));
  }

  private record ResolvedStageForRetry(PipelineStage stage, Integration integration, CiTriggerConnector connector) {}
}
