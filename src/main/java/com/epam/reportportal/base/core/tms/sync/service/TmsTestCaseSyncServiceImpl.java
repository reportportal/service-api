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
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncError;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  private static final int MAX_ATTACHMENT_SIZE = 50 * 1024 * 1024;

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
    var organizationId = projectRepository.findById(projectId)
        .map(Project::getOrganizationId)
        .orElse(null);
    var existingTestCases = loadExistingTestCasesAndBeforeSnapshots(projectId, remoteTestCaseBatch);
    var testCaseSyncContext = new TestCaseSyncContext(
        projectId, connector, integration, localFolderId
    );
    var batchProcessingResult = processRemoteTestCaseBatch(
        remoteTestCaseBatch, existingTestCases.testCases(), testCaseSyncContext
    );

    transactionTemplate.execute(status -> {
      persistTestCasesAndAttachments(
          projectId,
          organizationId,
          remoteTestCaseBatch,
          existingTestCases.beforeSnapshots(),
          batchProcessingResult
      );
      return updateSyncJob(jobId, batchProcessingResult);
    });
  }

  private ExistingTestCases loadExistingTestCasesAndBeforeSnapshots(
      Long projectId,
      List<RemoteTestCase> remoteTestCaseBatch) {
    var testCases = new HashMap<String, TmsTestCase>();
    var beforeSnapshots = new HashMap<String, TestCaseActivityResource>();
    var externalIds = remoteTestCaseBatch.stream().map(RemoteTestCase::getId).toList();

    transactionTemplate.executeWithoutResult(status -> {
      var foundTestCases = tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, externalIds)
          .stream()
          .collect(Collectors.toMap(TmsTestCase::getExternalId, Function.identity()));
      testCases.putAll(foundTestCases);

      if (!foundTestCases.isEmpty()) {
        var existingTestCaseIds = foundTestCases.values().stream().map(TmsTestCase::getId).toList();
        var defaultVersionsBefore = tmsTestCaseVersionService.getDefaultVersions(existingTestCaseIds);
        for (var entry : foundTestCases.entrySet()) {
          var testCase = entry.getValue();
          var version = defaultVersionsBefore != null ? defaultVersionsBefore.get(testCase.getId()) : null;
          beforeSnapshots.put(
              entry.getKey(),
              tmsTestCaseActivityResourceMapper.buildActivityResource(testCase, version)
          );
        }
      }
    });
    return new ExistingTestCases(testCases, beforeSnapshots);
  }

  private BatchProcessingResult processRemoteTestCaseBatch(
      List<RemoteTestCase> remoteTestCaseBatch,
      Map<String, TmsTestCase> existingTestCases,
      TestCaseSyncContext context) {
    var testCasesToSave = new ArrayList<TmsTestCase>();
    var isNewTestCaseMap = new HashMap<String, Boolean>();
    var attachmentsByTestCaseId = new HashMap<String, List<TmsAttachment>>();
    var errors = new ArrayList<SyncError>();
    var processedCount = 0;
    var failedCount = 0;

    for (var remoteTestCase : remoteTestCaseBatch) {
      try {
        var testCaseSyncResult = processTestCase(
            context, remoteTestCase, existingTestCases.get(remoteTestCase.getId()), errors
        );
        if (testCaseSyncResult.processed()) {
          processedCount++;
          testCasesToSave.add(testCaseSyncResult.testCase());
          isNewTestCaseMap.put(remoteTestCase.getId(), testCaseSyncResult.isNew());
          if (CollectionUtils.isNotEmpty(testCaseSyncResult.attachments())) {
            attachmentsByTestCaseId.put(remoteTestCase.getId(), testCaseSyncResult.attachments());
          }
        }
      } catch (Exception e) {
        log.error("Failed to sync test case: {}", remoteTestCase.getId(), e);
        failedCount++;
        errors.add(
            new SyncError(remoteTestCase.getId(), e.getMessage(), ExceptionUtils.getStackTrace(e)));
      }
    }
    return new BatchProcessingResult(
        testCasesToSave,
        isNewTestCaseMap,
        attachmentsByTestCaseId,
        processedCount,
        failedCount,
        errors
    );
  }

  private void persistTestCasesAndAttachments(
      Long projectId,
      Long organizationId,
      List<RemoteTestCase> remoteTestCaseBatch,
      Map<String, TestCaseActivityResource> beforeSnapshots,
      BatchProcessingResult batchProcessingResult) {
    var allAttachments = batchProcessingResult.attachmentsByTestCaseId()
        .values()
        .stream()
        .flatMap(List::stream)
        .toList();
    if (!allAttachments.isEmpty()) {
      tmsAttachmentRepository.saveAll(allAttachments);
    }

    if (batchProcessingResult.testCasesToSave().isEmpty()) {
      return;
    }

    var savedTestCasesByExternalId = tmsTestCaseRepository.saveAll(batchProcessingResult.testCasesToSave())
        .stream()
        .collect(Collectors.toMap(
            TmsTestCase::getExternalId,
            Function.identity(),
            (tc1, tc2) -> tc1)
        );
    for (var remoteTestCase : remoteTestCaseBatch) {
      createOrUpdateScenarioAndActivity(
          projectId,
          organizationId,
          remoteTestCase,
          savedTestCasesByExternalId,
          beforeSnapshots,
          batchProcessingResult
      );
    }
  }

  private void createOrUpdateScenarioAndActivity(
      Long projectId,
      Long organizationId,
      RemoteTestCase remoteTestCase,
      Map<String, TmsTestCase> savedTestCasesByExternalId,
      Map<String, TestCaseActivityResource> beforeSnapshots,
      BatchProcessingResult batchProcessingResult) {
    var isTestCaseNew = batchProcessingResult.isNewTestCaseMap().get(remoteTestCase.getId());
    if (isTestCaseNew == null) {
      return;
    }

    var tmsTestCase = savedTestCasesByExternalId.get(remoteTestCase.getId());
    if (tmsTestCase == null) {
      return;
    }

    createTestCaseAttributes(projectId, tmsTestCase, remoteTestCase);
    var attachmentIds = batchProcessingResult.attachmentsByTestCaseId()
        .getOrDefault(remoteTestCase.getId(), List.of())
        .stream()
        .map(TmsAttachment::getId)
        .toList();
    var manualScenarioRQ = tmsManualScenarioMapper.convertFromRemote(remoteTestCase, attachmentIds);
    var version = isTestCaseNew
        ? tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, tmsTestCase, manualScenarioRQ)
        : tmsTestCaseVersionService.updateDefaultTestCaseVersion(projectId, tmsTestCase, manualScenarioRQ);
    publishTestCaseActivityEvent(
        organizationId,
        remoteTestCase.getId(),
        isTestCaseNew,
        beforeSnapshots.get(remoteTestCase.getId()),
        tmsTestCase,
        version
    );
  }

  private void createTestCaseAttributes(
      Long projectId,
      TmsTestCase tmsTestCase,
      RemoteTestCase remoteTestCase) {
    if (CollectionUtils.isNotEmpty(remoteTestCase.getLabels())) {
      var attributeRQs = remoteTestCase.getLabels().stream()
          .map(label -> TmsTestCaseAttributeRQ.builder().key(label).build())
          .toList();
      if (CollectionUtils.isNotEmpty(attributeRQs)) {
        tmsTestCaseAttributeService.createTestCaseAttributes(projectId, tmsTestCase, attributeRQs);
      }
    }
  }

  private void publishTestCaseActivityEvent(
      Long organizationId,
      String externalId,
      boolean isTestCaseNew,
      TestCaseActivityResource beforeSnapshot,
      TmsTestCase tmsTestCase,
      TmsTestCaseVersion version) {
    var afterSnapshot = tmsTestCaseActivityResourceMapper.buildActivityResource(tmsTestCase, version);
    if (isTestCaseNew) {
      var importedEvent = tmsTestCaseActivityResourceMapper.buildTestCaseImportedEvent(
          null, "System", organizationId, afterSnapshot
      );
      eventPublisher.publishEvent(importedEvent);
    } else if (beforeSnapshot != null) {
      var fieldChangedEvents = tmsTestCaseActivityResourceMapper.buildTestCaseFieldChangedEvents(
          null, "System", organizationId, beforeSnapshot, afterSnapshot
      );
      fieldChangedEvents.forEach(eventPublisher::publishEvent);
    }
  }

  private TmsSyncJob updateSyncJob(Long jobId, BatchProcessingResult batchProcessingResult) {
    var job = tmsSyncJobRepository.findById(jobId).orElseThrow();
    job.getCounters().setProcessed(
        job.getCounters().getProcessed() + batchProcessingResult.processedCount());
    job.getCounters().setFailed(job.getCounters().getFailed() + batchProcessingResult.failedCount());
    if (!batchProcessingResult.errors().isEmpty()) {
      job.getErrorLog().getErrors().addAll(batchProcessingResult.errors());
    }
    return tmsSyncJobRepository.save(job);
  }

  private TestCaseSyncResult processTestCase(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      TmsTestCase existing,
      List<SyncError> errors) {

    var needsUpdate = existing == null || existing.getSourceUpdatedAt() == null
        || remoteTestCase.getUpdatedAt() == null
        || remoteTestCase.getUpdatedAt().isAfter(existing.getSourceUpdatedAt());

    if (!needsUpdate) {
      return new TestCaseSyncResult(false, false, null, List.of());
    }

    var attachmentSyncResult = processAttachments(context, remoteTestCase, errors);
    if (!attachmentSyncResult.allAttachmentsDownloaded()) {
      throw new IllegalStateException("Failed to download all attachments for test case "
          + remoteTestCase.getId());
    }

    var isNew = (existing == null);
    var testCase = tmsTestCaseMapper.convertFromRemote(
        remoteTestCase, existing, context.projectId(), context.localFolderId());

    return new TestCaseSyncResult(true, isNew, testCase, attachmentSyncResult.attachments());
  }

  private AttachmentSyncResult processAttachments(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      List<SyncError> errors) {

    var testCaseAttachments = new ArrayList<TmsAttachment>();
    var allAttachmentsDownloaded = true;
    if (remoteTestCase.getAttachments() != null) {
      for (var remoteAttachment : remoteTestCase.getAttachments()) {
        var attachment = processAttachment(context, remoteTestCase, remoteAttachment, errors);
        if (attachment != null) {
          testCaseAttachments.add(attachment);
        } else {
          allAttachmentsDownloaded = false;
        }
      }
    }
    return new AttachmentSyncResult(testCaseAttachments, allAttachmentsDownloaded);
  }

  private TmsAttachment processAttachment(
      TestCaseSyncContext context,
      RemoteTestCase remoteTestCase,
      RemoteAttachment remoteAttachment,
      List<SyncError> errors) {

    try {
      if (remoteAttachment.getSize() != null
          && remoteAttachment.getSize() > MAX_ATTACHMENT_SIZE) {
        throw new IOException("Attachment size exceeds the maximum allowed size of "
            + MAX_ATTACHMENT_SIZE + " bytes");
      }

      try (var inputStream = context.connector().downloadAttachment(
          context.integration(), remoteAttachment.getContentUrl())) {
      var sanitizedFilename = remoteAttachment.getFilename()
          .replaceAll("[^a-zA-Z0-9.-]", "_");
      var shardedPath = String.format("tms/%d/%s/%s_%s",
          context.projectId(), remoteTestCase.getId(), remoteAttachment.getId(), sanitizedFilename);
      var bytes = readAttachmentBytes(inputStream);
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
      return tmsAttachmentMapper.convertFromRemote(remoteAttachment, fileId, thumbnailId, context.projectId());
      }
    } catch (Exception e) {
      log.warn("Failed to sync attachment {} for test case {}", remoteAttachment.getId(),
          remoteTestCase.getId(), e);
      errors.add(new SyncError(remoteAttachment.getId(),
          "Attachment sync failed: " + e.getMessage(), null));
      return null;
    }
  }

  private byte[] readAttachmentBytes(InputStream inputStream) throws IOException {
    try (var outputStream = new ByteArrayOutputStream()) {
      var buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        if (outputStream.size() > MAX_ATTACHMENT_SIZE - bytesRead) {
          throw new IOException("Attachment size exceeds the maximum allowed size of "
              + MAX_ATTACHMENT_SIZE + " bytes");
        }
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    }
  }

  private boolean isImage(String contentType) {
    return contentType != null && (contentType.equalsIgnoreCase("image/jpeg")
        || contentType.equalsIgnoreCase("image/png")
        || contentType.equalsIgnoreCase("image/jpg"));
  }

  private record ExistingTestCases(
      Map<String, TmsTestCase> testCases,
      Map<String, TestCaseActivityResource> beforeSnapshots
  ) {}

  private record BatchProcessingResult(
      List<TmsTestCase> testCasesToSave,
      Map<String, Boolean> isNewTestCaseMap,
      Map<String, List<TmsAttachment>> attachmentsByTestCaseId,
      int processedCount,
      int failedCount,
      List<SyncError> errors
  ) {}

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

  private record AttachmentSyncResult(
      List<TmsAttachment> attachments,
      boolean allAttachmentsDownloaded
  ) {}
}
