package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.tms.TmsAttachment;
import com.epam.ta.reportportal.entity.tms.TmsManualScenario;
import com.epam.ta.reportportal.entity.tms.TmsTextManualScenario;
import com.epam.ta.reportportal.dao.tms.TmsTextManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTextManualScenarioMapper;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTextManualScenarioImplServiceTest {

  @Mock
  private TmsTextManualScenarioMapper tmsTextManualScenarioMapper;

  @Mock
  private TmsTextManualScenarioRepository tmsTextManualScenarioRepository;

  @Mock
  private TmsTextManualScenarioAttachmentService tmsTextManualScenarioAttachmentService;

  @InjectMocks
  private TmsTextManualScenarioImplService textManualScenarioService;

  private TmsManualScenario manualScenario;
  private TmsTextManualScenarioRQ textScenarioRQ;
  private TmsTextManualScenario textManualScenario;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
    textScenarioRQ = createTextScenarioRQ();
    textManualScenario = createTextManualScenario();
  }

  @Test
  void shouldReturnCorrectManualScenarioType() {
    // When
    var type = textManualScenarioService.getTmsManualScenarioType();

    // Then
    assertThat(type).isEqualTo(TmsManualScenarioType.TEXT);
  }

  @Test
  void shouldCreateTmsManualScenarioImpl() {
    // Given
    when(tmsTextManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(textManualScenario);
    when(tmsTextManualScenarioRepository.save(textManualScenario))
        .thenReturn(textManualScenario);

    // When
    textManualScenarioService.createTmsManualScenarioImpl(manualScenario, textScenarioRQ);

    // Then
    verify(tmsTextManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsTextManualScenarioAttachmentService).createAttachments(textManualScenario, textScenarioRQ);
    verify(tmsTextManualScenarioRepository).save(textManualScenario);

    assertThat(manualScenario.getTextScenario()).isEqualTo(textManualScenario);
    assertThat(textManualScenario.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldUpdateTmsManualScenarioImplWhenTextScenarioExists() {
    // Given
    var existingTextManualScenario = createExistingTextManualScenario();
    manualScenario.setTextScenario(existingTextManualScenario);

    when(tmsTextManualScenarioRepository.save(existingTextManualScenario))
        .thenReturn(existingTextManualScenario);

    // When
    textManualScenarioService.updateTmsManualScenarioImpl(manualScenario, textScenarioRQ);

    // Then
    verify(tmsTextManualScenarioMapper).updateTmsManualScenario(existingTextManualScenario, textScenarioRQ);
    verify(tmsTextManualScenarioAttachmentService).updateAttachments(existingTextManualScenario, textScenarioRQ);
    verify(tmsTextManualScenarioRepository).save(existingTextManualScenario);
    verify(tmsTextManualScenarioMapper, never()).createTmsManualScenario(any());
  }

  @Test
  void shouldUpdateTmsManualScenarioImplWhenTextScenarioDoesNotExist() {
    // Given
    manualScenario.setTextScenario(null);

    when(tmsTextManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(textManualScenario);
    when(tmsTextManualScenarioRepository.save(textManualScenario))
        .thenReturn(textManualScenario);

    // When
    textManualScenarioService.updateTmsManualScenarioImpl(manualScenario, textScenarioRQ);

    // Then
    verify(tmsTextManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsTextManualScenarioRepository).save(textManualScenario);
    verify(tmsTextManualScenarioMapper, never()).updateTmsManualScenario(any(), any());
    verify(tmsTextManualScenarioAttachmentService, never()).updateAttachments(any(), any());

    assertThat(manualScenario.getTextScenario()).isEqualTo(textManualScenario);
    assertThat(textManualScenario.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldPatchTmsManualScenarioImpl() {
    // Given
    var existingTextManualScenario = createExistingTextManualScenario();
    manualScenario.setTextScenario(existingTextManualScenario);

    when(tmsTextManualScenarioRepository.save(existingTextManualScenario))
        .thenReturn(existingTextManualScenario);

    // When
    textManualScenarioService.patchTmsManualScenarioImpl(manualScenario, textScenarioRQ);

    // Then
    verify(tmsTextManualScenarioMapper).patchTmsManualScenario(existingTextManualScenario, textScenarioRQ);
    verify(tmsTextManualScenarioAttachmentService).patchAttachments(existingTextManualScenario, textScenarioRQ);
    verify(tmsTextManualScenarioRepository).save(existingTextManualScenario);
  }

  @Test
  void shouldThrowExceptionWhenPatchingWithNoTextManualScenario() {
    // Given
    manualScenario.setTextScenario(null);

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        textManualScenarioService.patchTmsManualScenarioImpl(manualScenario, textScenarioRQ));

    assertThat(exception.getMessage()).contains("Text Manual Scenario for Manual Scenario with id");
    verify(tmsTextManualScenarioRepository, never()).save(any());
    verify(tmsTextManualScenarioAttachmentService, never()).patchAttachments(any(), any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    textManualScenarioService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsTextManualScenarioAttachmentService).deleteAllByTestCaseId(123L);
    verify(tmsTextManualScenarioRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    textManualScenarioService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsTextManualScenarioAttachmentService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsTextManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    textManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsTextManualScenarioAttachmentService, never()).deleteAllByTestCaseIds(any());
    verify(tmsTextManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    textManualScenarioService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsTextManualScenarioAttachmentService, never()).deleteAllByTestCaseIds(any());
    verify(tmsTextManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    textManualScenarioService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsTextManualScenarioAttachmentService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsTextManualScenarioRepository).deleteAllByTestFolderId(1L, 123L);
  }

  @Test
  void shouldDuplicateManualScenarioImplWhenOriginalHasTextScenarioWithAttachments() {
    // Given
    var originalScenario = createManualScenario();
    var originalTextScenario = createExistingTextManualScenarioWithAttachments();
    originalScenario.setTextScenario(originalTextScenario);

    var newScenario = createManualScenario();
    newScenario.setId(2L);

    var duplicatedTextScenario = createTextManualScenario();
    duplicatedTextScenario.setManualScenarioId(2L);

    when(tmsTextManualScenarioMapper.duplicateTextScenario(newScenario, originalTextScenario))
        .thenReturn(duplicatedTextScenario);
    when(tmsTextManualScenarioRepository.save(duplicatedTextScenario))
        .thenReturn(duplicatedTextScenario);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, originalScenario);

    // Then
    verify(tmsTextManualScenarioMapper).duplicateTextScenario(newScenario, originalTextScenario);
    verify(tmsTextManualScenarioAttachmentService).duplicateAttachments(originalTextScenario, duplicatedTextScenario);
    verify(tmsTextManualScenarioRepository).save(duplicatedTextScenario);

    assertThat(newScenario.getTextScenario()).isEqualTo(duplicatedTextScenario);
  }

  @Test
  void shouldDuplicateManualScenarioImplWhenOriginalHasTextScenarioWithoutAttachments() {
    // Given
    var originalScenario = createManualScenario();
    var originalTextScenario = createExistingTextManualScenarioWithoutAttachments();
    originalScenario.setTextScenario(originalTextScenario);

    var newScenario = createManualScenario();
    newScenario.setId(2L);

    var duplicatedTextScenario = createTextManualScenario();
    duplicatedTextScenario.setManualScenarioId(2L);

    when(tmsTextManualScenarioMapper.duplicateTextScenario(newScenario, originalTextScenario))
        .thenReturn(duplicatedTextScenario);
    when(tmsTextManualScenarioRepository.save(duplicatedTextScenario))
        .thenReturn(duplicatedTextScenario);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, originalScenario);

    // Then
    verify(tmsTextManualScenarioMapper).duplicateTextScenario(newScenario, originalTextScenario);
    verify(tmsTextManualScenarioAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsTextManualScenarioRepository).save(duplicatedTextScenario);

    assertThat(newScenario.getTextScenario()).isEqualTo(duplicatedTextScenario);
  }

  @Test
  void shouldNotDuplicateManualScenarioImplWhenOriginalHasNoTextScenario() {
    // Given
    var originalScenario = createManualScenario();
    originalScenario.setTextScenario(null);

    var newScenario = createManualScenario();
    newScenario.setId(2L);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, originalScenario);

    // Then
    verify(tmsTextManualScenarioMapper, never()).duplicateTextScenario(any(), any());
    verify(tmsTextManualScenarioAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsTextManualScenarioRepository, never()).save(any());

    assertThat(newScenario.getTextScenario()).isNull();
  }

  @Test
  void shouldHandleDuplicateManualScenarioImplWithNullOriginalScenario() {
    // Given
    var newScenario = createManualScenario();
    newScenario.setId(2L);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, null);

    // Then
    verify(tmsTextManualScenarioMapper, never()).duplicateTextScenario(any(), any());
    verify(tmsTextManualScenarioAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsTextManualScenarioRepository, never()).save(any());

    assertThat(newScenario.getTextScenario()).isNull();
  }

  @Test
  void shouldDuplicateAndMaintainOriginalScenarioIntegrity() {
    // Given
    var originalScenario = createManualScenario();
    var originalTextScenario = createExistingTextManualScenarioWithAttachments();
    originalTextScenario.setInstructions("Original instructions");
    originalTextScenario.setExpectedResult("Original result");
    originalScenario.setTextScenario(originalTextScenario);

    var newScenario = createManualScenario();
    newScenario.setId(2L);

    var duplicatedTextScenario = createTextManualScenario();
    duplicatedTextScenario.setManualScenarioId(2L);
    duplicatedTextScenario.setInstructions("Duplicated instructions");
    duplicatedTextScenario.setExpectedResult("Duplicated result");

    when(tmsTextManualScenarioMapper.duplicateTextScenario(newScenario, originalTextScenario))
        .thenReturn(duplicatedTextScenario);
    when(tmsTextManualScenarioRepository.save(duplicatedTextScenario))
        .thenReturn(duplicatedTextScenario);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, originalScenario);

    // Then
    verify(tmsTextManualScenarioMapper).duplicateTextScenario(newScenario, originalTextScenario);
    verify(tmsTextManualScenarioAttachmentService).duplicateAttachments(originalTextScenario, duplicatedTextScenario);
    verify(tmsTextManualScenarioRepository).save(duplicatedTextScenario);

    // Original scenario should remain unchanged
    assertThat(originalScenario.getTextScenario()).isEqualTo(originalTextScenario);
    assertThat(originalTextScenario.getInstructions()).isEqualTo("Original instructions");
    assertThat(originalTextScenario.getExpectedResult()).isEqualTo("Original result");

    // New scenario should have the duplicated text scenario
    assertThat(newScenario.getTextScenario()).isEqualTo(duplicatedTextScenario);
    assertThat(duplicatedTextScenario.getInstructions()).isEqualTo("Duplicated instructions");
    assertThat(duplicatedTextScenario.getExpectedResult()).isEqualTo("Duplicated result");
  }

  @Test
  void shouldDuplicateManualScenarioWithNullAttachments() {
    // Given
    var originalScenario = createManualScenario();
    var originalTextScenario = createExistingTextManualScenario();
    originalTextScenario.setAttachments(null);
    originalScenario.setTextScenario(originalTextScenario);

    var newScenario = createManualScenario();
    newScenario.setId(2L);

    var duplicatedTextScenario = createTextManualScenario();
    duplicatedTextScenario.setManualScenarioId(2L);

    when(tmsTextManualScenarioMapper.duplicateTextScenario(newScenario, originalTextScenario))
        .thenReturn(duplicatedTextScenario);
    when(tmsTextManualScenarioRepository.save(duplicatedTextScenario))
        .thenReturn(duplicatedTextScenario);

    // When
    textManualScenarioService.duplicateManualScenarioImpl(newScenario, originalScenario);

    // Then
    verify(tmsTextManualScenarioMapper).duplicateTextScenario(newScenario, originalTextScenario);
    verify(tmsTextManualScenarioAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsTextManualScenarioRepository).save(duplicatedTextScenario);

    assertThat(newScenario.getTextScenario()).isEqualTo(duplicatedTextScenario);
  }

  // Helper methods
  private TmsTextManualScenarioRQ createTextScenarioRQ() {
    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(30)
        .linkToRequirements("http://requirements.com")
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .attributes(Collections.emptyList())
        .build();
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    return scenario;
  }

  private TmsTextManualScenario createTextManualScenario() {
    var textScenario = new TmsTextManualScenario();
    textScenario.setManualScenarioId(1L);
    textScenario.setInstructions("Test instructions");
    textScenario.setExpectedResult("Expected result");
    return textScenario;
  }

  private TmsTextManualScenario createExistingTextManualScenario() {
    var textScenario = new TmsTextManualScenario();
    textScenario.setManualScenarioId(99L);
    textScenario.setInstructions("Existing instructions");
    textScenario.setExpectedResult("Existing result");
    return textScenario;
  }

  private TmsTextManualScenario createExistingTextManualScenarioWithAttachments() {
    var textScenario = createExistingTextManualScenario();
    textScenario.setAttachments(Set.of(createAttachment(1L), createAttachment(2L)));
    return textScenario;
  }

  private TmsTextManualScenario createExistingTextManualScenarioWithoutAttachments() {
    var textScenario = createExistingTextManualScenario();
    textScenario.setAttachments(Collections.emptySet());
    return textScenario;
  }

  private TmsAttachment createAttachment(Long id) {
    var attachment = new TmsAttachment();
    attachment.setId(id);
    return attachment;
  }
}
