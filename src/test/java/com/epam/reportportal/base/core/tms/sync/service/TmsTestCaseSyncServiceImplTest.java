package com.epam.reportportal.base.core.tms.sync.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

