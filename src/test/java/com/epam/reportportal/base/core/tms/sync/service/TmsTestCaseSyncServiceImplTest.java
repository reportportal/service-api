package com.epam.reportportal.base.core.tms.sync.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseFieldChangedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TestCaseImportedEvent;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
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
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseSyncServiceImplTest {

  @Mock
  private TmsTestCaseRepository tmsTestCaseRepository;
  @Mock
  private TmsTestCaseVersionService tmsTestCaseVersionService;
  @Mock
  private TmsTestCaseAttributeService tmsTestCaseAttributeService;
  @Mock
  private TmsAttachmentDataStoreService tmsAttachmentDataStoreService;
  @Mock
  private TmsAttachmentRepository tmsAttachmentRepository;
  @Mock
  private TmsSyncJobRepository tmsSyncJobRepository;
  @Mock
  private TmsTestCaseMapper tmsTestCaseMapper;
  @Mock
  private TmsAttachmentMapper tmsAttachmentMapper;
  @Mock
  private TmsManualScenarioMapper tmsManualScenarioMapper;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private TmsTestCaseActivityResourceMapper tmsTestCaseActivityResourceMapper;
  @Mock
  private PlatformTransactionManager transactionManager;

  private TmsTestCaseSyncServiceImpl sut;

  private final Long jobId = 1L;
  private final Long projectId = 10L;
  private final Long organizationId = 100L;

  @BeforeEach
  void setUp() {
    when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    sut = new TmsTestCaseSyncServiceImpl(
        tmsTestCaseRepository,
        tmsTestCaseVersionService,
        tmsTestCaseAttributeService,
        tmsAttachmentDataStoreService,
        tmsAttachmentRepository,
        tmsSyncJobRepository,
        tmsTestCaseMapper,
        tmsAttachmentMapper,
        tmsManualScenarioMapper,
        projectRepository,
        eventPublisher,
        tmsTestCaseActivityResourceMapper,
        transactionManager
    );
  }

  @Test
  void processTestCaseBatch_WhenNewTestCase_ShouldPublishTestCaseImportedEvent() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-1");
    remoteTestCase.setName("New Remote TC");
    remoteTestCase.setUpdatedAt(Instant.now());

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-1")))
        .thenReturn(List.of());

    var newTestCase = new TmsTestCase();
    newTestCase.setExternalId("EXT-1");
    newTestCase.setId(50L);

    when(tmsTestCaseMapper.convertFromRemote(any(), any(), eq(projectId), any()))
        .thenReturn(newTestCase);
    when(tmsTestCaseRepository.saveAll(anyList())).thenReturn(List.of(newTestCase));

    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    when(tmsManualScenarioMapper.convertFromRemote(any(), any())).thenReturn(manualScenarioRQ);

    var version = new TmsTestCaseVersion();
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, newTestCase, manualScenarioRQ))
        .thenReturn(version);

    var activityResource = TestCaseActivityResource.builder().id(50L).name("New Remote TC").build();
    when(tmsTestCaseActivityResourceMapper.buildActivityResource(newTestCase, version))
        .thenReturn(activityResource);

    var importedEvent = new TestCaseImportedEvent(activityResource, null, "System", organizationId);
    when(tmsTestCaseActivityResourceMapper.buildTestCaseImportedEvent(null, "System", organizationId, activityResource))
        .thenReturn(importedEvent);

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);
    var integration = new Integration();

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), null);

    verify(eventPublisher).publishEvent(importedEvent);
  }

  @Test
  void processTestCaseBatch_ShouldStoreRemoteAttachmentWithProjectScopedKeyAndUpdateCounters() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteAttachment = new RemoteAttachment();
    remoteAttachment.setId("attachment-1");
    remoteAttachment.setFilename("screen shot.png");
    remoteAttachment.setMimeType("text/plain");
    remoteAttachment.setSize(4L);
    remoteAttachment.setContentUrl("https://example.com/attachment-1");

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-ATTACHMENT-1");
    remoteTestCase.setName("New Remote TC With Attachment");
    remoteTestCase.setUpdatedAt(Instant.now());
    remoteTestCase.setAttachments(List.of(remoteAttachment));

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-ATTACHMENT-1")))
        .thenReturn(List.of());

    var newTestCase = new TmsTestCase();
    newTestCase.setExternalId("EXT-ATTACHMENT-1");
    newTestCase.setId(70L);
    when(tmsTestCaseMapper.convertFromRemote(any(), any(), eq(projectId), any()))
        .thenReturn(newTestCase);
    when(tmsTestCaseRepository.saveAll(anyList())).thenReturn(List.of(newTestCase));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);
    var integration = new Integration();
    when(connector.downloadAttachment(any(), any())).thenReturn(new ByteArrayInputStream(new byte[] {1, 2, 3, 4}));

    var fileId = "opaque-file-id";
    when(tmsAttachmentDataStoreService.save(any(String.class), any())).thenReturn(fileId);

    var attachment = new TmsAttachment();
    attachment.setId(80L);
    when(tmsAttachmentMapper.convertFromRemote(any(RemoteAttachment.class), eq(fileId), any(), eq(projectId)))
        .thenReturn(attachment);
    when(tmsAttachmentRepository.saveAll(anyList())).thenReturn(List.of(attachment));

    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    when(tmsManualScenarioMapper.convertFromRemote(any(), any())).thenReturn(manualScenarioRQ);

    var version = new TmsTestCaseVersion();
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(projectId, newTestCase, manualScenarioRQ))
        .thenReturn(version);

    var activityResource = TestCaseActivityResource.builder().id(70L).name("New Remote TC With Attachment").build();
    when(tmsTestCaseActivityResourceMapper.buildActivityResource(newTestCase, version))
        .thenReturn(activityResource);

    var importedEvent = new TestCaseImportedEvent(activityResource, null, "System", organizationId);
    when(tmsTestCaseActivityResourceMapper.buildTestCaseImportedEvent(null, "System", organizationId, activityResource))
        .thenReturn(importedEvent);

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), null);

    var fileNameCaptor = ArgumentCaptor.forClass(String.class);
    verify(tmsAttachmentDataStoreService).save(fileNameCaptor.capture(), any());

    var fileName = fileNameCaptor.getValue();
    assertTrue(fileName.startsWith(projectId + File.separator));
    assertTrue(!fileName.startsWith("tms/"));
    assertTrue(fileName.endsWith("_screen_shot.png"));
    assertEquals(1, syncJob.getCounters().getProcessed());
    assertEquals(1, syncJob.getCounters().getImported());
    assertEquals(0, syncJob.getCounters().getSkipped());
    assertEquals(0, syncJob.getCounters().getFailed());
    verify(tmsSyncJobRepository).save(syncJob);
  }

  @Test
  void processTestCaseBatch_WhenExistingTestCaseIsUnchanged_ShouldSkipTestCaseAndUpdateCounters() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-UNCHANGED-1");
    remoteTestCase.setName("Unchanged Remote TC");
    remoteTestCase.setUpdatedAt(Instant.now());

    var existingTestCase = new TmsTestCase();
    existingTestCase.setId(90L);
    existingTestCase.setExternalId("EXT-UNCHANGED-1");
    existingTestCase.setSourceUpdatedAt(remoteTestCase.getUpdatedAt().plusSeconds(1));

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-UNCHANGED-1")))
        .thenReturn(List.of(existingTestCase));

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);
    var integration = new Integration();

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), null);

    assertEquals(1, syncJob.getCounters().getProcessed());
    assertEquals(0, syncJob.getCounters().getImported());
    assertEquals(1, syncJob.getCounters().getSkipped());
    assertEquals(0, syncJob.getCounters().getFailed());
    verify(tmsSyncJobRepository).save(syncJob);
  }

  @Test
  void processTestCaseBatch_WhenTestCaseSyncFails_ShouldIncludeFailedTestCaseInProcessedCounters() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-FAILED-1");
    remoteTestCase.setName("Failed Remote TC");
    remoteTestCase.setUpdatedAt(Instant.now());

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-FAILED-1")))
        .thenReturn(List.of());
    when(tmsTestCaseMapper.convertFromRemote(any(), any(), eq(projectId), any()))
        .thenThrow(new IllegalStateException("sync failure"));

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);
    var integration = new Integration();

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), null);

    assertEquals(1, syncJob.getCounters().getProcessed());
    assertEquals(0, syncJob.getCounters().getImported());
    assertEquals(0, syncJob.getCounters().getSkipped());
    assertEquals(1, syncJob.getCounters().getFailed());
    verify(tmsSyncJobRepository).save(syncJob);
  }

  @Test
  void processTestCaseBatch_WhenBatchIsMixed_ShouldKeepProcessedEqualToImportedPlusSkippedPlusFailed() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var newRemote = new RemoteTestCase();
    newRemote.setId("EXT-NEW");
    newRemote.setName("New");
    newRemote.setUpdatedAt(Instant.now());

    var unchangedRemote = new RemoteTestCase();
    unchangedRemote.setId("EXT-UNCHANGED");
    unchangedRemote.setName("Unchanged");
    unchangedRemote.setUpdatedAt(Instant.now());

    var failingRemote = new RemoteTestCase();
    failingRemote.setId("EXT-FAILING");
    failingRemote.setName("Failing");
    failingRemote.setUpdatedAt(Instant.now());

    var unchangedExisting = new TmsTestCase();
    unchangedExisting.setId(91L);
    unchangedExisting.setExternalId("EXT-UNCHANGED");
    unchangedExisting.setSourceUpdatedAt(unchangedRemote.getUpdatedAt().plusSeconds(1));

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(
        projectId, List.of("EXT-NEW", "EXT-UNCHANGED", "EXT-FAILING")))
        .thenReturn(List.of(unchangedExisting));

    var newTestCase = new TmsTestCase();
    newTestCase.setId(92L);
    newTestCase.setExternalId("EXT-NEW");
    when(tmsTestCaseMapper.convertFromRemote(eq(newRemote), any(), eq(projectId), any()))
        .thenReturn(newTestCase);
    when(tmsTestCaseMapper.convertFromRemote(eq(failingRemote), any(), eq(projectId), any()))
        .thenThrow(new IllegalStateException("sync failure"));
    when(tmsTestCaseRepository.saveAll(anyList())).thenReturn(List.of(newTestCase));

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);

    sut.processTestCaseBatch(jobId, projectId, connector, new Integration(),
        List.of(newRemote, unchangedRemote, failingRemote), null);

    var counters = syncJob.getCounters();
    assertEquals(3, counters.getProcessed());
    assertEquals(1, counters.getImported());
    assertEquals(1, counters.getSkipped());
    assertEquals(1, counters.getFailed());
    assertEquals(counters.getProcessed(),
        counters.getImported() + counters.getSkipped() + counters.getFailed());
    assertEquals(1, syncJob.getErrorLog().getErrors().size());
  }

  @Test
  void processTestCaseBatch_WhenBatchHasDuplicateIds_ShouldUseNewestRecordOnceAndCountDuplicateAsSkipped() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var older = new RemoteTestCase();
    older.setId("EXT-DUP");
    older.setName("Older");
    older.setUpdatedAt(Instant.now().minusSeconds(60));
    var newer = new RemoteTestCase();
    newer.setId("EXT-DUP");
    newer.setName("Newer");
    newer.setUpdatedAt(Instant.now());

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-DUP")))
        .thenReturn(List.of());
    var newTestCase = new TmsTestCase();
    newTestCase.setExternalId("EXT-DUP");
    newTestCase.setId(60L);
    when(tmsTestCaseMapper.convertFromRemote(any(), any(), eq(projectId), any()))
        .thenReturn(newTestCase);
    when(tmsTestCaseRepository.saveAll(anyList())).thenReturn(List.of(newTestCase));
    when(tmsManualScenarioMapper.convertFromRemote(any(), any()))
        .thenReturn(new TmsTextManualScenarioRQ());

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);

    sut.processTestCaseBatch(jobId, projectId, connector, new Integration(),
        List.of(older, newer), null);

    verify(tmsTestCaseMapper, times(1)).convertFromRemote(any(), any(), eq(projectId), any());
    verify(tmsTestCaseMapper).convertFromRemote(eq(newer), any(), eq(projectId), any());
    verify(tmsManualScenarioMapper, times(1)).convertFromRemote(eq(newer), any());
    assertEquals(2, syncJob.getCounters().getProcessed());
    assertEquals(1, syncJob.getCounters().getImported());
    assertEquals(1, syncJob.getCounters().getSkipped());
  }

  @Test
  void processTestCaseBatch_WhenPersistingBatchFails_ShouldCountImportCandidatesAsFailedAndRethrow() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-ROLLBACK-1");
    remoteTestCase.setName("Rolled Back Remote TC");
    remoteTestCase.setUpdatedAt(Instant.now());

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-ROLLBACK-1")))
        .thenReturn(List.of());
    var newTestCase = new TmsTestCase();
    newTestCase.setExternalId("EXT-ROLLBACK-1");
    when(tmsTestCaseMapper.convertFromRemote(any(), any(), eq(projectId), any()))
        .thenReturn(newTestCase);
    when(tmsTestCaseRepository.saveAll(anyList())).thenThrow(new IllegalStateException("db down"));

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);

    assertThrows(IllegalStateException.class, () -> sut.processTestCaseBatch(
        jobId, projectId, connector, new Integration(), List.of(remoteTestCase), null));

    assertEquals(1, syncJob.getCounters().getProcessed());
    assertEquals(0, syncJob.getCounters().getImported());
    assertEquals(0, syncJob.getCounters().getSkipped());
    assertEquals(1, syncJob.getCounters().getFailed());
  }

  @Test
  void processTestCaseBatch_WhenExistingTestCaseUpdated_ShouldPublishTestCaseFieldChangedEvent() {
    var project = new Project();
    project.setOrganizationId(organizationId);
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

    var remoteTestCase = new RemoteTestCase();
    remoteTestCase.setId("EXT-2");
    remoteTestCase.setName("Updated Remote TC");
    remoteTestCase.setUpdatedAt(Instant.now());

    var existingTestCase = new TmsTestCase();
    existingTestCase.setId(60L);
    existingTestCase.setExternalId("EXT-2");
    existingTestCase.setSourceUpdatedAt(Instant.now().minusSeconds(100));

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("EXT-2")))
        .thenReturn(List.of(existingTestCase));

    var beforeVersion = new TmsTestCaseVersion();
    when(tmsTestCaseVersionService.getDefaultVersions(List.of(60L)))
        .thenReturn(Map.of(60L, beforeVersion));

    var beforeResource = TestCaseActivityResource.builder().id(60L).name("Old Name").build();
    when(tmsTestCaseActivityResourceMapper.buildActivityResource(existingTestCase, beforeVersion))
        .thenReturn(beforeResource);

    var updatedTestCase = new TmsTestCase();
    updatedTestCase.setId(60L);
    updatedTestCase.setExternalId("EXT-2");

    when(tmsTestCaseMapper.convertFromRemote(eq(remoteTestCase), eq(existingTestCase), eq(projectId), any()))
        .thenReturn(updatedTestCase);
    when(tmsTestCaseRepository.saveAll(anyList())).thenReturn(List.of(updatedTestCase));

    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    when(tmsManualScenarioMapper.convertFromRemote(any(), any())).thenReturn(manualScenarioRQ);

    var afterVersion = new TmsTestCaseVersion();
    when(tmsTestCaseVersionService.updateDefaultTestCaseVersion(projectId, updatedTestCase, manualScenarioRQ))
        .thenReturn(afterVersion);

    var afterResource = TestCaseActivityResource.builder().id(60L).name("Updated Remote TC").build();
    when(tmsTestCaseActivityResourceMapper.buildActivityResource(updatedTestCase, afterVersion))
        .thenReturn(afterResource);

    var fieldChangedEvent = new TestCaseFieldChangedEvent();
    when(tmsTestCaseActivityResourceMapper.buildTestCaseFieldChangedEvents(null, "System", organizationId, beforeResource, afterResource))
        .thenReturn(List.of(fieldChangedEvent));

    var syncJob = new TmsSyncJob();
    syncJob.setCounters(new SyncCounters());
    syncJob.setErrorLog(new SyncErrorLog());
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(syncJob));

    @SuppressWarnings("unchecked")
    TmsSyncConnector<Integration> connector = mock(TmsSyncConnector.class);
    var integration = new Integration();

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), null);

    verify(eventPublisher).publishEvent(fieldChangedEvent);
  }
}
