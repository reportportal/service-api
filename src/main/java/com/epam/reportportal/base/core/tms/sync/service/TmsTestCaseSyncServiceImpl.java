package com.epam.reportportal.base.core.tms.sync.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncError;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class TmsTestCaseSyncServiceImpl implements TmsTestCaseSyncService {

  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsAttachmentDataStoreService tmsAttachmentDataStoreService;
  private final TmsAttachmentRepository tmsAttachmentRepository;
  private final TmsSyncJobRepository tmsSyncJobRepository;
  private final TmsTestFolderMapper tmsTestFolderMapper;
  private final TransactionTemplate transactionTemplate;

  public TmsTestCaseSyncServiceImpl(
      TmsTestCaseRepository tmsTestCaseRepository,
      TmsTestCaseVersionService tmsTestCaseVersionService,
      TmsAttachmentDataStoreService tmsAttachmentDataStoreService,
      TmsAttachmentRepository tmsAttachmentRepository,
      TmsSyncJobRepository tmsSyncJobRepository,
      TmsTestFolderMapper tmsTestFolderMapper,
      PlatformTransactionManager transactionManager) {
    this.tmsTestCaseRepository = tmsTestCaseRepository;
    this.tmsTestCaseVersionService = tmsTestCaseVersionService;
    this.tmsAttachmentDataStoreService = tmsAttachmentDataStoreService;
    this.tmsAttachmentRepository = tmsAttachmentRepository;
    this.tmsSyncJobRepository = tmsSyncJobRepository;
    this.tmsTestFolderMapper = tmsTestFolderMapper;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Override
  public void processTestCaseBatch(Long jobId, Long projectId,
      TmsSyncConnector<Integration> connector, Integration integration, List<RemoteTestCase> batch,
      Long localFolderId) {
    List<String> externalIds = batch.stream().map(RemoteTestCase::getId).toList();

    // 1. Fetch existing in one query
    Map<String, TmsTestCase> existingTestCases = transactionTemplate.execute(status ->
        tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, externalIds)
            .stream()
            .collect(Collectors.toMap(TmsTestCase::getExternalId, tc -> tc))
    );

    List<TmsTestCase> testCasesToSave = new ArrayList<>();
    Map<String, Boolean> isNewMap = new HashMap<>();
    Map<String, List<TmsAttachment>> attachmentsPerTestCase = new HashMap<>();
    int processedCount = 0;
    int failedCount = 0;
    List<SyncError> errors = new ArrayList<>();

    for (var remoteTestCase : batch) {
      try {
        TmsTestCase existing =
            existingTestCases != null ? existingTestCases.get(remoteTestCase.getId()) : null;
        boolean needsUpdate = existing == null || existing.getSourceUpdatedAt() == null
            || remoteTestCase.getUpdatedAt().isAfter(existing.getSourceUpdatedAt());

        if (!needsUpdate) {
          continue;
        }

        // 2. Process Attachments (Network + Datastore I/O - NO DB TRANSACTION)
        List<TmsAttachment> testCaseAttachments = new ArrayList<>();
        if (remoteTestCase.getAttachments() != null) {
          for (var remoteAttachment : remoteTestCase.getAttachments()) {
            try (InputStream inputStream = connector.downloadAttachment(integration,
                remoteAttachment.getContentUrl())) {

              String sanitizedFilename = remoteAttachment.getFilename()
                  .replaceAll("[^a-zA-Z0-9.-]", "_");
              String shardedPath = String.format("tms/%d/%s/%s_%s",
                  projectId, remoteTestCase.getId(), remoteAttachment.getId(), sanitizedFilename);

              var fileId = tmsAttachmentDataStoreService.save(shardedPath, inputStream);

              var attachment = new TmsAttachment();
              attachment.setFileName(remoteAttachment.getFilename());
              attachment.setFileType(remoteAttachment.getMimeType());
              attachment.setFileSize(remoteAttachment.getSize());
              attachment.setPathToFile(fileId);
              attachment.setExpiresAt(null);
              testCaseAttachments.add(attachment);
            } catch (Exception e) {
              log.warn("Failed to sync attachment {} for test case {}", remoteAttachment.getId(),
                  remoteTestCase.getId(), e);
              errors.add(new SyncError(remoteAttachment.getId(),
                  "Attachment sync failed: " + e.getMessage(), null));
            }
          }
        }
        if (!testCaseAttachments.isEmpty()) {
          attachmentsPerTestCase.put(remoteTestCase.getId(), testCaseAttachments);
        }

        TmsTestCase testCase = existing != null ? existing : new TmsTestCase();
        if (existing == null) {
          var project = new Project();
          project.setId(projectId);
          testCase.setProject(project);
          testCase.setExternalId(remoteTestCase.getId());
          isNewMap.put(remoteTestCase.getId(), true);
        } else {
          isNewMap.put(remoteTestCase.getId(), false);
        }

        testCase.setName(remoteTestCase.getName());
        testCase.setDescription(remoteTestCase.getDescription());
        testCase.setSourceUpdatedAt(remoteTestCase.getUpdatedAt());

        if (localFolderId != null) {
          testCase.setTestFolder(tmsTestFolderMapper.convertFromId(localFolderId));
        }

        testCasesToSave.add(testCase);
        processedCount++;
      } catch (Exception e) {
        log.error("Failed to sync test case: {}", remoteTestCase.getId(), e);
        failedCount++;
        errors.add(
            new SyncError(remoteTestCase.getId(), e.getMessage(), ExceptionUtils.getStackTrace(e)));
      }
    }

    // 3. Save Batch (Short DB Write Transaction)
    final int finalProcessedCount = processedCount;
    final int finalFailedCount = failedCount;
    transactionTemplate.execute(status -> {
      List<TmsAttachment> allAttachments = attachmentsPerTestCase.values().stream()
          .flatMap(List::stream)
          .toList();
      if (!allAttachments.isEmpty()) {
        tmsAttachmentRepository.saveAll(allAttachments);
      }

      if (!testCasesToSave.isEmpty()) {
        List<TmsTestCase> savedTestCases = tmsTestCaseRepository.saveAll(testCasesToSave);
        Map<String, TmsTestCase> savedMap = savedTestCases.stream()
            .collect(Collectors.toMap(TmsTestCase::getExternalId, tc -> tc, (tc1, tc2) -> tc1));

        for (var remoteTestCase : batch) {
          Boolean isNew = isNewMap.get(remoteTestCase.getId());
          if (isNew != null) {
            TmsTestCase savedCase = savedMap.get(remoteTestCase.getId());
            if (savedCase != null) {
              List<TmsAttachment> savedAttachments = attachmentsPerTestCase.get(remoteTestCase.getId());
              TmsTextManualScenarioRQ manualScenarioRQ = buildManualScenarioRQ(remoteTestCase, savedAttachments);
              if (Boolean.TRUE.equals(isNew)) {
                tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, savedCase, manualScenarioRQ);
              } else {
                tmsTestCaseVersionService.updateDefaultTestCaseVersion(projectId, savedCase, manualScenarioRQ);
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

  private TmsTextManualScenarioRQ buildManualScenarioRQ(RemoteTestCase remoteTestCase,
      List<TmsAttachment> attachments) {
    String rawSteps = remoteTestCase.getSteps();
    String preconditionsText = null;
    String instructionsText = rawSteps;

    if (StringUtils.isNotBlank(rawSteps)) {
      if (rawSteps.contains("*Preconditions*:")) {
        int preconditionsIdx = rawSteps.indexOf("*Preconditions*:");
        int stepsIdx = rawSteps.indexOf("*Steps*:");
        if (stepsIdx > preconditionsIdx) {
          preconditionsText = rawSteps.substring(preconditionsIdx + "*Preconditions*:".length(), stepsIdx).trim();
          instructionsText = rawSteps.substring(stepsIdx + "*Steps*:".length()).trim();
        } else {
          preconditionsText = rawSteps.substring(preconditionsIdx + "*Preconditions*:".length()).trim();
          instructionsText = "";
        }
      } else if (rawSteps.contains("*Steps*:")) {
        int stepsIdx = rawSteps.indexOf("*Steps*:");
        instructionsText = rawSteps.substring(stepsIdx + "*Steps*:".length()).trim();
      }
    }

    TmsManualScenarioPreconditionsRQ preconditionsRQ = null;
    if (StringUtils.isNotBlank(preconditionsText)) {
      preconditionsRQ = TmsManualScenarioPreconditionsRQ.builder()
          .value(preconditionsText)
          .build();
    }

    List<TmsManualScenarioAttachmentRQ> attachmentRQs = null;
    if (CollectionUtils.isNotEmpty(attachments)) {
      attachmentRQs = attachments.stream()
          .filter(att -> att.getId() != null)
          .map(att -> TmsManualScenarioAttachmentRQ.builder()
              .id(String.valueOf(att.getId()))
              .build())
          .collect(Collectors.toList());
    }

    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions(instructionsText)
        .expectedResult(remoteTestCase.getExpectedResults())
        .preconditions(preconditionsRQ)
        .attachments(attachmentRQs)
        .build();
  }
}
