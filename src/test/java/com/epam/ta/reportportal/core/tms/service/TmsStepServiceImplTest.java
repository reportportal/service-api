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
import java.util.Collection;
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
  private TmsStepsManualScenario newStepsScenario;
  private Collection<TmsStep> originalSteps;
  private Set<TmsStep> duplicatedSteps;

  @BeforeEach
  void setUp() {
    stepsManualScenario = createStepsManualScenario();
    stepsScenarioRQ = createStepsScenarioRQ();
    steps = createSteps();
    newStepsScenario = createNewStepsScenario();
    originalSteps = createOriginalSteps();
    duplicatedSteps = createDuplicatedSteps();
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

  @Test
  void shouldDuplicateSteps() {
    // Given
    TmsStep originalStep1 = originalSteps.iterator().next();
    TmsStep duplicatedStep1 = duplicatedSteps.iterator().next();

    // Setup mapper to return duplicated steps for each original step
    originalSteps.forEach(originalStep -> {
      TmsStep duplicatedStep = duplicatedSteps.stream()
          .filter(dup -> dup.getInstructions().equals(originalStep.getInstructions().replace("Original", "Duplicated")))
          .findFirst()
          .orElse(null);
      when(tmsStepMapper.duplicateStep(originalStep, newStepsScenario))
          .thenReturn(duplicatedStep);
    });

    // When
    tmsStepService.duplicateSteps(originalSteps, newStepsScenario);

    // Then
    verify(tmsStepRepository).saveAll(duplicatedSteps);
    assertThat(newStepsScenario.getSteps()).isEqualTo(duplicatedSteps);

    // Verify that duplicateStep was called for each original step
    originalSteps.forEach(originalStep -> {
      verify(tmsStepMapper).duplicateStep(originalStep, newStepsScenario);
    });
  }

  @Test
  void shouldNotDuplicateStepsWhenOriginalStepsIsEmpty() {
    // Given
    Collection<TmsStep> emptySteps = Collections.emptyList();

    // When
    tmsStepService.duplicateSteps(emptySteps, newStepsScenario);

    // Then
    verify(tmsStepMapper, never()).duplicateStep(any(), any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotDuplicateStepsWhenOriginalStepsIsNull() {
    // When
    tmsStepService.duplicateSteps(null, newStepsScenario);

    // Then
    verify(tmsStepMapper, never()).duplicateStep(any(), any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldDuplicateSingleStep() {
    // Given
    TmsStep singleOriginalStep = createSingleOriginalStep();
    TmsStep singleDuplicatedStep = createSingleDuplicatedStep();
    Collection<TmsStep> singleStepCollection = Collections.singletonList(singleOriginalStep);
    Set<TmsStep> singleDuplicatedStepSet = Collections.singleton(singleDuplicatedStep);

    when(tmsStepMapper.duplicateStep(singleOriginalStep, newStepsScenario))
        .thenReturn(singleDuplicatedStep);

    // When
    tmsStepService.duplicateSteps(singleStepCollection, newStepsScenario);

    // Then
    verify(tmsStepMapper).duplicateStep(singleOriginalStep, newStepsScenario);
    verify(tmsStepRepository).saveAll(singleDuplicatedStepSet);
    assertThat(newStepsScenario.getSteps()).isEqualTo(singleDuplicatedStepSet);
  }

  @Test
  void shouldDuplicateMultipleSteps() {
    // Given
    Collection<TmsStep> multipleOriginalSteps = createMultipleOriginalSteps();
    Set<TmsStep> multipleDuplicatedSteps = createMultipleDuplicatedSteps();

    // Setup mapper mocks for each original step
    var originalStepsList = List.copyOf(multipleOriginalSteps);
    var duplicatedStepsList = List.copyOf(multipleDuplicatedSteps);

    when(tmsStepMapper.duplicateStep(originalStepsList.get(0), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(0));
    when(tmsStepMapper.duplicateStep(originalStepsList.get(1), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(1));
    when(tmsStepMapper.duplicateStep(originalStepsList.get(2), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(2));

    // When
    tmsStepService.duplicateSteps(multipleOriginalSteps, newStepsScenario);

    // Then
    multipleOriginalSteps.forEach(originalStep -> {
      verify(tmsStepMapper).duplicateStep(originalStep, newStepsScenario);
    });
    verify(tmsStepRepository).saveAll(multipleDuplicatedSteps);
    assertThat(newStepsScenario.getSteps()).isEqualTo(multipleDuplicatedSteps);
  }

  // Helper methods
  private TmsStepsManualScenario createStepsManualScenario() {
    var scenario = new TmsStepsManualScenario();
    scenario.setManualScenarioId(1L);
    scenario.setSteps(new HashSet<>());
    return scenario;
  }

  private TmsStepsManualScenario createNewStepsScenario() {
    var scenario = new TmsStepsManualScenario();
    scenario.setManualScenarioId(2L);
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

  private Collection<TmsStep> createOriginalSteps() {
    var step1 = new TmsStep();
    step1.setId(10L);
    step1.setInstructions("Original step 1 instructions");
    step1.setExpectedResult("Original step 1 expected result");

    var step2 = new TmsStep();
    step2.setId(11L);
    step2.setInstructions("Original step 2 instructions");
    step2.setExpectedResult("Original step 2 expected result");

    return Arrays.asList(step1, step2);
  }

  private Set<TmsStep> createDuplicatedSteps() {
    var step1 = new TmsStep();
    step1.setId(20L);
    step1.setInstructions("Duplicated step 1 instructions");
    step1.setExpectedResult("Duplicated step 1 expected result");

    var step2 = new TmsStep();
    step2.setId(21L);
    step2.setInstructions("Duplicated step 2 instructions");
    step2.setExpectedResult("Duplicated step 2 expected result");

    return new HashSet<>(Arrays.asList(step1, step2));
  }

  private TmsStep createSingleOriginalStep() {
    var step = new TmsStep();
    step.setId(30L);
    step.setInstructions("Single original step instructions");
    step.setExpectedResult("Single original step expected result");
    return step;
  }

  private TmsStep createSingleDuplicatedStep() {
    var step = new TmsStep();
    step.setId(31L);
    step.setInstructions("Single duplicated step instructions");
    step.setExpectedResult("Single duplicated step expected result");
    return step;
  }

  private Collection<TmsStep> createMultipleOriginalSteps() {
    var step1 = new TmsStep();
    step1.setId(40L);
    step1.setInstructions("Multiple original step 1 instructions");
    step1.setExpectedResult("Multiple original step 1 expected result");

    var step2 = new TmsStep();
    step2.setId(41L);
    step2.setInstructions("Multiple original step 2 instructions");
    step2.setExpectedResult("Multiple original step 2 expected result");

    var step3 = new TmsStep();
    step3.setId(42L);
    step3.setInstructions("Multiple original step 3 instructions");
    step3.setExpectedResult("Multiple original step 3 expected result");

    return Arrays.asList(step1, step2, step3);
  }

  private Set<TmsStep> createMultipleDuplicatedSteps() {
    var step1 = new TmsStep();
    step1.setId(50L);
    step1.setInstructions("Multiple duplicated step 1 instructions");
    step1.setExpectedResult("Multiple duplicated step 1 expected result");

    var step2 = new TmsStep();
    step2.setId(51L);
    step2.setInstructions("Multiple duplicated step 2 instructions");
    step2.setExpectedResult("Multiple duplicated step 2 expected result");

    var step3 = new TmsStep();
    step3.setId(52L);
    step3.setInstructions("Multiple duplicated step 3 instructions");
    step3.setExpectedResult("Multiple duplicated step 3 expected result");

    return new HashSet<>(Arrays.asList(step1, step2, step3));
  }
}
