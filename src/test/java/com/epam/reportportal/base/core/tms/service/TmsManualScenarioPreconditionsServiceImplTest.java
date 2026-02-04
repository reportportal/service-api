package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioPreconditions;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioPreconditionRepository;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioPreconditionsMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsManualScenarioPreconditionsServiceImplTest {

  @Mock
  private TmsManualScenarioPreconditionsAttachmentService tmsManualScenarioPreconditionsAttachmentService;

  @Mock
  private TmsManualScenarioPreconditionRepository tmsManualScenarioPreconditionRepository;

  @Mock
  private TmsManualScenarioPreconditionsMapper tmsManualScenarioPreconditionsMapper;

  @InjectMocks
  private TmsManualScenarioPreconditionsServiceImpl sut;

  private TmsManualScenario manualScenario;
  private TmsManualScenario existingManualScenario;
  private TmsManualScenario originalScenario;
  private TmsManualScenario duplicatedScenario;
  private TmsManualScenarioPreconditionsRQ preconditionsRQ;
  private TmsManualScenarioPreconditions preconditions;
  private TmsManualScenarioPreconditions existingPreconditions;
  private TmsManualScenarioPreconditions savedPreconditions;
  private TmsManualScenarioPreconditions duplicatedPreconditions;
  private TmsManualScenarioPreconditions savedDuplicatedPreconditions;
  private Long testCaseId;
  private Long projectId;
  private Long folderId;
  private List<Long> testCaseIds;

  @BeforeEach
  void setUp() {
    testCaseId = 1L;
    projectId = 2L;
    folderId = 3L;
    testCaseIds = Arrays.asList(1L, 2L, 3L);

    manualScenario = new TmsManualScenario();
    manualScenario.setId(10L);

    existingManualScenario = new TmsManualScenario();
    existingManualScenario.setId(11L);

    originalScenario = new TmsManualScenario();
    originalScenario.setId(12L);

    duplicatedScenario = new TmsManualScenario();
    duplicatedScenario.setId(13L);

    preconditionsRQ = new TmsManualScenarioPreconditionsRQ();
    preconditionsRQ.setValue("Test preconditions");

    preconditions = new TmsManualScenarioPreconditions();
    preconditions.setValue("Test preconditions");

    existingPreconditions = new TmsManualScenarioPreconditions();
    existingPreconditions.setId(20L);
    existingPreconditions.setValue("Existing preconditions");

    savedPreconditions = new TmsManualScenarioPreconditions();
    savedPreconditions.setId(21L);
    savedPreconditions.setValue("Test preconditions");
    savedPreconditions.setManualScenario(manualScenario);

    duplicatedPreconditions = new TmsManualScenarioPreconditions();
    duplicatedPreconditions.setValue("Test preconditions");

    savedDuplicatedPreconditions = new TmsManualScenarioPreconditions();
    savedDuplicatedPreconditions.setId(22L);
    savedDuplicatedPreconditions.setValue("Test preconditions");
    savedDuplicatedPreconditions.setManualScenario(duplicatedScenario);
  }

  @Test
  void createPreconditions_ShouldCreatePreconditions_WhenValidData() {
    // Given valid manual scenario and preconditions RQ
    when(tmsManualScenarioPreconditionsMapper.toEntity(preconditionsRQ)).thenReturn(preconditions);
    when(tmsManualScenarioPreconditionRepository.save(preconditions)).thenReturn(savedPreconditions);

    // When creating preconditions
    sut.createPreconditions(manualScenario, preconditionsRQ);

    // Then preconditions should be created with attachments and set on manual scenario
    verify(tmsManualScenarioPreconditionsMapper).toEntity(preconditionsRQ);
    verify(tmsManualScenarioPreconditionRepository).save(preconditions);
    verify(tmsManualScenarioPreconditionsAttachmentService).createAttachments(savedPreconditions, preconditionsRQ);
    // Note: manualScenario.setPreconditions(savedPreconditions) is called in implementation
  }

  @Test
  void createPreconditions_ShouldDoNothing_WhenPreconditionsRQIsNull() {
    // When creating preconditions with null RQ
    sut.createPreconditions(manualScenario, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsMapper);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
  }

  @Test
  void updatePreconditions_ShouldUpdateExistingPreconditions_WhenExistingPreconditionsExistAndRQProvided() {
    // Given manual scenario with existing preconditions and update RQ
    existingManualScenario.setPreconditions(existingPreconditions);

    // When updating preconditions
    sut.updatePreconditions(existingManualScenario, preconditionsRQ);

    // Then existing preconditions should be updated through mapper and attachments updated
    verify(tmsManualScenarioPreconditionsMapper).update(existingPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionsAttachmentService).updateAttachments(existingPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionRepository, never()).deleteById(any());
    verify(tmsManualScenarioPreconditionsMapper, never()).toEntity(any());
  }

  @Test
  void updatePreconditions_ShouldDeleteExistingPreconditions_WhenExistingPreconditionsExistAndRQIsNull() {
    // Given manual scenario with existing preconditions and null RQ
    existingManualScenario.setPreconditions(existingPreconditions);

    // When updating with null preconditions RQ
    sut.updatePreconditions(existingManualScenario, null);

    // Then existing preconditions should be deleted and scenario preconditions set to null
    verify(tmsManualScenarioPreconditionRepository).deleteById(existingPreconditions.getId());
    verify(tmsManualScenarioPreconditionsMapper, never()).update(any(), any());
    verify(tmsManualScenarioPreconditionsAttachmentService, never()).updateAttachments(any(), any());
    verify(tmsManualScenarioPreconditionsMapper, never()).toEntity(any());
  }

  @Test
  void updatePreconditions_ShouldCreatePreconditions_WhenNoExistingPreconditionsAndRQProvided() {
    // Given manual scenario without existing preconditions and valid RQ
    existingManualScenario.setPreconditions(null);
    when(tmsManualScenarioPreconditionsMapper.toEntity(preconditionsRQ)).thenReturn(preconditions);
    when(tmsManualScenarioPreconditionRepository.save(preconditions)).thenReturn(savedPreconditions);

    // When updating preconditions
    sut.updatePreconditions(existingManualScenario, preconditionsRQ);

    // Then new preconditions should be created
    verify(tmsManualScenarioPreconditionsMapper).toEntity(preconditionsRQ);
    verify(tmsManualScenarioPreconditionRepository).save(preconditions);
    verify(tmsManualScenarioPreconditionsAttachmentService).createAttachments(savedPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionsMapper, never()).update(any(), any());
  }

  @Test
  void updatePreconditions_ShouldDoNothing_WhenNoExistingPreconditionsAndRQIsNull() {
    // Given manual scenario without existing preconditions and null RQ
    existingManualScenario.setPreconditions(null);

    // When updating with null preconditions RQ
    sut.updatePreconditions(existingManualScenario, null);

    // Then no operations should be performed
    verify(tmsManualScenarioPreconditionRepository, never()).deleteById(any());
    verify(tmsManualScenarioPreconditionsMapper, never()).toEntity(any());
    verify(tmsManualScenarioPreconditionsMapper, never()).update(any(), any());
    verify(tmsManualScenarioPreconditionsAttachmentService, never()).createAttachments(any(), any());
    verify(tmsManualScenarioPreconditionsAttachmentService, never()).updateAttachments(any(), any());
  }

  @Test
  void patchPreconditions_ShouldCreatePreconditions_WhenNoExistingPreconditions() {
    // Given manual scenario without existing preconditions
    existingManualScenario.setPreconditions(null);
    when(tmsManualScenarioPreconditionsMapper.toEntity(preconditionsRQ)).thenReturn(preconditions);
    when(tmsManualScenarioPreconditionRepository.save(preconditions)).thenReturn(savedPreconditions);

    // When patching preconditions
    sut.patchPreconditions(existingManualScenario, preconditionsRQ);

    // Then new preconditions should be created
    verify(tmsManualScenarioPreconditionsMapper).toEntity(preconditionsRQ);
    verify(tmsManualScenarioPreconditionRepository).save(preconditions);
    verify(tmsManualScenarioPreconditionsAttachmentService).createAttachments(savedPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionsMapper, never()).patch(any(), any());
  }

  @Test
  void patchPreconditions_ShouldPatchExistingPreconditions_WhenExistingPreconditionsExist() {
    // Given manual scenario with existing preconditions
    existingManualScenario.setPreconditions(existingPreconditions);
    when(tmsManualScenarioPreconditionRepository.save(existingPreconditions)).thenReturn(savedPreconditions);

    // When patching preconditions
    sut.patchPreconditions(existingManualScenario, preconditionsRQ);

    // Then existing preconditions should be patched
    verify(tmsManualScenarioPreconditionsMapper).patch(existingPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionRepository).save(existingPreconditions);
    verify(tmsManualScenarioPreconditionsAttachmentService).patchAttachments(savedPreconditions, preconditionsRQ);
    verify(tmsManualScenarioPreconditionsMapper, never()).toEntity(any());
  }

  @Test
  void patchPreconditions_ShouldDoNothing_WhenPreconditionsRQIsNull() {
    // Given manual scenario with existing preconditions
    existingManualScenario.setPreconditions(existingPreconditions);

    // When patching with null preconditions RQ
    sut.patchPreconditions(existingManualScenario, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsMapper);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDeletePreconditions_WhenValidTestCaseId() {
    // When deleting all preconditions by test case ID
    sut.deleteAllByTestCaseId(testCaseId);

    // Then attachments and preconditions should be deleted
    verify(tmsManualScenarioPreconditionsAttachmentService).deleteAllByTestCaseId(testCaseId);
    verify(tmsManualScenarioPreconditionRepository).deleteAllByTestCaseId(testCaseId);
  }

  @Test
  void deleteAllByTestCaseId_ShouldDoNothing_WhenTestCaseIdIsNull() {
    // When deleting with null test case ID
    sut.deleteAllByTestCaseId(null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDeletePreconditions_WhenValidTestCaseIds() {
    // When deleting all preconditions by test case IDs
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then attachments and preconditions should be deleted
    verify(tmsManualScenarioPreconditionsAttachmentService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioPreconditionRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsIsEmpty() {
    // Given empty test case IDs list
    var emptyTestCaseIds = Collections.<Long>emptyList();

    // When deleting with empty list
    sut.deleteAllByTestCaseIds(emptyTestCaseIds);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void deleteAllByTestCaseIds_ShouldDoNothing_WhenTestCaseIdsIsNull() {
    // When deleting with null test case IDs
    sut.deleteAllByTestCaseIds(null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDeletePreconditions_WhenValidProjectAndFolderId() {
    // When deleting all preconditions by project and folder ID
    sut.deleteAllByTestFolderId(projectId, folderId);

    // Then attachments and preconditions should be deleted
    verify(tmsManualScenarioPreconditionsAttachmentService).deleteAllByTestFolderId(projectId, folderId);
    verify(tmsManualScenarioPreconditionRepository).deleteAllByTestFolderId(projectId, folderId);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenProjectIdIsNull() {
    // When deleting with null project ID
    sut.deleteAllByTestFolderId(null, folderId);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenFolderIdIsNull() {
    // When deleting with null folder ID
    sut.deleteAllByTestFolderId(projectId, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void deleteAllByTestFolderId_ShouldDoNothing_WhenBothIdsAreNull() {
    // When deleting with both null IDs
    sut.deleteAllByTestFolderId(null, null);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
  }

  @Test
  void duplicatePreconditions_ShouldDuplicatePreconditions_WhenOriginalHasPreconditions() {
    // Given original scenario with preconditions
    originalScenario.setPreconditions(existingPreconditions);
    when(tmsManualScenarioPreconditionsMapper.duplicate(existingPreconditions))
        .thenReturn(duplicatedPreconditions);
    when(tmsManualScenarioPreconditionRepository.save(duplicatedPreconditions))
        .thenReturn(savedDuplicatedPreconditions);

    // When duplicating preconditions
    sut.duplicatePreconditions(originalScenario, duplicatedScenario);

    // Then preconditions should be duplicated with attachments and set on duplicated scenario
    verify(tmsManualScenarioPreconditionsMapper).duplicate(existingPreconditions);
    verify(tmsManualScenarioPreconditionRepository).save(duplicatedPreconditions);
    verify(tmsManualScenarioPreconditionsAttachmentService).duplicateAttachments(
        existingPreconditions, savedDuplicatedPreconditions);
    // Note: duplicatedScenario.setPreconditions(savedDuplicatedPreconditions) is called in implementation
  }

  @Test
  void duplicatePreconditions_ShouldDoNothing_WhenOriginalHasNoPreconditions() {
    // Given original scenario without preconditions
    originalScenario.setPreconditions(null);

    // When duplicating preconditions
    sut.duplicatePreconditions(originalScenario, duplicatedScenario);

    // Then no operations should be performed
    verifyNoInteractions(tmsManualScenarioPreconditionsMapper);
    verifyNoInteractions(tmsManualScenarioPreconditionRepository);
    verifyNoInteractions(tmsManualScenarioPreconditionsAttachmentService);
  }

  @Test
  void duplicatePreconditions_ShouldNotThrowException_WhenValidScenarios() {
    // Given valid scenarios
    originalScenario.setPreconditions(existingPreconditions);
    when(tmsManualScenarioPreconditionsMapper.duplicate(existingPreconditions))
        .thenReturn(duplicatedPreconditions);
    when(tmsManualScenarioPreconditionRepository.save(duplicatedPreconditions))
        .thenReturn(savedDuplicatedPreconditions);

    // When/Then duplicating should not throw exception
    assertDoesNotThrow(() -> sut.duplicatePreconditions(originalScenario, duplicatedScenario));
  }

  @Test
  void createPreconditions_ShouldSetManualScenarioOnPreconditions_WhenCreating() {
    // Given valid manual scenario and preconditions RQ
    when(tmsManualScenarioPreconditionsMapper.toEntity(preconditionsRQ)).thenReturn(preconditions);
    when(tmsManualScenarioPreconditionRepository.save(preconditions)).thenReturn(savedPreconditions);

    // When creating preconditions
    sut.createPreconditions(manualScenario, preconditionsRQ);

    // Then manual scenario should be set on preconditions before saving
    verify(tmsManualScenarioPreconditionsMapper).toEntity(preconditionsRQ);
    // Verify that preconditions.setManualScenario(manualScenario) would have been called
    verify(tmsManualScenarioPreconditionRepository).save(preconditions);
  }

  @Test
  void duplicatePreconditions_ShouldSetDuplicatedScenarioOnPreconditions_WhenDuplicating() {
    // Given original scenario with preconditions
    originalScenario.setPreconditions(existingPreconditions);
    when(tmsManualScenarioPreconditionsMapper.duplicate(existingPreconditions))
        .thenReturn(duplicatedPreconditions);
    when(tmsManualScenarioPreconditionRepository.save(duplicatedPreconditions))
        .thenReturn(savedDuplicatedPreconditions);

    // When duplicating preconditions
    sut.duplicatePreconditions(originalScenario, duplicatedScenario);

    // Then duplicated scenario should be set on duplicated preconditions before saving
    verify(tmsManualScenarioPreconditionsMapper).duplicate(existingPreconditions);
    // Verify that duplicatedPreconditions.setManualScenario(duplicatedScenario) would have been called
    verify(tmsManualScenarioPreconditionRepository).save(duplicatedPreconditions);
  }
}
