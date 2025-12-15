package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTextManualScenario;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTextManualScenarioAttachmentRepository;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import java.time.Instant;
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
class TmsTextManualScenarioAttachmentServiceImplTest {

  @Mock
  private TmsAttachmentService tmsAttachmentService;

  @Mock
  private TmsTextManualScenarioAttachmentRepository textManualScenarioAttachmentRepository;

  @InjectMocks
  private TmsTextManualScenarioAttachmentServiceImpl sut;

  private TmsTextManualScenario textManualScenario;
  private TmsTextManualScenario duplicatedTextManualScenario;
  private TmsTextManualScenarioRQ textManualScenarioRQ;
  private TmsAttachment attachment1;
  private TmsAttachment attachment2;
  private TmsAttachment duplicatedAttachment1;
  private TmsAttachment duplicatedAttachment2;
  private List<TmsAttachment> attachments;
  private List<TmsManualScenarioAttachmentRQ> attachmentRQs;
  private List<Long> attachmentIds;
  private Long textScenarioId;
  private Long testCaseId;
  private Long projectId;
  private Long folderId;

  @BeforeEach
  void setUp() {
    textScenarioId = 1L;
    testCaseId = 2L;
    projectId = 3L;
    folderId = 4L;

    textManualScenario = new TmsTextManualScenario();
    textManualScenario.setManualScenarioId(textScenarioId);

    duplicatedTextManualScenario = new TmsTextManualScenario();
    duplicatedTextManualScenario.setManualScenarioId(5L);

    attachment1 = new TmsAttachment();
    attachment1.setId(10L);
    attachment1.setFileName("test1.txt");
    attachment1.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment1.setTextManualScenarios(new HashSet<>());

    attachment2 = new TmsAttachment();
    attachment2.setId(11L);
    attachment2.setFileName("test2.txt");
    attachment2.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment2.setTextManualScenarios(new HashSet<>());

    duplicatedAttachment1 = new TmsAttachment();
    duplicatedAttachment1.setId(20L);
    duplicatedAttachment1.setFileName("test1_copy.txt");
    duplicatedAttachment1.setTextManualScenarios(new HashSet<>());

    duplicatedAttachment2 = new TmsAttachment();
    duplicatedAttachment2.setId(21L);
    duplicatedAttachment2.setFileName("test2_copy.txt");
    duplicatedAttachment2.setTextManualScenarios(new HashSet<>());

    attachments = Arrays.asList(attachment1, attachment2);
    attachmentIds = Arrays.asList(10L, 11L);

    var attachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ1.setId("10");

    var attachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ2.setId("11");

    attachmentRQs = Arrays.asList(attachmentRQ1, attachmentRQ2);

    textManualScenarioRQ = new TmsTextManualScenarioRQ();
    textManualScenarioRQ.setAttachments(attachmentRQs);
  }

