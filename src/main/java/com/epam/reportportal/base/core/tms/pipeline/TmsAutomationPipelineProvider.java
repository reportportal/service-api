package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineProvisioner;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Owns the "Test automation" pipeline of a project, so that it exists before the first Automate and an administrator
 * can set up its CI connection like for any other pipeline.
 * <p>
 * Example: a project that already has the "Test case generation" pipeline gets "Test automation" the first time its
 * pipeline list is read; {@code getOrCreate} is what Automate uses and creates it when it is still missing.
 * The pipeline is created in its own transaction, so it stays even when the Automate that asked for it is refused
 * (for instance with {@code CI_NOT_CONNECTED}).
 */
@Slf4j
@Service
public class TmsAutomationPipelineProvider implements PipelineProvisioner {

  private final PipelineRepository pipelineRepository;
  private final ProjectRepository projectRepository;
  private final TransactionTemplate newTransaction;

  /**
   * Creates the provider.
   */
  public TmsAutomationPipelineProvider(PipelineRepository pipelineRepository, ProjectRepository projectRepository,
      PlatformTransactionManager transactionManager) {
    this.pipelineRepository = pipelineRepository;
    this.projectRepository = projectRepository;
    this.newTransaction = new TransactionTemplate(transactionManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Makes sure a project that already uses pipelines has its Test automation pipeline. A project without any pipeline
   * is left alone. Never fails the caller: the pipeline list must stay readable.
   */
  @Override
  public void ensureForProject(long projectId) {
    try {
      if (pipelineRepository.existsByProjectId(projectId)) {
        getOrCreate(projectId);
      }
    } catch (RuntimeException e) {
      log.warn("Could not create the Test automation pipeline of project {}: {}", projectId, e.getMessage());
    }
  }

  /**
   * The Test automation pipeline of the project, created when missing.
   */
  public Pipeline getOrCreate(long projectId) {
    return find(projectId).orElseGet(() -> {
      try {
        newTransaction.executeWithoutResult(status -> create(projectId));
      } catch (DataIntegrityViolationException e) {
        log.debug("Test automation pipeline of project {} was created concurrently", projectId);
      }
      return find(projectId).orElseThrow();
    });
  }

  private Optional<Pipeline> find(long projectId) {
    return pipelineRepository.findFirstByProjectIdAndTypeOrderByIdAsc(projectId,
        TmsAutomationStartService.PIPELINE_TYPE);
  }

  private void create(long projectId) {
    var pipeline = new Pipeline();
    pipeline.setProject(projectRepository.getReferenceById(projectId));
    pipeline.setName(TmsAutomationStartService.PIPELINE_NAME);
    pipeline.setType(TmsAutomationStartService.PIPELINE_TYPE);
    pipelineRepository.saveAndFlush(pipeline);
  }
}
