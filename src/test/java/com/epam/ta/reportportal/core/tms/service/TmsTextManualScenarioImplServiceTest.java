package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTextManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTextManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTextManualScenarioMapper;
import java.util.Collections;
import java.util.List;
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
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    textManualScenarioService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsTextManualScenarioRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    textManualScenarioService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsTextManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    textManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsTextManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    textManualScenarioService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsTextManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    textManualScenarioService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsTextManualScenarioRepository).deleteAllByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsTextManualScenarioRQ createTextScenarioRQ() {
    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(30)
        .linkToRequirements("http://requirements.com")
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .tags(Collections.emptyList())
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
}
