package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsStep;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
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

  private TmsStepsManualScenario stepsManualScenario;
  private TmsStepsManualScenarioRQ stepsScenarioRQ;
  private Set<TmsStep> steps;

  @BeforeEach
  void setUp() {
    stepsManualScenario = createStepsManualScenario();
    stepsScenarioRQ = createStepsScenarioRQ();
    steps = createSteps();
  }

  @Test
  void shouldCreateSteps() {
    // Given
    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.createSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    assertThat(stepsManualScenario.getSteps()).isEqualTo(steps);
    for (var step : steps) {
      assertThat(step.getStepsManualScenario()).isEqualTo(stepsManualScenario);
    }
  }

  @Test
  void shouldNotCreateStepsWhenListIsEmpty() {
    // Given
    var emptyStepsRQ = TmsStepsManualScenarioRQ.builder()
        .steps(Collections.emptyList())
        .build();

    // When
    tmsStepService.createSteps(stepsManualScenario, emptyStepsRQ);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotCreateStepsWhenListIsNull() {
    // Given
    var nullStepsRQ = TmsStepsManualScenarioRQ.builder()
        .steps(null)
        .build();

    // When
    tmsStepService.createSteps(stepsManualScenario, nullStepsRQ);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldUpdateSteps() {
    // Given
    var existingSteps = createExistingSteps();
    stepsManualScenario.setSteps(existingSteps);

    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.updateSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepRepository).deleteAll(existingSteps);
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    assertThat(stepsManualScenario.getSteps()).isNotEqualTo(existingSteps);
    assertThat(stepsManualScenario.getSteps()).isEqualTo(steps);
  }

  @Test
  void shouldUpdateStepsWhenNoExistingSteps() {
    // Given
    stepsManualScenario.setSteps(new HashSet<>());

    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.updateSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepRepository, never()).deleteAll(any());
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    assertThat(stepsManualScenario.getSteps()).isEqualTo(steps);
  }

  @Test
  void shouldPatchSteps() {
    // Given
    var existingSteps = createExistingSteps();
    stepsManualScenario.setSteps(existingSteps);

    when(tmsStepMapper.convertToTmsSteps(stepsScenarioRQ)).thenReturn(steps);

    // When
    tmsStepService.patchSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsSteps(stepsScenarioRQ);
    verify(tmsStepRepository).saveAll(steps);

    // Verify existing steps are still there and new ones are added
    var allSteps = new HashSet<>(existingSteps);
    allSteps.addAll(steps);
    assertThat(stepsManualScenario.getSteps()).isEqualTo(allSteps);

    for (var step : steps) {
      assertThat(step.getStepsManualScenario()).isEqualTo(stepsManualScenario);
    }
  }

  @Test
  void shouldNotPatchStepsWhenRequestIsNull() {
    // When
    tmsStepService.patchSteps(stepsManualScenario, null);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotPatchStepsWhenStepsListIsEmpty() {
    // Given
    var emptyStepsRQ = TmsStepsManualScenarioRQ.builder()
        .steps(Collections.emptyList())
        .build();

    // When
    tmsStepService.patchSteps(stepsManualScenario, emptyStepsRQ);

    // Then
    verify(tmsStepMapper, never()).convertToTmsSteps(any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotPatchStepsWhenStepsListIsNull() {
    // Given
    var nullStepsRQ = TmsStepsManualScenarioRQ.builder()
        .steps(null)
        .build();

    // When
    tmsStepService.patchSteps(stepsManualScenario, nullStepsRQ);

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
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsStepService.deleteAllByTestCaseIds(null);

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
  private TmsStepsManualScenario createStepsManualScenario() {
    var scenario = new TmsStepsManualScenario();
    scenario.setManualScenarioId(1L);
    scenario.setSteps(new HashSet<>());
    return scenario;
  }

  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    var stepRQ1 = TmsManualScenarioStepRQ.builder()
        .instructions("Step 1 instructions")
        .expectedResult("Step 1 expected result")
        .attachments(Collections.emptyList())
        .build();

    var stepRQ2 = TmsManualScenarioStepRQ.builder()
        .instructions("Step 2 instructions")
        .expectedResult("Step 2 expected result")
        .attachments(Collections.emptyList())
        .build();

    return TmsStepsManualScenarioRQ.builder()
        .steps(Arrays.asList(stepRQ1, stepRQ2))
        .build();
  }

  private Set<TmsStep> createSteps() {
    var step1 = new TmsStep();
    step1.setId(1L);
    step1.setInstructions("Step 1 instructions");
    step1.setExpectedResult("Step 1 expected result");

    var step2 = new TmsStep();
    step2.setId(2L);
    step2.setInstructions("Step 2 instructions");
    step2.setExpectedResult("Step 2 expected result");

    return new HashSet<>(Arrays.asList(step1, step2));
  }

  private Set<TmsStep> createExistingSteps() {
    var step = new TmsStep();
    step.setId(99L);
    step.setInstructions("Existing step instructions");
    step.setExpectedResult("Existing step result");
    return new HashSet<>(Collections.singletonList(step));
  }
}
