package com.epam.ta.reportportal.core.tms.service;

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

import com.epam.ta.reportportal.entity.tms.TmsAttachment;
import com.epam.ta.reportportal.entity.tms.TmsStep;
import com.epam.ta.reportportal.dao.tms.TmsStepAttachmentRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepRQ;
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
class TmsStepAttachmentServiceImplTest {

  @Mock
  private TmsAttachmentService tmsAttachmentService;

  @Mock
  private TmsStepAttachmentRepository stepAttachmentRepository;

  @InjectMocks
  private TmsStepAttachmentServiceImpl sut;

  private TmsStep step;
  private TmsStep duplicatedStep;
  private TmsStepRQ stepRQ;
  private TmsAttachment attachment1;
  private TmsAttachment attachment2;
  private TmsAttachment duplicatedAttachment1;
  private TmsAttachment duplicatedAttachment2;
  private List<TmsAttachment> attachments;
  private List<TmsManualScenarioAttachmentRQ> attachmentRQs;
  private List<Long> attachmentIds;
  private Long stepId;
  private Long testCaseId;
  private Long projectId;
  private Long folderId;

  @BeforeEach
  void setUp() {
    stepId = 1L;
    testCaseId = 2L;
    projectId = 3L;
    folderId = 4L;

    step = new TmsStep();
    step.setId(stepId);

    duplicatedStep = new TmsStep();
    duplicatedStep.setId(5L);

    attachment1 = new TmsAttachment();
    attachment1.setId(10L);
    attachment1.setFileName("test1.txt");
    attachment1.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment1.setSteps(new HashSet<>());

    attachment2 = new TmsAttachment();
    attachment2.setId(11L);
    attachment2.setFileName("test2.txt");
    attachment2.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
    attachment2.setSteps(new HashSet<>());

    duplicatedAttachment1 = new TmsAttachment();
    duplicatedAttachment1.setId(20L);
    duplicatedAttachment1.setFileName("test1_copy.txt");
    duplicatedAttachment1.setSteps(new HashSet<>());

    duplicatedAttachment2 = new TmsAttachment();
    duplicatedAttachment2.setId(21L);
    duplicatedAttachment2.setFileName("test2_copy.txt");
    duplicatedAttachment2.setSteps(new HashSet<>());

    attachments = Arrays.asList(attachment1, attachment2);
    attachmentIds = Arrays.asList(10L, 11L);

    var attachmentRQ1 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ1.setId("10");

    var attachmentRQ2 = new TmsManualScenarioAttachmentRQ();
    attachmentRQ2.setId("11");

    attachmentRQs = Arrays.asList(attachmentRQ1, attachmentRQ2);

    stepRQ = new TmsStepRQ();
    stepRQ.setAttachments(attachmentRQs);
  }

  @Test
  void createAttachments_ShouldCreateAttachments_WhenValidStepAndAttachments() {
    // Given valid step and attachments to create
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments for step
    sut.createAttachments(step, stepRQ);

    // Then attachments should be created and made permanent
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);

    // Verify that step now has attachments
    assertNotNull(step.getAttachments());
    assertEquals(2, step.getAttachments().size());
    assertTrue(step.getAttachments().contains(attachment1));
    assertTrue(step.getAttachments().contains(attachment2));

