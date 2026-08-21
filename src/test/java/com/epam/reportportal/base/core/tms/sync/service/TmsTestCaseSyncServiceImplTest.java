package com.epam.reportportal.base.core.tms.sync.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseVersionService;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteAttachment;
import com.epam.reportportal.base.core.tms.sync.dto.RemoteTestCase;
import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsSyncJobRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncCounters;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.sync.SyncErrorLog;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseSyncServiceImplTest {

  @Mock
  private TmsTestCaseRepository tmsTestCaseRepository;

  @Mock
  private TmsTestCaseVersionService tmsTestCaseVersionService;

  @Mock
  private TmsAttachmentDataStoreService tmsAttachmentDataStoreService;

  @Mock
  private TmsAttachmentRepository tmsAttachmentRepository;

  @Mock
  private TmsSyncJobRepository tmsSyncJobRepository;

  @Mock
  private TmsTestFolderMapper tmsTestFolderMapper;

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private TmsSyncConnector<Integration> connector;

  private TmsTestCaseSyncServiceImpl sut;

  @BeforeEach
  void setUp() {
    TransactionStatus transactionStatus = mock(TransactionStatus.class);
    when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

    sut = new TmsTestCaseSyncServiceImpl(
        tmsTestCaseRepository,
        tmsTestCaseVersionService,
        tmsAttachmentDataStoreService,
        tmsAttachmentRepository,
        tmsSyncJobRepository,
        tmsTestFolderMapper,
        transactionManager
    );
  }

  @Test
  void shouldProcessTestCaseBatchAndLinkAttachments() {
    Long jobId = 10L;
    Long projectId = 1L;
    Long localFolderId = 5L;

    Integration integration = new Integration();
    integration.setId(20L);

    RemoteAttachment remoteAttachment = RemoteAttachment.builder()
        .id("att-1")
        .filename("image.png")
        .mimeType("image/png")
        .size(1024L)
        .contentUrl("http://jira/attachments/1")
        .build();

    RemoteTestCase remoteTestCase = RemoteTestCase.builder()
        .id("KEY-101")
        .name("Test Scenario")
        .description("Description")
        .steps("*Preconditions*: Some preconditions\n*Steps*: Step 1 instruction")
        .expectedResults("Expected outcome")
        .updatedAt(Instant.now())
        .attachments(List.of(remoteAttachment))
        .build();

    when(tmsTestCaseRepository.findByProjectIdAndExternalIdIn(projectId, List.of("KEY-101")))
        .thenReturn(Collections.emptyList());

    InputStream inputStream = new ByteArrayInputStream("file content".getBytes());
    when(connector.downloadAttachment(eq(integration), eq("http://jira/attachments/1")))
        .thenReturn(inputStream);
    when(tmsAttachmentDataStoreService.save(any(), any())).thenReturn("datastore-file-id-1");

    TmsTestFolder folder = new TmsTestFolder();
    folder.setId(localFolderId);
    when(tmsTestFolderMapper.convertFromId(localFolderId)).thenReturn(folder);

    when(tmsTestCaseRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<TmsTestCase> cases = invocation.getArgument(0);
      cases.get(0).setId(100L);
      return cases;
    });

    when(tmsAttachmentRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<TmsAttachment> attachments = invocation.getArgument(0);
      attachments.get(0).setId(555L);
      return attachments;
    });

    TmsSyncJob job = new TmsSyncJob();
    job.setId(jobId);
    job.setCounters(new SyncCounters(0, 0, 0));
    job.setErrorLog(new SyncErrorLog(new ArrayList<>()));
    when(tmsSyncJobRepository.findById(jobId)).thenReturn(Optional.of(job));
    when(tmsSyncJobRepository.save(any(TmsSyncJob.class))).thenReturn(job);

    sut.processTestCaseBatch(jobId, projectId, connector, integration, List.of(remoteTestCase), localFolderId);

    ArgumentCaptor<TmsTextManualScenarioRQ> scenarioCaptor =
        ArgumentCaptor.forClass(TmsTextManualScenarioRQ.class);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(
        eq(projectId), any(TmsTestCase.class), scenarioCaptor.capture());

    TmsTextManualScenarioRQ capturedScenario = scenarioCaptor.getValue();
    assertNotNull(capturedScenario);
    assertEquals("Step 1 instruction", capturedScenario.getInstructions());
    assertEquals("Expected outcome", capturedScenario.getExpectedResult());
    assertNotNull(capturedScenario.getPreconditions());
    assertEquals("Some preconditions", capturedScenario.getPreconditions().getValue());
    assertNotNull(capturedScenario.getAttachments());
    assertEquals(1, capturedScenario.getAttachments().size());
    assertEquals("555", capturedScenario.getAttachments().get(0).getId());

    verify(tmsAttachmentRepository, times(1)).saveAll(anyList());
    assertEquals(1, job.getCounters().getProcessed());
    assertEquals(0, job.getCounters().getFailed());
  }
}
