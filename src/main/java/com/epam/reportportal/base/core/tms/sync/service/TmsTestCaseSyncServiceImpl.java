package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsAttachmentMapper;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseActivityResourceMapper;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseMapper;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseAttributeService;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteAttachment;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncError;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class TmsTestCaseSyncServiceImpl implements TmsTestCaseSyncService {

  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseAttributeService tmsTestCaseAttributeService;
  private final TmsAttachmentDataStoreService tmsAttachmentDataStoreService;
  private final TmsAttachmentRepository tmsAttachmentRepository;
  private final TmsSyncJobRepository tmsSyncJobRepository;
  private final TmsTestCaseMapper tmsTestCaseMapper;
  private final TmsAttachmentMapper tmsAttachmentMapper;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;
  private final ProjectRepository projectRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TmsTestCaseActivityResourceMapper tmsTestCaseActivityResourceMapper;
  private final TransactionTemplate transactionTemplate;

  public TmsTestCaseSyncServiceImpl(
      TmsTestCaseRepository tmsTestCaseRepository,
      TmsTestCaseVersionService tmsTestCaseVersionService,
      TmsTestCaseAttributeService tmsTestCaseAttributeService,
      TmsAttachmentDataStoreService tmsAttachmentDataStoreService,
      TmsAttachmentRepository tmsAttachmentRepository,
      TmsSyncJobRepository tmsSyncJobRepository,
      TmsTestCaseMapper tmsTestCaseMapper,
      TmsAttachmentMapper tmsAttachmentMapper,
      TmsManualScenarioMapper tmsManualScenarioMapper,
      ProjectRepository projectRepository,
      ApplicationEventPublisher eventPublisher,
      TmsTestCaseActivityResourceMapper tmsTestCaseActivityResourceMapper,
      PlatformTransactionManager transactionManager) {
    this.tmsTestCaseRepository = tmsTestCaseRepository;
    this.tmsTestCaseVersionService = tmsTestCaseVersionService;
    this.tmsTestCaseAttributeService = tmsTestCaseAttributeService;
    this.tmsAttachmentDataStoreService = tmsAttachmentDataStoreService;
    this.tmsAttachmentRepository = tmsAttachmentRepository;
    this.tmsSyncJobRepository = tmsSyncJobRepository;
    this.tmsTestCaseMapper = tmsTestCaseMapper;
    this.tmsAttachmentMapper = tmsAttachmentMapper;
    this.tmsManualScenarioMapper = tmsManualScenarioMapper;
    this.projectRepository = projectRepository;
    this.eventPublisher = eventPublisher;
    this.tmsTestCaseActivityResourceMapper = tmsTestCaseActivityResourceMapper;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Override
  public void processTestCaseBatch(Long jobId, 
      Long projectId,
      TmsSyncConnector<Integration> connector, 
      Integration integration, 
      List<RemoteTestCase> remoteTestCaseBatch,
      Long localFolderId) {
    var externalIds = remoteTestCaseBatch
        .stream()
        .map(RemoteTestCase::getId)
        .toList();

    var organizationId = projectRepository.findById(projectId)
        .map(Project::getOrganizationId)
        .orElse(null);

    // 1. Fetch existing in one query
    var existingTestCases = transactionTemplate.execute(status ->
        tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, externalIds)
            .stream()
            .collect(
                Collectors.toMap(TmsTestCase::getExternalId, Function.identity())
            )
    );

    var beforeSnapshots = new HashMap<String, TestCaseActivityResource>();
    if (existingTestCases != null && !existingTestCases.isEmpty()) {
      transactionTemplate.executeWithoutResult(status -> {
        var existingTestCaseIds = existingTestCases.values().stream().map(TmsTestCase::getId).toList();
        var defaultVersionsBefore = tmsTestCaseVersionService.getDefaultVersions(existingTestCaseIds);
        for (var entry : existingTestCases.entrySet()) {
          var tc = entry.getValue();
          var ver = defaultVersionsBefore != null ? defaultVersionsBefore.get(tc.getId()) : null;
          beforeSnapshots.put(entry.getKey(), tmsTestCaseActivityResourceMapper.buildActivityResource(tc, ver));
        }
      });
    }

    var testCaseSyncContext = new TestCaseSyncContext(
        projectId, connector, integration, localFolderId
    );
    var testCasesToSave = new ArrayList<TmsTestCase>();
    var isNewTestCaseMap = new HashMap<String, Boolean>();
    var attachmentsByTestCaseId = new HashMap<String, List<TmsAttachment>>();
    var processedCount = 0;
    var failedCount = 0;
    var errors = new ArrayList<SyncError>();

    for (var remoteTestCase : remoteTestCaseBatch) {
      try {
        var existingTestCase = existingTestCases != null ? 
            existingTestCases.get(remoteTestCase.getId()) : null;

        var testCaseSyncResult = processTestCase(
            testCaseSyncContext, remoteTestCase, existingTestCase, errors
        );
        if (testCaseSyncResult.processed()) {
          processedCount++;
          testCasesToSave.add(testCaseSyncResult.testCase());
          isNewTestCaseMap.put(remoteTestCase.getId(), testCaseSyncResult.isNew());
          if (CollectionUtils.isNotEmpty(testCaseSyncResult.attachments())) {
            attachmentsByTestCaseId.put(
                remoteTestCase.getId(), 
                testCaseSyncResult.attachments()
            );
          }
        }
      } catch (Exception e) {
        log.error("Failed to sync test case: {}", remoteTestCase.getId(), e);
        failedCount++;
        errors.add(
            new SyncError(remoteTestCase.getId(), e.getMessage(), ExceptionUtils.getStackTrace(e)));
      }
    }

    // 3. Save Batch (Short DB Write Transaction)
    final var finalProcessedCount = processedCount;
    final var finalFailedCount = failedCount;
    transactionTemplate.execute(status -> {
      var allAttachments = attachmentsByTestCaseId
          .values()
          .stream()
          .flatMap(List::stream)
          .toList();
      if (!allAttachments.isEmpty()) {
        tmsAttachmentRepository.saveAll(allAttachments);
      }

      if (!testCasesToSave.isEmpty()) {
        var savedTestCases = tmsTestCaseRepository.saveAll(testCasesToSave);
        var savedTestCasesByExternalId = savedTestCases
            .stream()
            .collect(Collectors.toMap(
                TmsTestCase::getExternalId, 
                Function.identity(), 
                (tc1, tc2) -> tc1)
            );

        for (var remoteTestCase : remoteTestCaseBatch) {
          var isTestCaseNew = isNewTestCaseMap.get(remoteTestCase.getId());
          if (isTestCaseNew != null) {
            var tmsTestCase = savedTestCasesByExternalId.get(remoteTestCase.getId());
            if (tmsTestCase != null) {
              if (CollectionUtils.isNotEmpty(remoteTestCase.getLabels())) {
                var attributeRQs = remoteTestCase.getLabels().stream()
                    .map(label -> TmsTestCaseAttributeRQ.builder().key(label).build())
                    .toList();
                if (CollectionUtils.isNotEmpty(attributeRQs)) {
                  tmsTestCaseAttributeService.createTestCaseAttributes(projectId, tmsTestCase, attributeRQs);
                }
              }
              var attachmentIds = attachmentsByTestCaseId
                  .getOrDefault(remoteTestCase.getId(), List.of())
                  .stream()
                  .map(TmsAttachment::getId)
                  .toList();
              var manualScenarioRQ = tmsManualScenarioMapper.convertFromRemote(remoteTestCase, attachmentIds);
              TmsTestCaseVersion version;
              if (isTestCaseNew) {
                version = tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, tmsTestCase, manualScenarioRQ);
              } else {
                version = tmsTestCaseVersionService.updateDefaultTestCaseVersion(projectId, tmsTestCase, manualScenarioRQ);
              }

              var afterSnapshot = tmsTestCaseActivityResourceMapper.buildActivityResource(tmsTestCase, version);
              if (isTestCaseNew) {
                var importedEvent = tmsTestCaseActivityResourceMapper.buildTestCaseImportedEvent(
                    null, "System", organizationId, afterSnapshot
                );
                eventPublisher.publishEvent(importedEvent);
              } else {
                var beforeSnapshot = beforeSnapshots.get(remoteTestCase.getId());
                if (beforeSnapshot != null) {
                  var fieldChangedEvents = tmsTestCaseActivityResourceMapper.buildTestCaseFieldChangedEvents(
                      null, "System", organizationId, beforeSnapshot, afterSnapshot
                  );
                  fieldChangedEvents.forEach(eventPublisher::publishEvent);
                }
              }
            }
          }
        }
      }

      var job = tmsSyncJobRepository.findById(jobId).orElseThrow();
      job.getCounters().setProcessed(job.getCounters().getProcessed() + finalProcessedCount);
      job.getCounters().setFailed(job.getCounters().getFailed() + finalFailedCount);
      if (!errors.isEmpty()) {
        job.getErrorLog().getErrors().addAll(errors);
      }
      return tmsSyncJobRepository.save(job);
    });
  }

  private TestCaseSyncResult processTestCase(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      TmsTestCase existing,
      List<SyncError> errors) {

    var needsUpdate = existing == null || existing.getSourceUpdatedAt() == null
        || remoteTestCase.getUpdatedAt().isAfter(existing.getSourceUpdatedAt());

    if (!needsUpdate) {
      return new TestCaseSyncResult(false, false, null, List.of());
    }

    var attachments = processAttachments(context, remoteTestCase, errors);
    var isNew = (existing == null);
    var testCase = tmsTestCaseMapper.convertFromRemote(
        remoteTestCase, existing, context.projectId(), context.localFolderId());

    return new TestCaseSyncResult(true, isNew, testCase, attachments);
  }

  private List<TmsAttachment> processAttachments(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      List<SyncError> errors) {

    var testCaseAttachments = new ArrayList<TmsAttachment>();
    if (remoteTestCase.getAttachments() != null) {
      for (var remoteAttachment : remoteTestCase.getAttachments()) {
        var attachment = processAttachment(context, remoteTestCase, remoteAttachment, errors);
        if (attachment != null) {
          testCaseAttachments.add(attachment);
        }
      }
    }
    return testCaseAttachments;
  }

  private TmsAttachment processAttachment(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      RemoteAttachment remoteAttachment,
      List<SyncError> errors) {

    try (var inputStream = context.connector().downloadAttachment(
        context.integration(), remoteAttachment.getContentUrl())) {
      var sanitizedFilename = remoteAttachment.getFilename()
          .replaceAll("[^a-zA-Z0-9.-]", "_");
      var shardedPath = String.format("tms/%d/%s/%s_%s",
          context.projectId(), remoteTestCase.getId(), remoteAttachment.getId(), sanitizedFilename);
      var bytes = inputStream.readAllBytes();
      var fileId = tmsAttachmentDataStoreService.save(shardedPath, new ByteArrayInputStream(bytes));
      String thumbnailId = null;
      if (isImage(remoteAttachment.getMimeType())) {
        try {
          var thumbnailShardedPath = String.format("tms/%d/%s/thumbnail_%s_%s",
              context.projectId(), remoteTestCase.getId(), remoteAttachment.getId(), sanitizedFilename);
          thumbnailId = tmsAttachmentDataStoreService.saveThumbnail(
              thumbnailShardedPath, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
          log.warn("Failed to create thumbnail for attachment {} in test case {}",
              remoteAttachment.getId(), remoteTestCase.getId(), e);
        }
      }
      return tmsAttachmentMapper.convertFromRemote(remoteAttachment, fileId, thumbnailId);
    } catch (Exception e) {
      log.warn("Failed to sync attachment {} for test case {}", remoteAttachment.getId(),
          remoteTestCase.getId(), e);
      errors.add(new SyncError(remoteAttachment.getId(),
          "Attachment sync failed: " + e.getMessage(), null));
      return null;
    }
  }

  private boolean isImage(String contentType) {
    return contentType != null && (contentType.equalsIgnoreCase("image/jpeg")
        || contentType.equalsIgnoreCase("image/png")
        || contentType.equalsIgnoreCase("image/jpg"));
  }

  private record TestCaseSyncContext(
      Long projectId,
      TmsSyncConnector<Integration> connector,
      Integration integration,
      Long localFolderId
  ) {}

  private record TestCaseSyncResult(
      boolean processed,
      boolean isNew,
      TmsTestCase testCase,
      List<TmsAttachment> attachments
  ) {}
}