  @Test
  void createAttachments_ShouldCreateAttachments_WhenValidTextScenarioAndAttachments() {
    // Given valid text manual scenario and attachments to create
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments for text manual scenario
    sut.createAttachments(textManualScenario, textManualScenarioRQ);

    // Then attachments should be created and made permanent
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    // Verify that text scenario now has attachments
    assertNotNull(textManualScenario.getAttachments());
    assertEquals(2, textManualScenario.getAttachments().size());
    assertTrue(textManualScenario.getAttachments().contains(attachment1));
    assertTrue(textManualScenario.getAttachments().contains(attachment2));

    // Verify TTL was removed from attachments
    assertEquals(null, attachment1.getExpiresAt());
    assertEquals(null, attachment2.getExpiresAt());

    // Verify bidirectional relationships
    assertTrue(attachment1.getTextManualScenarios().contains(textManualScenario));
    assertTrue(attachment2.getTextManualScenarios().contains(textManualScenario));
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenTextScenarioRQIsNull() {
    // When creating attachments with null text scenario RQ
    sut.createAttachments(textManualScenario, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsEmpty() {
    // Given text scenario RQ with empty attachments list
    var emptyTextScenarioRQ = new TmsTextManualScenarioRQ();
    emptyTextScenarioRQ.setAttachments(Collections.emptyList());

    // When creating attachments with empty list
    sut.createAttachments(textManualScenario, emptyTextScenarioRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsNull() {
    // Given text scenario RQ with null attachments
    var nullAttachmentsTextScenarioRQ = new TmsTextManualScenarioRQ();
    nullAttachmentsTextScenarioRQ.setAttachments(null);

    // When creating attachments with null list
    sut.createAttachments(textManualScenario, nullAttachmentsTextScenarioRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenNoAttachmentsFound() {
    // Given attachment service returns empty list
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(
        Collections.emptyList());

    // When creating attachments but none exist
    sut.createAttachments(textManualScenario, textManualScenarioRQ);

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
    sut.createAttachments(textManualScenario, textManualScenarioRQ);

    // Then should not throw exception and process normally
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
    assertNotNull(textManualScenario.getAttachments());
  }

  @Test
  void createAttachments_ShouldInitializeTextManualScenariosSet_WhenAttachmentTextManualScenariosIsNull() {
    // Given attachment with null text manual scenarios set
    attachment1.setTextManualScenarios(null);
    attachment2.setTextManualScenarios(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments
    sut.createAttachments(textManualScenario, textManualScenarioRQ);

    // Then text manual scenarios sets should be initialized
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
    assertNotNull(attachment1.getTextManualScenarios());
    assertNotNull(attachment2.getTextManualScenarios());
  }

  @Test
  void updateAttachments_ShouldUpdateAttachments_WhenTextScenarioHasExistingAttachments() {
    // Given text scenario with existing attachments
    var existingAttachments = new HashSet<TmsAttachment>();
    existingAttachments.add(new TmsAttachment());
    textManualScenario.setAttachments(existingAttachments);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When updating attachments
    sut.updateAttachments(textManualScenario, textManualScenarioRQ);

    // Then existing relationships should be deleted and new ones created
    verify(textManualScenarioAttachmentRepository).deleteByTextManualScenarioId(textScenarioId);
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    // Verify attachments were replaced
    assertEquals(2, textManualScenario.getAttachments().size());
    assertTrue(textManualScenario.getAttachments().contains(attachment1));
    assertTrue(textManualScenario.getAttachments().contains(attachment2));
  }

  @Test
  void updateAttachments_ShouldSkipDelete_WhenTextScenarioHasNoExistingAttachments() {
    // Given text scenario with no existing attachments
    textManualScenario.setAttachments(new HashSet<>());

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When updating attachments
    sut.updateAttachments(textManualScenario, textManualScenarioRQ);

    // Then delete should be skipped and new attachments created
    verify(textManualScenarioAttachmentRepository, never()).deleteByTextManualScenarioId(any());
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
  }

  @Test
  void updateAttachments_ShouldSkipDelete_WhenTextScenarioAttachmentsIsNull() {
    // Given text scenario with null attachments
    textManualScenario.setAttachments(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When updating attachments
    sut.updateAttachments(textManualScenario, textManualScenarioRQ);

    // Then delete should be skipped and new attachments created
    verify(textManualScenarioAttachmentRepository, never()).deleteByTextManualScenarioId(any());
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
  }

  @Test
  void patchAttachments_ShouldPatchAttachments_WhenValidTextScenarioAndNewAttachments() {
    // Given valid existing text scenario and new attachments to patch
    var existingAttachment = new TmsAttachment();
    existingAttachment.setId(99L);
    textManualScenario.setAttachments(new HashSet<>(Arrays.asList(existingAttachment)));

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When patching attachments for existing text scenario
    sut.patchAttachments(textManualScenario, textManualScenarioRQ);

    // Then new attachments should be added to existing ones
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    // Verify all attachments are present (existing + new)
    assertEquals(3, textManualScenario.getAttachments().size());
    assertTrue(textManualScenario.getAttachments().contains(existingAttachment));
    assertTrue(textManualScenario.getAttachments().contains(attachment1));
    assertTrue(textManualScenario.getAttachments().contains(attachment2));
  }

  @Test
  void patchAttachments_ShouldInitializeAttachments_WhenTextScenarioAttachmentsIsNull() {
    // Given text scenario with null attachments
    textManualScenario.setAttachments(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When patching attachments
    sut.patchAttachments(textManualScenario, textManualScenarioRQ);

    // Then attachments should be initialized and new ones added
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    assertNotNull(textManualScenario.getAttachments());
    assertEquals(2, textManualScenario.getAttachments().size());
  }

  @Test
  void patchAttachments_ShouldDoNothing_WhenTextScenarioRQIsNull() {
    // When patching attachments with null text scenario RQ
    sut.patchAttachments(textManualScenario, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void patchAttachments_ShouldDoNothing_WhenAttachmentsListIsEmpty() {
    // Given text scenario RQ with empty attachments list
    var emptyTextScenarioRQ = new TmsTextManualScenarioRQ();
    emptyTextScenarioRQ.setAttachments(Collections.emptyList());

    // When patching attachments with empty list
    sut.patchAttachments(textManualScenario, emptyTextScenarioRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void patchAttachments_ShouldDoNothing_WhenNoAttachmentsFound() {
    // Given attachment service returns empty list
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(
        Collections.emptyList());

    // When patching attachments but none exist
    sut.patchAttachments(textManualScenario, textManualScenarioRQ);

    // Then no save operations should occur
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService, never()).saveAll(anyList());
  }

  @Test
  void deleteAllByTestCaseId_ShouldDeleteRelationships_WhenValidTestCaseId() {
    // When deleting all text scenario attachment relationships by test case ID
    sut.deleteAllByTestCaseId(testCaseId);

    // Then relationships should be deleted from repository
    verify(textManualScenarioAttachmentRepository).deleteByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDoNothing_WhenTestCaseIdIsNull() {
    // When deleting relationships with null test case ID
    sut.deleteAllByTestCaseId(null);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDeleteRelationships_WhenValidTestCaseIds() {
    // Given list of test case IDs
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    // When deleting all text scenario attachment relationships by test case IDs
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then relationships should be deleted from repository
    verify(textManualScenarioAttachmentRepository).deleteByTestCaseIds(testCaseIds);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsEmpty() {
    // Given empty list of test case IDs
    var emptyTestCaseIds = Collections.<Long>emptyList();

    // When deleting relationships with empty list
    sut.deleteAllByTestCaseIds(emptyTestCaseIds);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsNull() {
    // When deleting relationships with null list
    sut.deleteAllByTestCaseIds(null);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDeleteRelationships_WhenValidProjectAndFolderId() {
    // When deleting all text scenario attachment relationships by project and folder ID
    sut.deleteAllByTestFolderId(projectId, folderId);

    // Then relationships should be deleted from repository
    verify(textManualScenarioAttachmentRepository).deleteByTestFolderId(projectId, folderId);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenProjectIdIsNull() {
    // When deleting relationships with null project ID
    sut.deleteAllByTestFolderId(null, folderId);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenFolderIdIsNull() {
    // When deleting relationships with null folder ID
    sut.deleteAllByTestFolderId(projectId, null);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenBothIdsAreNull() {
    // When deleting relationships with both null IDs
    sut.deleteAllByTestFolderId(null, null);

    // Then no repository operations should occur
    verifyNoInteractions(textManualScenarioAttachmentRepository);
  }

  @Test
  void duplicateAttachments_ShouldDuplicateAttachments_WhenOriginalTextScenarioHasAttachments() {
    // Given original text scenario with attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    originalAttachments.add(attachment2);
    textManualScenario.setAttachments(originalAttachments);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);
    when(tmsAttachmentService.duplicateTmsAttachment(attachment2)).thenReturn(duplicatedAttachment2);

    // When duplicating attachment relationships
    sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario);

    // Then original attachments should be duplicated and new relationships created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment2);

    assertNotNull(duplicatedTextManualScenario.getAttachments());
    assertEquals(2, duplicatedTextManualScenario.getAttachments().size());
    assertTrue(duplicatedTextManualScenario.getAttachments().contains(duplicatedAttachment1));
    assertTrue(duplicatedTextManualScenario.getAttachments().contains(duplicatedAttachment2));

    // Verify bidirectional relationships
    assertTrue(duplicatedAttachment1.getTextManualScenarios().contains(duplicatedTextManualScenario));
    assertTrue(duplicatedAttachment2.getTextManualScenarios().contains(duplicatedTextManualScenario));
  }

  @Test
  void duplicateAttachments_ShouldDoNothing_WhenOriginalTextScenarioHasNoAttachments() {
    // Given original text scenario with no attachments
    textManualScenario.setAttachments(Collections.emptySet());

    // When duplicating attachments from text scenario with no attachments
    sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario);

    // Then no duplication operations should occur
    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldHandleSingleAttachment_WhenOriginalTextScenarioHasOneAttachment() {
    // Given original text scenario with single attachment
    var singleAttachment = new HashSet<TmsAttachment>();
    singleAttachment.add(attachment1);
    textManualScenario.setAttachments(singleAttachment);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating single attachment relationship
    sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario);

    // Then single attachment should be duplicated and new relationship created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);

    assertNotNull(duplicatedTextManualScenario.getAttachments());
    assertEquals(1, duplicatedTextManualScenario.getAttachments().size());
    assertTrue(duplicatedTextManualScenario.getAttachments().contains(duplicatedAttachment1));
  }

  @Test
  void duplicateAttachments_ShouldNotThrowException_WhenOriginalAttachmentsIsNull() {
    // Given original text scenario with null attachments
    textManualScenario.setAttachments(null);

    // When/Then duplicating attachments should not throw exception
    assertDoesNotThrow(() -> sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario));

    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldInitializeDuplicatedAttachments_WhenDuplicatedTextScenarioHasNullAttachments() {
    // Given original text scenario with attachments and duplicated text scenario with null attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    textManualScenario.setAttachments(originalAttachments);
    duplicatedTextManualScenario.setAttachments(null);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating attachments
    sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario);

    // Then duplicated text scenario attachments should be initialized
    assertNotNull(duplicatedTextManualScenario.getAttachments());
    assertEquals(1, duplicatedTextManualScenario.getAttachments().size());
  }

  @Test
  void duplicateAttachments_ShouldInitializeAttachmentTextManualScenarios_WhenDuplicatedAttachmentHasNullTextManualScenarios() {
    // Given original text scenario with attachments and duplicated attachment with null text manual scenarios
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    textManualScenario.setAttachments(originalAttachments);
    duplicatedAttachment1.setTextManualScenarios(null);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating attachments
    sut.duplicateAttachments(textManualScenario, duplicatedTextManualScenario);

    // Then duplicated attachment text manual scenarios should be initialized
    assertNotNull(duplicatedAttachment1.getTextManualScenarios());
    assertTrue(duplicatedAttachment1.getTextManualScenarios().contains(duplicatedTextManualScenario));
  }

  @Test
  void createAttachments_ShouldHandleAttachmentIds_WhenTextScenarioRQHasValidAttachmentIds() {
    // Given text scenario RQ with string attachment IDs
    var attachmentRQ = new TmsManualScenarioAttachmentRQ();
    attachmentRQ.setId("123");

    var textScenarioRQWithIds = new TmsTextManualScenarioRQ();
    textScenarioRQWithIds.setAttachments(List.of(attachmentRQ));

    var expectedAttachment = new TmsAttachment();
    expectedAttachment.setId(123L);
    expectedAttachment.setTextManualScenarios(new HashSet<>());

    when(tmsAttachmentService.getTmsAttachmentsByIds(List.of(123L)))
        .thenReturn(List.of(expectedAttachment));

    // When creating attachments with string IDs
    sut.createAttachments(textManualScenario, textScenarioRQWithIds);

    // Then string IDs should be converted to Long and used correctly
    verify(tmsAttachmentService).getTmsAttachmentsByIds(List.of(123L));
    verify(tmsAttachmentService).saveAll(List.of(expectedAttachment));
  }

  @Test
  void patchAttachments_ShouldHandleAttachmentIds_WhenTextScenarioRQHasValidAttachmentIds() {
    // Given text scenario RQ with string attachment IDs
    var attachmentRQ = new TmsManualScenarioAttachmentRQ();
    attachmentRQ.setId("456");

    var textScenarioRQWithIds = new TmsTextManualScenarioRQ();
    textScenarioRQWithIds.setAttachments(List.of(attachmentRQ));

    var expectedAttachment = new TmsAttachment();
    expectedAttachment.setId(456L);
    expectedAttachment.setTextManualScenarios(new HashSet<>());

    when(tmsAttachmentService.getTmsAttachmentsByIds(List.of(456L)))
        .thenReturn(List.of(expectedAttachment));

    // When patching attachments with string IDs
    sut.patchAttachments(textManualScenario, textScenarioRQWithIds);

    // Then string IDs should be converted to Long and used correctly
    verify(tmsAttachmentService).getTmsAttachmentsByIds(List.of(456L));
    verify(tmsAttachmentService).saveAll(List.of(expectedAttachment));
  }
}
