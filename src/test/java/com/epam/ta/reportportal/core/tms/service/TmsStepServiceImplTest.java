package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStep;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsStepMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsStepServiceImplTest {

  @Mock
  private TmsStepMapper tmsStepMapper;

  @Mock
  private TmsStepRepository tmsStepRepository;

  @InjectMocks
  private TmsStepServiceImpl tmsStepService;

  private TmsManualScenario manualScenario;
  private TmsTextManualScenarioRQ textScenarioRQ;
  private TmsStepsManualScenarioRQ stepsScenarioRQ;
  private TmsStep step;
  private Set<TmsStep> steps;

  @BeforeEach
  void setUp() {
    manualScenario = createManualScenario();
    textScenarioRQ = createTextScenarioRQ();
    stepsScenarioRQ = createStepsScenarioRQ();
    step = createStep();
    steps = createSteps();
  }

  @Test
  void shouldCreateStep() {
    // Given
    when(tmsStepMapper.convertToTmsStep(textScenarioRQ)).thenReturn(step);

    // When
    tmsStepService.createStep(manualScenario, textScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(textScenarioRQ);
    verify(tmsStepRepository).save(step);

    assertThat(manualScenario.getSteps()).containsExactly(step);
    assertThat(step.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldCreateSteps() {
    // Given
    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.createSteps(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    assertThat(manualScenario.getSteps()).isEqualTo(steps);
    for (var step : steps) {
      assertThat(step.getManualScenario()).isEqualTo(manualScenario);
    }
  }

  @Test
  void shouldNotCreateStepsWhenListIsEmpty() {
    // Given
    var emptyStepsRQ = new TmsStepsManualScenarioRQ();
    emptyStepsRQ.setSteps(Collections.emptyList());

    // When
    tmsStepService.createSteps(manualScenario, emptyStepsRQ);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldUpdateStep() {
    // Given
    var existingSteps = createExistingSteps();
    manualScenario.setSteps(existingSteps);

    when(tmsStepMapper.convertToTmsStep(textScenarioRQ)).thenReturn(step);

    // When
    tmsStepService.updateStep(manualScenario, textScenarioRQ);

    // Then
    verify(tmsStepRepository).deleteAll(existingSteps);
    verify(tmsStepMapper).convertToTmsStep(textScenarioRQ);
    verify(tmsStepRepository).save(step);
  }

  @Test
  void shouldUpdateSteps() {
    // Given
    var existingSteps = createExistingSteps();
    manualScenario.setSteps(existingSteps);

    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.updateSteps(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepRepository).deleteAll(existingSteps);
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    assertThat(manualScenario.getSteps()).isNotEqualTo(existingSteps);
  }

  @Test
  void shouldPatchStepWhenExists() {
    // Given
    var existingStep = createExistingStep();
    manualScenario.setSteps(Collections.singleton(existingStep));

    when(tmsStepMapper.convertToTmsStep(textScenarioRQ)).thenReturn(step);

    // When
    tmsStepService.patchStep(manualScenario, textScenarioRQ);

    // Then
    verify(tmsStepMapper).patch(existingStep, step);
    verify(tmsStepRepository).save(existingStep);
  }

  @Test
  void shouldCreateStepWhenNotExistsForPatch() {
    // Given
    manualScenario.setSteps(new HashSet<>());
    when(tmsStepMapper.convertToTmsStep(textScenarioRQ)).thenReturn(step);

    // When
    tmsStepService.patchStep(manualScenario, textScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(textScenarioRQ);
    verify(tmsStepRepository).save(step);

    assertThat(manualScenario.getSteps()).containsExactly(step);
  }

  @Test
  void shouldNotPatchStepWhenRequestIsNull() {
    // When
    tmsStepService.patchStep(manualScenario, null);

    // Then
    verify(tmsStepMapper, never()).patch(any(), any());
    verify(tmsStepRepository, never()).save(any());
  }

  @Test
  void shouldPatchStepsUsingUpdateStrategy() {
    // Given
    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.patchSteps(manualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);
  }

  @Test
  void shouldNotPatchStepsWhenRequestIsNull() {
    // When
    tmsStepService.patchSteps(manualScenario, null);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    tmsStepService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsStepRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    tmsStepService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsStepRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsStepService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsStepRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsStepService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsStepRepository).deleteStepsByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setSteps(new HashSet<>());
    return scenario;
  }

  private TmsTextManualScenarioRQ createTextScenarioRQ() {
    var scenarioRQ = new TmsTextManualScenarioRQ();
    scenarioRQ.setExpectedResult("Expected result");
    return scenarioRQ;
  }

  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    var stepRQ1 = new TmsManualScenarioStepRQ();
    stepRQ1.setExpectedResult("Step 1 expected result");

    var stepRQ2 = new TmsManualScenarioStepRQ();
    stepRQ2.setExpectedResult("Step 2 expected result");

    var scenarioRQ = new TmsStepsManualScenarioRQ();
    scenarioRQ.setSteps(Arrays.asList(stepRQ1, stepRQ2));
    return scenarioRQ;
  }

  private TmsStep createStep() {
    var step = new TmsStep();
    step.setId(1L);
    step.setExpectedResult("Expected result");
    return step;
  }

  private Set<TmsStep> createSteps() {
    var step1 = new TmsStep();
    step1.setId(1L);

    var step2 = new TmsStep();
    step2.setId(2L);

    return new HashSet<>(Arrays.asList(step1, step2));
  }

  private Set<TmsStep> createExistingSteps() {
    var step = new TmsStep();
    step.setId(99L);
    return new HashSet<>(Collections.singletonList(step));
  }

  private TmsStep createExistingStep() {
    var step = new TmsStep();
    step.setId(99L);
    return step;
  }
}