    // Verify TTL was removed from attachments
    assertEquals(null, attachment1.getExpiresAt());
    assertEquals(null, attachment2.getExpiresAt());
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenStepRQIsNull() {
    // When creating attachments with null step RQ
    sut.createAttachments(step, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsEmpty() {
    // Given step RQ with empty attachments list
    var emptyStepRQ = new TmsStepRQ();
    emptyStepRQ.setAttachments(Collections.emptyList());

    // When creating attachments with empty list
    sut.createAttachments(step, emptyStepRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenAttachmentsListIsNull() {
    // Given step RQ with null attachments
    var nullAttachmentsStepRQ = new TmsStepRQ();
    nullAttachmentsStepRQ.setAttachments(null);

    // When creating attachments with null list
    sut.createAttachments(step, nullAttachmentsStepRQ);

    // Then no operations should be performed
    verifyNoInteractions(tmsAttachmentService);
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void createAttachments_ShouldDoNothing_WhenNoAttachmentsFound() {
    // Given attachment service returns empty list
    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(Collections.emptyList());

    // When creating attachments but none exist
    sut.createAttachments(step, stepRQ);

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
    sut.createAttachments(step, stepRQ);

    // Then should not throw exception and process normally
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
    assertNotNull(step.getAttachments());
  }

  @Test
  void createAttachments_ShouldInitializeStepsSet_WhenAttachmentStepsIsNull() {
    // Given attachment with null steps set
    attachment1.setSteps(null);
    attachment2.setSteps(null);

    when(tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds)).thenReturn(attachments);

    // When creating attachments
    sut.createAttachments(step, stepRQ);

    // Then steps sets should be initialized
    verify(tmsAttachmentService).getTmsAttachmentsByIds(attachmentIds);
    verify(tmsAttachmentService).saveAll(attachments);
    assertNotNull(attachment1.getSteps());
    assertNotNull(attachment2.getSteps());
  }

  @Test
  void deleteAllByTestCaseId_ShouldDeleteRelationships_WhenValidTestCaseId() {
    // When deleting all step attachment relationships by test case ID
    sut.deleteAllByTestCaseId(testCaseId);

    // Then relationships should be deleted from repository
    verify(stepAttachmentRepository).deleteByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDoNothing_WhenTestCaseIdIsNull() {
    // When deleting relationships with null test case ID
    sut.deleteAllByTestCaseId(null);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDeleteRelationships_WhenValidTestCaseIds() {
    // Given list of test case IDs
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    // When deleting all step attachment relationships by test case IDs
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then relationships should be deleted from repository
    verify(stepAttachmentRepository).deleteByTestCaseIds(testCaseIds);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsEmpty() {
    // Given empty list of test case IDs
    var emptyTestCaseIds = Collections.<Long>emptyList();

    // When deleting relationships with empty list
    sut.deleteAllByTestCaseIds(emptyTestCaseIds);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsListIsNull() {
    // When deleting relationships with null list
    sut.deleteAllByTestCaseIds(null);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDeleteRelationships_WhenValidProjectAndFolderId() {
    // When deleting all step attachment relationships by project and folder ID
    sut.deleteStepsByTestFolderId(projectId, folderId);

    // Then relationships should be deleted from repository
    verify(stepAttachmentRepository).deleteByTestFolderId(projectId, folderId);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenProjectIdIsNull() {
    // When deleting relationships with null project ID
    sut.deleteStepsByTestFolderId(null, folderId);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenFolderIdIsNull() {
    // When deleting relationships with null folder ID
    sut.deleteStepsByTestFolderId(projectId, null);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenBothIdsAreNull() {
    // When deleting relationships with both null IDs
    sut.deleteStepsByTestFolderId(null, null);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllBySteps_ShouldDeleteRelationships_WhenValidSteps() {
    // Given collection of steps
    var steps = Arrays.asList(step, duplicatedStep);

    // When deleting all step attachment relationships by steps
    sut.deleteAllBySteps(steps);

    // Then relationships should be deleted from repository
    verify(stepAttachmentRepository).deleteByStepIdIn(Arrays.asList(stepId, 5L));
  }

  @Test
  void deleteAllBySteps_ShouldDoNothing_WhenStepsCollectionIsEmpty() {
    // Given empty collection of steps
    var emptySteps = Collections.<TmsStep>emptyList();

    // When deleting relationships with empty collection
    sut.deleteAllBySteps(emptySteps);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void deleteAllBySteps_ShouldDoNothing_WhenStepsCollectionIsNull() {
    // When deleting relationships with null collection
    sut.deleteAllBySteps(null);

    // Then no repository operations should occur
    verifyNoInteractions(stepAttachmentRepository);
  }

  @Test
  void duplicateAttachments_ShouldDuplicateAttachments_WhenOriginalStepHasAttachments() {
    // Given original step with attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    originalAttachments.add(attachment2);
    step.setAttachments(originalAttachments);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);
    when(tmsAttachmentService.duplicateTmsAttachment(attachment2)).thenReturn(duplicatedAttachment2);

    // When duplicating attachment relationships
    sut.duplicateAttachments(step, duplicatedStep);

    // Then original attachments should be duplicated and new relationships created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment2);

    assertNotNull(duplicatedStep.getAttachments());
    assertEquals(2, duplicatedStep.getAttachments().size());
    assertTrue(duplicatedStep.getAttachments().contains(duplicatedAttachment1));
    assertTrue(duplicatedStep.getAttachments().contains(duplicatedAttachment2));
  }

  @Test
  void duplicateAttachments_ShouldDoNothing_WhenOriginalStepHasNoAttachments() {
    // Given original step with no attachments
    step.setAttachments(Collections.emptySet());

    // When duplicating attachments from step with no attachments
    sut.duplicateAttachments(step, duplicatedStep);

    // Then no duplication operations should occur
    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldHandleSingleAttachment_WhenOriginalStepHasOneAttachment() {
    // Given original step with single attachment
    var singleAttachment = new HashSet<TmsAttachment>();
    singleAttachment.add(attachment1);
    step.setAttachments(singleAttachment);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating single attachment relationship
    sut.duplicateAttachments(step, duplicatedStep);

    // Then single attachment should be duplicated and new relationship created
    verify(tmsAttachmentService).duplicateTmsAttachment(attachment1);

    assertNotNull(duplicatedStep.getAttachments());
    assertEquals(1, duplicatedStep.getAttachments().size());
    assertTrue(duplicatedStep.getAttachments().contains(duplicatedAttachment1));
  }

  @Test
  void duplicateAttachments_ShouldNotThrowException_WhenOriginalAttachmentsIsNull() {
    // Given original step with null attachments
    step.setAttachments(null);

    // When/Then duplicating attachments should not throw exception
    assertDoesNotThrow(() -> sut.duplicateAttachments(step, duplicatedStep));

    verify(tmsAttachmentService, never()).duplicateTmsAttachment(any());
  }

  @Test
  void duplicateAttachments_ShouldInitializeDuplicatedAttachments_WhenDuplicatedStepHasNullAttachments() {
    // Given original step with attachments and duplicated step with null attachments
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    step.setAttachments(originalAttachments);
    duplicatedStep.setAttachments(null);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating attachments
    sut.duplicateAttachments(step, duplicatedStep);

    // Then duplicated step attachments should be initialized
    assertNotNull(duplicatedStep.getAttachments());
    assertEquals(1, duplicatedStep.getAttachments().size());
  }

  @Test
  void duplicateAttachments_ShouldInitializeAttachmentSteps_WhenDuplicatedAttachmentHasNullSteps() {
    // Given original step with attachments and duplicated attachment with null steps
    var originalAttachments = new HashSet<TmsAttachment>();
    originalAttachments.add(attachment1);
    step.setAttachments(originalAttachments);
    duplicatedAttachment1.setSteps(null);

    when(tmsAttachmentService.duplicateTmsAttachment(attachment1)).thenReturn(duplicatedAttachment1);

    // When duplicating attachments
    sut.duplicateAttachments(step, duplicatedStep);

    // Then duplicated attachment steps should be initialized
    assertNotNull(duplicatedAttachment1.getSteps());
    assertTrue(duplicatedAttachment1.getSteps().contains(duplicatedStep));
  }

  @Test
  void createAttachments_ShouldHandleAttachmentIds_WhenStepRQHasValidAttachmentIds() {
    // Given step RQ with string attachment IDs
    var attachmentRQ = new TmsManualScenarioAttachmentRQ();
    attachmentRQ.setId("123");

    var stepRQWithIds = new TmsStepRQ();
    stepRQWithIds.setAttachments(List.of(attachmentRQ));

    var expectedAttachment = new TmsAttachment();
    expectedAttachment.setId(123L);
    expectedAttachment.setSteps(new HashSet<>());

    when(tmsAttachmentService.getTmsAttachmentsByIds(List.of(123L)))
        .thenReturn(List.of(expectedAttachment));

    // When creating attachments with string IDs
    sut.createAttachments(step, stepRQWithIds);

    // Then string IDs should be converted to Long and used correctly
    verify(tmsAttachmentService).getTmsAttachmentsByIds(List.of(123L));
    verify(tmsAttachmentService).saveAll(List.of(expectedAttachment));
  }
}
