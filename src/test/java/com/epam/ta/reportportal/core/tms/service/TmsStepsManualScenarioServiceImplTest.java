package com.epam.ta.reportportal.core.tms.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsStepsManualScenarioServiceImplTest {

  @Mock
  private TmsManualScenarioRepository tmsManualScenarioRepository;

  @Mock
  private TmsManualScenarioAttributeService tmsManualScenarioAttributeService;

  @Mock
  private TmsManualScenarioMapper tmsManualScenarioMapper;

  @Mock
  private TmsStepService tmsStepService;

  @InjectMocks
  private TmsStepsManualScenarioServiceImpl stepsManualScenarioService;

  private TmsTestCaseVersion testCaseVersion;
  private TmsStepsManualScenarioRQ stepsScenarioRQ;
  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    testCaseVersion = createTestCaseVersion();
    stepsScenarioRQ = createStepsScenarioRQ();
    manualScenario = createManualScenario();
  }

  @Test
  void shouldReturnCorrectManualScenarioType() {
    // When
    var type = stepsManualScenarioService.getTmsManualScenarioType();

    // Then
    assertThat(type).isEqualTo(TmsManualScenarioType.STEPS);
  }

  @Test
  void shouldCreateTmsManualScenario() {
    // Given
    when(tmsManualScenarioMapper.createTmsManualScenario(stepsScenarioRQ)).thenReturn(manualScenario);
    when(tmsManualScenarioRepository.save(manualScenario)).thenReturn(manualScenario);

    // When
    var result = stepsManualScenarioService.createTmsManualScenario(testCaseVersion, stepsScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(stepsScenarioRQ);
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsStepService).createSteps(manualScenario, stepsScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);

    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldUpdateTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(stepsScenarioRQ)).thenReturn(manualScenario);
    when(tmsManualScenarioRepository.save(existingManualScenario)).thenReturn(existingManualScenario);

    // When
    var result = stepsManualScenarioService.updateTmsManualScenario(testCaseVersion, stepsScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).update(existingManualScenario, manualScenario);
    verify(tmsManualScenarioAttributeService).updateAttributes(eq(existingManualScenario), any());
    verify(tmsStepService).updateSteps(existingManualScenario, stepsScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldPatchTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(stepsScenarioRQ)).thenReturn(manualScenario);
    when(tmsManualScenarioRepository.save(existingManualScenario)).thenReturn(existingManualScenario);

    // When
    var result = stepsManualScenarioService.patchTmsManualScenario(testCaseVersion, stepsScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).patch(existingManualScenario, manualScenario);
    verify(tmsManualScenarioAttributeService).patchAttributes(eq(existingManualScenario), any());
    verify(tmsStepService).patchSteps(existingManualScenario, stepsScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    stepsManualScenarioService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsStepService).deleteAllByTestCaseId(123L);
    verify(tmsManualScenarioAttributeService).deleteAllByTestCaseId(123L);
    verify(tmsManualScenarioRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    stepsManualScenarioService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsStepService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioAttributeService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    stepsManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsStepService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioAttributeService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    stepsManualScenarioService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsStepService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioAttributeService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioRepository).deleteManualScenariosByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsTestCaseVersion createTestCaseVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(1L);
    version.setName("Test Version");
    return version;
  }

  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    var scenarioRQ = new TmsStepsManualScenarioRQ();
    scenarioRQ.setManualScenarioType(TmsManualScenarioType.STEPS);
    scenarioRQ.setAttributes(Collections.emptyList());
    return scenarioRQ;
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    return scenario;
  }

  private TmsManualScenario createExistingManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(99L);
    return scenario;
  }
}
