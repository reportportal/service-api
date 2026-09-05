package com.epam.reportportal.base.core.tms.sync;

import com.epam.reportportal.base.core.tms.enums.TmsSyncDirection;
import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.enums.TmsSyncStatus;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteFolder;
import com.epam.reportportal.base.core.tms.sync.dto.TmsSyncJobRS;
import com.epam.reportportal.base.core.tms.sync.mapper.TmsSyncJobMapper;
import com.epam.reportportal.base.core.tms.sync.service.TmsFolderSyncService;
import com.epam.reportportal.base.core.tms.sync.service.TmsTestCaseSyncService;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncError;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncScopeConfig;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.core.tms.sync.event.TmsSyncJobScheduledEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class TmsSyncJobServiceImpl implements TmsSyncJobService {

  private static final int BATCH_SIZE = 50;

  private final TmsSyncJobRepository tmsSyncJobRepository;
  private final IntegrationRepository integrationRepository;
  private final List<TmsSyncConnector<Integration>> connectors;
  private final TmsSyncJobMapper tmsSyncJobMapper;
  private final TmsFolderSyncService tmsFolderSyncService;
  private final TmsTestCaseSyncService tmsTestCaseSyncService;
  private final TransactionTemplate transactionTemplate;
  private final ApplicationEventPublisher eventPublisher;

  public TmsSyncJobServiceImpl(
      TmsSyncJobRepository tmsSyncJobRepository,
      IntegrationRepository integrationRepository,
      List<TmsSyncConnector<Integration>> connectors,
      TmsSyncJobMapper tmsSyncJobMapper,
      TmsFolderSyncService tmsFolderSyncService,
      TmsTestCaseSyncService tmsTestCaseSyncService,
      PlatformTransactionManager transactionManager,
      ApplicationEventPublisher eventPublisher) {
    this.tmsSyncJobRepository = tmsSyncJobRepository;
    this.integrationRepository = integrationRepository;
    this.connectors = connectors;
    this.tmsSyncJobMapper = tmsSyncJobMapper;
    this.tmsFolderSyncService = tmsFolderSyncService;
    this.tmsTestCaseSyncService = tmsTestCaseSyncService;
    this.eventPublisher = eventPublisher;

    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Override
  @Transactional
  public TmsSyncJobRS startSyncJob(Long projectId, Long integrationId, String remoteFolderId,
      Long localFolderId) {
    var integration = integrationRepository.findByIdAndProjectIdForUpdate(integrationId, projectId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

    var provider = parseProvider(integration.getType().getName());

    var isRunning = tmsSyncJobRepository.existsByProjectIdAndIntegrationIdAndStatusIn(
        projectId, integrationId, List.of(TmsSyncStatus.PENDING, TmsSyncStatus.IN_PROGRESS));
    if (isRunning) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "A sync job is already running for this integration.");
    }

    var project = new Project();
    project.setId(projectId);

    var job = new TmsSyncJob();
    job.setProject(project);
    job.setIntegration(integration);
    job.setProvider(provider);
    job.setDirection(TmsSyncDirection.IMPORT);
    job.setStatus(TmsSyncStatus.PENDING);
    job.setCreatedAt(Instant.now());

    var scopeConfig = new SyncScopeConfig();
    scopeConfig.setRemoteFolderId(remoteFolderId);
    scopeConfig.setLocalFolderId(localFolderId);
    job.setScopeConfig(scopeConfig);

    job = tmsSyncJobRepository.save(job);
    final Long savedJobId = job.getId();

    eventPublisher.publishEvent(new TmsSyncJobScheduledEvent(this, savedJobId));

    return tmsSyncJobMapper.toTmsSyncJobRS(job);
  }


  @Override
  @Transactional(readOnly = true)
  public Page<TmsSyncJobRS> getSyncJobs(Long projectId, Pageable pageable) {
    return tmsSyncJobRepository.findByProjectId(projectId, pageable)
        .map(tmsSyncJobMapper::toTmsSyncJobRS);
  }

  @Override
  @Transactional(readOnly = true)
  public TmsSyncJobRS getSyncJob(Long projectId, Long jobId) {
    return tmsSyncJobRepository.findById(jobId)
        .filter(job -> job.getProject().getId().equals(projectId))
        .map(tmsSyncJobMapper::toTmsSyncJobRS)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.NOT_FOUND, "Sync Job not found: " + jobId));
  }

  @Override
  @Transactional
  public void cancelSyncJob(Long projectId, Long jobId) {
    var job = tmsSyncJobRepository.findById(jobId)
        .filter(j -> j.getProject().getId().equals(projectId))
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.NOT_FOUND, "Sync Job not found: " + jobId));

    if (job.getStatus() == TmsSyncStatus.PENDING || job.getStatus() == TmsSyncStatus.IN_PROGRESS) {
      job.setStatus(TmsSyncStatus.CANCELED);
      job.setCompletedAt(Instant.now());
      tmsSyncJobRepository.save(job);
    } else {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Cannot cancel a job in status: " + job.getStatus());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<RemoteFolder> getRemoteFolders(Long projectId, Long integrationId, String provider,
      String rootFolderId) {
    var integration = integrationRepository.findByIdAndProjectId(integrationId, projectId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

    var syncProvider = parseProvider(provider);
    var connector = connectors.stream()
        .filter(c -> c.getSupportedProvider() == syncProvider)
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "No connector found for provider: " + provider));

    return connector.fetchFolderTree(integration, rootFolderId);
  }

  @Override
  public void executeSync(Long jobId) {
    log.info("Starting TMS Sync Job: {}", jobId);

    var executingJob = transactionTemplate.execute(status -> {
      var job = tmsSyncJobRepository
          .findByIdWithIntegration(jobId)
          .orElse(null);
      if (job != null && job.getStatus() == TmsSyncStatus.PENDING) {
        job.setStatus(TmsSyncStatus.IN_PROGRESS);
        job.setStartedAt(Instant.now());
        job.setCounters(new SyncCounters(0, 0, 0));
        if (job.getErrorLog() == null) {
          job.setErrorLog(new SyncErrorLog(new ArrayList<>()));
        }
        return tmsSyncJobRepository.save(job);
      }
      return null;
    });

    if (executingJob == null) {
      log.warn("Job {} is not in PENDING state or does not exist.", jobId);
      return;
    }

    try {
      if (isJobCanceled(jobId)) {
        log.info("TMS Sync Job {} was cancelled before execution started.", jobId);
        return;
      }

      var connector = getConnector(executingJob);
      var integration = executingJob.getIntegration();
      connector.validateConfig(integration);

      var remoteRootFolderId = executingJob.getScopeConfig().getRemoteFolderId();
      var localRootFolderId = executingJob.getScopeConfig().getLocalFolderId();

      // 1. Sync Folders
      var remoteFolders = connector.fetchFolderTree(integration, remoteRootFolderId);
      var folderIdMap = tmsFolderSyncService.syncFolders(executingJob, remoteFolders, localRootFolderId);

      // 2. Sync Test Cases
      var since = transactionTemplate.execute(status ->
          tmsSyncJobRepository.findFirstByProjectIdAndIntegrationIdAndStatusAndRemoteFolderIdOrderByCompletedAtDesc(
                  executingJob.getProject().getId(),
                  integration.getId(),
                  TmsSyncStatus.SUCCESS,
                  remoteRootFolderId
              ).map(TmsSyncJob::getCompletedAt)
              .orElseGet(() -> executingJob.getProject().getCreationDate())
      );

      for (var remoteFolder : remoteFolders) {
        if (isJobCanceled(jobId)) {
          log.info("TMS Sync Job {} was cancelled. Terminating execution.", jobId);
          return;
        }

        var localFolderId = folderIdMap.get(remoteFolder.getId());
        var offset = 0;
        boolean hasMore;

        do {
          if (isJobCanceled(jobId)) {
            log.info("TMS Sync Job {} was cancelled. Terminating execution.", jobId);
            return;
          }

          var remoteTestCases = connector.fetchTestCases(
              integration, remoteFolder, since, offset, BATCH_SIZE
          );

          if (isJobCanceled(jobId)) {
            log.info("TMS Sync Job {} was cancelled after fetching folder {}.", jobId, remoteFolder.getId());
            return;
          }

          var remoteTestCasesBatch = remoteTestCases.getTestCases();
          if (CollectionUtils.isNotEmpty(remoteTestCasesBatch)) {
            updateJobTotalCounter(jobId, remoteTestCasesBatch.size());
            tmsTestCaseSyncService.processTestCaseBatch(
                jobId,
                executingJob.getProject().getId(),
                connector,
                integration,
                remoteTestCasesBatch,
                localFolderId
            );
          }
          offset += BATCH_SIZE;

          hasMore = remoteTestCases.isHasMore() && CollectionUtils.isNotEmpty(remoteTestCasesBatch);
        } while (hasMore);
      }

      transactionTemplate.execute(status -> {
        var job = tmsSyncJobRepository.findById(jobId).orElseThrow();
        if (job.getStatus() == TmsSyncStatus.IN_PROGRESS) {
          job.setStatus(TmsSyncStatus.SUCCESS);
          job.setCompletedAt(Instant.now());
          tmsSyncJobRepository.save(job);
        }
        return null;
      });
    } catch (Exception e) {
      log.error("TMS Sync Job {} failed", jobId, e);
      transactionTemplate.execute(status -> {
        tmsSyncJobRepository
            .findById(jobId)
            .ifPresent(job -> {
                if (job.getStatus() != TmsSyncStatus.CANCELED) {
                  job.setStatus(TmsSyncStatus.FAILED);
                  job.setCompletedAt(Instant.now());
                  if (job.getErrorLog() == null) {
                    job.setErrorLog(new SyncErrorLog(new ArrayList<>()));
                  }
                  job.getErrorLog().getErrors()
                      .add(new SyncError("JOB_LEVEL", e.getMessage(), ExceptionUtils.getStackTrace(e)));
                  tmsSyncJobRepository.save(job);
                }
            });
        return null;
      });
    }
  }

  private void updateJobTotalCounter(Long jobId, int totalCount) {
    if (totalCount <= 0) {
      return;
    }
    transactionTemplate.execute(status -> {
      var job = tmsSyncJobRepository.findById(jobId).orElseThrow();
      if (job.getStatus() == TmsSyncStatus.IN_PROGRESS) {
        job.getCounters().setTotal(job.getCounters().getTotal() + totalCount);
        return tmsSyncJobRepository.save(job);
      }
      return job;
    });
  }

  private boolean isJobCanceled(Long jobId) {
    return tmsSyncJobRepository.findById(jobId)
        .map(job -> job.getStatus() == TmsSyncStatus.CANCELED)
        .orElse(true);
  }

  private TmsSyncProvider parseProvider(String providerName) {
    if (StringUtils.isBlank(providerName)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "TMS Provider name cannot be empty");
    }
    try {
      return TmsSyncProvider.valueOf(providerName.toUpperCase().replace("-", "_"));
    } catch (IllegalArgumentException e) {
      throw new ReportPortalException(
          ErrorType.BAD_REQUEST_ERROR, "Unsupported TMS provider: " + providerName);
    }
  }

  private TmsSyncConnector<Integration> getConnector(TmsSyncJob job) {
    return connectors
        .stream()
        .filter(c -> c.getSupportedProvider() == job.getProvider())
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR, "No connector found for provider: " + job.getProvider()));
  }
}