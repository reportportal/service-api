package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsStepsManualScenarioMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsStepsManualScenarioImplServiceTest {

  @Mock
  private TmsStepService tmsStepService;

  @Mock
  private TmsStepsManualScenarioMapper tmsStepsManualScenarioMapper;

  @Mock
  private TmsStepsManualScenarioRepository tmsStepsManualScenarioRepository;

  @InjectMocks
  private TmsStepsManualScenarioImplService stepsManualScenarioService;

  private TmsManualScenario manualScenario;
  private TmsStepsManualScenarioRQ stepsScenarioRQ;
  private TmsStepsManualScenario stepsManualScenario;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
    stepsScenarioRQ = createStepsScenarioRQ();
    stepsManualScenario = createStepsManualScenario();
  }

  @Test
  void shouldReturnCorrectManualScenarioType() {
    // When
    var type = stepsManualScenarioService.getTmsManualScenarioType();

    // Then
    assertThat(type).isEqualTo(TmsManualScenarioType.STEPS);
  }

  @Test
  void shouldCreateTmsManualScenarioImpl() {
    // Given
    when(tmsStepsManualScenarioMapper.createTmsStepsManualScenario())
        .thenReturn(stepsManualScenario);
    when(tmsStepsManualScenarioRepository.save(stepsManualScenario))
        .thenReturn(stepsManualScenario);

    // When
    stepsManualScenarioService.createTmsManualScenarioImpl(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepsManualScenarioMapper).createTmsStepsManualScenario();
    verify(tmsStepService).createSteps(stepsManualScenario, stepsScenarioRQ);
    verify(tmsStepsManualScenarioRepository).save(stepsManualScenario);

    assertThat(manualScenario.getStepsScenario()).isEqualTo(stepsManualScenario);
    assertThat(stepsManualScenario.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldUpdateTmsManualScenarioImplWhenStepsScenarioExists() {
    // Given
    var existingStepsManualScenario = createExistingStepsManualScenario();
    manualScenario.setStepsScenario(existingStepsManualScenario);

    when(tmsStepsManualScenarioRepository.save(existingStepsManualScenario))
        .thenReturn(existingStepsManualScenario);

    // When
    stepsManualScenarioService.updateTmsManualScenarioImpl(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepService).updateSteps(existingStepsManualScenario, stepsScenarioRQ);
    verify(tmsStepsManualScenarioRepository).save(existingStepsManualScenario);
    verify(tmsStepsManualScenarioMapper, never()).createTmsStepsManualScenario();
    verify(tmsStepService, never()).createSteps(any(), any());
  }

  @Test
  void shouldUpdateTmsManualScenarioImplWhenStepsScenarioDoesNotExist() {
    // Given
    manualScenario.setStepsScenario(null);

    when(tmsStepsManualScenarioMapper.createTmsStepsManualScenario())
        .thenReturn(stepsManualScenario);
    when(tmsStepsManualScenarioRepository.save(stepsManualScenario))
        .thenReturn(stepsManualScenario);

    // When
    stepsManualScenarioService.updateTmsManualScenarioImpl(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepsManualScenarioMapper).createTmsStepsManualScenario();
    verify(tmsStepService).createSteps(stepsManualScenario, stepsScenarioRQ);
    verify(tmsStepsManualScenarioRepository).save(stepsManualScenario);
    verify(tmsStepService, never()).updateSteps(any(), any());

    assertThat(manualScenario.getStepsScenario()).isEqualTo(stepsManualScenario);
    assertThat(stepsManualScenario.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldPatchTmsManualScenarioImpl() {
    // Given
    var existingStepsManualScenario = createExistingStepsManualScenario();
    manualScenario.setStepsScenario(existingStepsManualScenario);

    when(tmsStepsManualScenarioRepository.save(existingStepsManualScenario))
        .thenReturn(existingStepsManualScenario);

    // When
    stepsManualScenarioService.patchTmsManualScenarioImpl(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepService).patchSteps(existingStepsManualScenario, stepsScenarioRQ);
    verify(tmsStepsManualScenarioRepository).save(existingStepsManualScenario);
  }

  @Test
  void shouldThrowExceptionWhenPatchingWithNoStepsManualScenario() {
    // Given
    manualScenario.setStepsScenario(null);

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        stepsManualScenarioService.patchTmsManualScenarioImpl(manualScenario, stepsScenarioRQ));

    assertThat(exception.getMessage()).contains("Steps Manual Scenario for Manual Scenario with id");
    verify(tmsStepsManualScenarioRepository, never()).save(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    stepsManualScenarioService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsStepService).deleteAllByTestCaseId(123L);
    verify(tmsStepsManualScenarioRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    stepsManualScenarioService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsStepService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsStepsManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    stepsManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsStepService, never()).deleteAllByTestCaseIds(any());
    verify(tmsStepsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    stepsManualScenarioService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsStepService, never()).deleteAllByTestCaseIds(any());
    verify(tmsStepsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    stepsManualScenarioService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsStepService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsStepsManualScenarioRepository).deleteAllByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    return TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(30)
        .attributes(Collections.emptyList())
        .steps(Collections.emptyList())
        .build();
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    return scenario;
  }

  private TmsStepsManualScenario createStepsManualScenario() {
    var stepsScenario = new TmsStepsManualScenario();
    stepsScenario.setManualScenarioId(1L);
    return stepsScenario;
  }

  private TmsStepsManualScenario createExistingStepsManualScenario() {
    var stepsScenario = new TmsStepsManualScenario();
    stepsScenario.setManualScenarioId(99L);
    return stepsScenario;
  }
}
