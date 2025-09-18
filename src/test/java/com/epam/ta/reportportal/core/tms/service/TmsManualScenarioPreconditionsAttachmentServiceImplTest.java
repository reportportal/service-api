package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttachment;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioPreconditions;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioPreconditionsAttachmentRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualScenarioPreconditionsAttachmentServiceImplTest {

  @Mock
  private TmsAttachmentService tmsAttachmentService;

  @Mock
  private TmsManualScenarioPreconditionsAttachmentRepository preconditionsAttachmentRepository;

  @InjectMocks
  private TmsManualScenarioPreconditionsAttachmentServiceImpl sut;

  private TmsManualScenarioPreconditions preconditions;
  private TmsManualScenarioPreconditions duplicatedPreconditions;
  private TmsManualScenarioPreconditionsRQ preconditionsRQ;
  private TmsAttachment attachment1;
  private TmsAttachment attachment2;
  private TmsAttachment duplicatedAttachment1;
  private TmsAttachment duplicatedAttachment2;
  private List<TmsAttachment> attachments;
  private List<TmsManualScenarioAttachmentRQ> attachmentRQs;
  private List<Long> attachmentIds;
  private Long preconditionsId;
  private Long testCaseId;
  private Long projectId;
  private Long folderId;

  @BeforeEach
  void setUp() {
    preconditionsId = 1L;
    testCaseId = 2L;
    projectId = 3L;
    folderId = 4L;

    preconditions = new TmsManualScenarioPreconditions();
    preconditions.setId(preconditionsId);

    duplicatedPreconditions = new TmsManualScenarioPreconditions();
    duplicatedPreconditions.setId(5L);

    attachment1 = new TmsAttachment();
    attachment1.setId(10L);
    attachment1.setFileName("test1.txt");
    attachment1.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment1.setManualScenarioPreconditions(new HashSet<>());

    attachment2 = new TmsAttachment();
    attachment2.setId(11L);
    attachment2.setFileName("test2.txt");
    attachment2.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment2.setManualScenarioPreconditions(new HashSet<>());

    duplicatedAttachment1 = new TmsAttachment();
    duplicatedAttachment1.setId(20L);
    duplicatedAttachment1.setFileName("test1_copy.txt");
    duplicatedAttachment1.setManualScenarioPreconditions(new HashSet<>());

    duplicatedAttachment2 = new TmsAttachment();
    duplicatedAttachment2.setId(21L);
    duplicatedAttachment2.setFileName("test2_copy.txt");
    duplicatedAttachment2.setManualScenarioPreconditions(new HashSet<>());

    attachments = Arrays.asList(attachment1, attachment2);
    attachmentIds = Arrays.asList(10L, 11L);

    var attachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ1.setId("10");

    var attachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ2.setId("11");

    attachmentRQs = Arrays.asList(attachmentRQ1, attachmentRQ2);

    preconditionsRQ = new TmsManualScenarioPreconditionsRQ();
    preconditionsRQ.setAttachments(attachmentRQs);
  }

  @Test
  void createAttachments_ShouldCreateAttachments_WhenValidPreconditionsAndAttachments() {
    // Given valid preconditions and attachments to create
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments for preconditions
    sut.createAttachments(preconditions, preconditionsRQ);

    // Then attachments should be created and made permanent
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    // Verify that preconditions now have attachments
    assertNotNull(preconditions.getAttachments());
    assertEquals(2, preconditions.getAttachments().size());
    assertTrue(preconditions.getAttachments().contains(attachment1));
    assertTrue(preconditions.getAttachments().contains(attachment2));

    // Verify TTL was removed from attachments
    assertNull(attachment1.getExpiresAt());
    assertNull(attachment2.getExpiresAt());
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenPreconditionsRQIsNull() {
    // When creating attachments with null preconditions RQ
    sut.createAttachments(preconditions, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsEmpty() {
    // Given preconditions RQ with empty attachments list
    var emptyPreconditionsRQ = new TmsManualScenarioPreconditionsRQ();
    emptyPreconditionsRQ.setAttachments(Collections.emptyList());

    // When creating attachments with empty list
    sut.createAttachments(preconditions, emptyPreconditionsRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsNull() {
    // Given preconditions RQ with null attachments
    var nullAttachmentsPreconditionsRQ = new TmsManualScenarioPreconditionsRQ();
    nullAttachmentsPreconditionsRQ.setAttachments(null);

    // When creating attachments with null list
    sut.createAttachments(preconditions, nullAttachmentsPreconditionsRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenNoAttachmentsFound() {
    // Given attachment service returns empty list
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(
        Collections.emptyList());

    // When creating attachments but none exist
    sut.createAttachments(preconditions, preconditionsRQ);

    // Then only validation should occur but no save operations
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService, never()).saveAll(anyList());
  }

  @Test
  void createAttachments_ShouldHandleAttachmentsWithoutExpiresAt_WhenSomeAttachmentsHaveNoTTL() {
    // Given one attachment without TTL
    attachment1.setExpiresAt(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments
    sut.createAttachments(preconditions, preconditionsRQ);

    // Then should not throw exception and process normally
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
    assertNotNull(preconditions.getAttachments());
  }

  @Test
  void updateAttachments_ShouldDeleteExistingAndCreateNew_WhenPreconditionsHaveExistingAttachments() {
    // Given existing preconditions with attachments
    var existingAttachments = new HashSet<TmsAttachment>();
    existingAttachments.add(attachment1);
    preconditions.setAttachments(existingAttachments);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When updating attachments
    sut.updateAttachments(preconditions, preconditionsRQ);

    // Then existing relationships should be deleted and new ones created
    verify(preconditionsAttachmentRepository).deleteByPreconditionsId(preconditionsId);
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
  }

  @Test
  void updateAttachments_ShouldOnlyCreateNew_WhenPreconditionsHaveNoExistingAttachments() {
    // Given preconditions without existing attachments
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When updating attachments
    sut.updateAttachments(preconditions, preconditionsRQ);

    // Then only new attachments should be created
    verify(preconditionsAttachmentRepository, never()).deleteByPreconditionsId(preconditionsId);
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
  }

  @Test
  void patchAttachments_ShouldAddNewAttachments_WhenValidPreconditionsAndNewAttachments() {
    // Given existing preconditions with some attachments
    var existingAttachments = new HashSet<TmsAttachment>();
    var existingAttachment = new TmsAttachment();
    existingAttachment.setId(99L);
    existingAttachments.add(existingAttachment);
    preconditions.setAttachments(existingAttachments);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When patching attachments for existing preconditions
    sut.patchAttachments(preconditions, preconditionsRQ);

    // Then new attachments should be added to existing ones
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    assertEquals(3, preconditions.getAttachments().size()); // 1 existing + 2 new
    assertTrue(preconditions.getAttachments().contains(existingAttachment));
    assertTrue(preconditions.getAttachments().contains(attachment1));
    assertTrue(preconditions.getAttachments().contains(attachment2));
  }

  @Test
  void patchAttachments_ShouldInitializeAttachments_WhenPreconditionsHaveNullAttachments() {
    // Given preconditions with null attachments
    preconditions.setAttachments(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When patching attachments
    sut.patchAttachments(preconditions, preconditionsRQ);

    // Then attachments should be initialized and new ones added
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    assertNotNull(preconditions.getAttachments());
    assertEquals(2, preconditions.getAttachments().size());
  }

  @Test
  void patchAttachments_ShouldDoNothing_WhenPreconditionsRQIsNull() {
    // When patching attachments with null preconditions RQ
    sut.patchAttachments(preconditions, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
  }

  @Test
  void patchAttachments_ShouldDoNothing_WhenAttachmentsListIsEmpty() {
    // Given preconditions RQ with empty attachments list
    var emptyPreconditionsRQ = new TmsManualScenarioPreconditionsRQ();
    emptyPreconditionsRQ.setAttachments(Collections.emptyList());

    // When patching attachments with empty list
    sut.patchAttachments(preconditions, emptyPreconditionsRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDeleteRelationships_WhenValidTestCaseId() {
    // When deleting all preconditions attachment relationships by test case ID
    sut.deleteAllByTestCaseId(testCaseId);

    // Then relationships should be deleted from repository
    verify(preconditionsAttachmentRepository).deleteByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDoNothing_WhenTestCaseIdIsNull() {
    // When deleting relationships with null test case ID
    sut.deleteAllByTestCaseId(null);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDeleteRelationships_WhenValidTestCaseIds() {
    // Given list of test case IDs
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    // When deleting all preconditions attachment relationships by test case IDs
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then relationships should be deleted from repository
    verify(preconditionsAttachmentRepository).deleteByTestCaseIds(testCaseIds);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsEmpty() {
    // Given empty list of test case IDs
    var emptyTestCaseIds = Collections.<Long>emptyList();

    // When deleting relationships with empty list
    sut.deleteAllByTestCaseIds(emptyTestCaseIds);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsNull() {
    // When deleting relationships with null list
    sut.deleteAllByTestCaseIds(null);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDeleteRelationships_WhenValidProjectAndFolderId() {
    // When deleting all preconditions attachment relationships by project and folder ID
    sut.deleteAllByTestFolderId(projectId, folderId);

    // Then relationships should be deleted from repository
    verify(preconditionsAttachmentRepository).deleteByTestFolderId(projectId, folderId);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenProjectIdIsNull() {
    // When deleting relationships with null project ID
    sut.deleteAllByTestFolderId(null, folderId);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenFolderIdIsNull() {
    // When deleting relationships with null folder ID
    sut.deleteAllByTestFolderId(projectId, null);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenBothIdsAreNull() {
    // When deleting relationships with both null IDs
    sut.deleteAllByTestFolderId(null, null);

    // Then no repository operations should occur
    verifyNoInteractions(preconditionsAttachmentRepository);
  }

  @Test
  void duplicateAttachments_ShouldDuplicateAttachments_WhenOriginalPreconditionsHaveAttachments() {
    // Given original preconditions with attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    originalAttachments.add(attachment2);
    preconditions.setAttachments(originalAttachments);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(
        duplicatedAttachment1);
    when(tmsAttachmentService.duplicateTmsAttachment(attachment2)).thenReturn(
        duplicatedAttachment2);

    // When duplicating attachment relationships
    sut.duplicateAttachments(preconditions, duplicatedPreconditions);

    // Then original attachments should be duplicated and new relationships created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment2);

    assertNotNull(duplicatedPreconditions.getAttachments());
    assertEquals(2, duplicatedPreconditions.getAttachments().size());
    assertTrue(duplicatedPreconditions.getAttachments().contains(duplicatedAttachment1));
    assertTrue(duplicatedPreconditions.getAttachments().contains(duplicatedAttachment2));
  }

  @Test
  void duplicateAttachments_ShouldDoNothing_WhenOriginalPreconditionsHaveNoAttachments() {
    // Given original preconditions with no attachments
    preconditions.setAttachments(Collections.emptySet());

    // When duplicating attachments from preconditions with no attachments
    sut.duplicateAttachments(preconditions, duplicatedPreconditions);

    // Then no duplication operations should occur
    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldHandleSingleAttachment_WhenOriginalPreconditionsHaveOneAttachment() {
    // Given original preconditions with single attachment
    var singleAttachment = new HashSet<TmsAttachment>();
    singleAttachment.add(attachment1);
    preconditions.setAttachments(singleAttachment);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(
        duplicatedAttachment1);

    // When duplicating single attachment relationship
    sut.duplicateAttachments(preconditions, duplicatedPreconditions);

    // Then single attachment should be duplicated and new relationship created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);

    assertNotNull(duplicatedPreconditions.getAttachments());
    assertEquals(1, duplicatedPreconditions.getAttachments().size());
    assertTrue(duplicatedPreconditions.getAttachments().contains(duplicatedAttachment1));
  }

  @Test
  void duplicateAttachments_ShouldNotThrowException_WhenOriginalAttachmentsIsNull() {
    // Given original preconditions with null attachments
    preconditions.setAttachments(null);

    // When/Then duplicating attachments should not throw exception
    assertDoesNotThrow(() -> sut.duplicateAttachments(preconditions, duplicatedPreconditions));

    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldInitializeDuplicatedAttachments_WhenDuplicatedPreconditionsHaveNullAttachments() {
    // Given original preconditions with attachments and duplicated with null attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    preconditions.setAttachments(originalAttachments);
    duplicatedPreconditions.setAttachments(null);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(
        duplicatedAttachment1);

    // When duplicating attachments
    sut.duplicateAttachments(preconditions, duplicatedPreconditions);

    // Then duplicated preconditions attachments should be initialized
    assertNotNull(duplicatedPreconditions.getAttachments());
    assertEquals(1, duplicatedPreconditions.getAttachments().size());
  }
}
