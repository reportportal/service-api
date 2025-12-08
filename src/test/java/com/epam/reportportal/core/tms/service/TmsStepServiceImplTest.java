package com.epam.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttachment;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStepsManualScenario;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsStepRepository;
import com.epam.reportportal.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.core.tms.mapper.TmsStepMapper;
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

  @Mock
  private TmsStepAttachmentService tmsStepAttachmentService;

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
    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.createSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepRepository).save(step1);
    verify(tmsStepRepository).save(step2);
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));

    // Verify that manual scenario's steps set is initialized and contains created steps
    assertThat(stepsManualScenario.getSteps()).isNotNull();
    assertThat(stepsManualScenario.getSteps()).containsExactlyInAnyOrder(step1, step2);

    // Verify that each step has reference to manual scenario
    assertThat(step1.getStepsManualScenario()).isEqualTo(stepsManualScenario);
    assertThat(step2.getStepsManualScenario()).isEqualTo(stepsManualScenario);
  }

  @Test
  void shouldCreateStepsWhenManualScenarioStepsIsNull() {
    // Given
    stepsManualScenario.setSteps(null);
    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.createSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepRepository).save(step1);
    verify(tmsStepRepository).save(step2);
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));

    // Verify that manual scenario's steps set is initialized and contains created steps
    assertThat(stepsManualScenario.getSteps()).isNotNull();
    assertThat(stepsManualScenario.getSteps()).containsExactlyInAnyOrder(step1, step2);
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
    verify(tmsStepMapper, never()).convertToTmsStep(any());
    verify(tmsStepAttachmentService, never()).createAttachments(any(), any());
    verify(tmsStepRepository, never()).save(any());
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
    verify(tmsStepMapper, never()).convertToTmsStep(any());
    verify(tmsStepAttachmentService, never()).createAttachments(any(), any());
    verify(tmsStepRepository, never()).save(any());
  }

  @Test
  void shouldCreateSingleStep() {
    // Given
    var singleStepRQ = TmsStepRQ.builder()
        .instructions("Single step instructions")
        .expectedResult("Single step expected result")
        .attachments(Collections.emptyList())
        .build();

    var singleStepScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .steps(Collections.singletonList(singleStepRQ))
        .build();

    var singleStep = new TmsStep();
    singleStep.setId(1L);
    singleStep.setInstructions("Single step instructions");

    when(tmsStepMapper.convertToTmsStep(singleStepRQ)).thenReturn(singleStep);

    // When
    tmsStepService.createSteps(stepsManualScenario, singleStepScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(singleStepRQ);
    verify(tmsStepRepository).save(singleStep);
    verify(tmsStepAttachmentService).createAttachments(singleStep, singleStepRQ);

    assertThat(stepsManualScenario.getSteps()).containsExactly(singleStep);
    assertThat(singleStep.getStepsManualScenario()).isEqualTo(stepsManualScenario);
  }

  @Test
  void shouldUpdateSteps() {
    // Given
    var existingSteps = createExistingSteps();
    stepsManualScenario.setSteps(existingSteps);

    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.updateSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepAttachmentService).deleteAllBySteps(existingSteps);
    verify(tmsStepRepository).deleteAll(existingSteps);
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepRepository).save(step1);
    verify(tmsStepRepository).save(step2);
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));

    assertThat(stepsManualScenario.getSteps()).isNotEqualTo(existingSteps);
    assertThat(stepsManualScenario.getSteps()).containsExactlyInAnyOrder(step1, step2);
  }

  @Test
  void shouldUpdateStepsWhenNoExistingSteps() {
    // Given
    stepsManualScenario.setSteps(new HashSet<>());

    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.updateSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepAttachmentService, never()).deleteAllBySteps(any());
    verify(tmsStepRepository, never()).deleteAll(any());
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepRepository).save(step1);
    verify(tmsStepRepository).save(step2);
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));

    assertThat(stepsManualScenario.getSteps()).containsExactlyInAnyOrder(step1, step2);
  }

  @Test
  void shouldPatchSteps() {
    // Given
    var existingSteps = createExistingSteps();
    stepsManualScenario.setSteps(existingSteps);

    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.patchSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));
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
  void shouldPatchStepsWhenExistingStepsIsNull() {
    // Given
    stepsManualScenario.setSteps(null);

    var stepRQList = stepsScenarioRQ.getSteps();
    var step1 = steps.stream().findFirst().orElseThrow();
    var step2 = steps.stream().skip(1).findFirst().orElseThrow();

    when(tmsStepMapper.convertToTmsStep(stepRQList.get(0))).thenReturn(step1);
    when(tmsStepMapper.convertToTmsStep(stepRQList.get(1))).thenReturn(step2);

    // When
    tmsStepService.patchSteps(stepsManualScenario, stepsScenarioRQ);

    // Then
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(0));
    verify(tmsStepMapper).convertToTmsStep(stepRQList.get(1));
    verify(tmsStepAttachmentService).createAttachments(step1, stepRQList.get(0));
    verify(tmsStepAttachmentService).createAttachments(step2, stepRQList.get(1));
    verify(tmsStepRepository).saveAll(steps);

    for (var step : steps) {
      assertThat(step.getStepsManualScenario()).isEqualTo(stepsManualScenario);
    }
  }

  @Test
  void shouldNotPatchStepsWhenRequestIsNull() {
    // When
    tmsStepService.patchSteps(stepsManualScenario, null);

    // Then
    verify(tmsStepMapper, never()).convertToTmsStep(any());
    verify(tmsStepAttachmentService, never()).createAttachments(any(), any());
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
    verify(tmsStepMapper, never()).convertToTmsStep(any());
    verify(tmsStepAttachmentService, never()).createAttachments(any(), any());
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
    verify(tmsStepMapper, never()).convertToTmsStep(any());
    verify(tmsStepAttachmentService, never()).createAttachments(any(), any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    tmsStepService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsStepAttachmentService).deleteAllByTestCaseId(123L);
    verify(tmsStepRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    tmsStepService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsStepAttachmentService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsStepRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsStepService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsStepAttachmentService, never()).deleteAllByTestCaseIds(any());
    verify(tmsStepRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsStepService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsStepAttachmentService, never()).deleteAllByTestCaseIds(any());
    verify(tmsStepRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsStepService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsStepAttachmentService).deleteStepsByTestFolderId(1L, 123L);
    verify(tmsStepRepository).deleteStepsByTestFolderId(1L, 123L);
  }

  @Test
  void shouldDuplicateStepsWithAttachments() {
    // Given
    var originalStepsWithAttachments = createOriginalStepsWithAttachments();
    var duplicatedStepsWithAttachments = createDuplicatedStepsWithAttachments();

    var originalStepsList = List.copyOf(originalStepsWithAttachments);
    var duplicatedStepsList = List.copyOf(duplicatedStepsWithAttachments);

    when(tmsStepMapper.duplicateStep(originalStepsList.get(0), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(0));
    when(tmsStepMapper.duplicateStep(originalStepsList.get(1), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(1));

    // When
    tmsStepService.duplicateSteps(originalStepsWithAttachments, newStepsScenario);

    // Then
    verify(tmsStepRepository).saveAll(duplicatedStepsWithAttachments);
    assertThat(newStepsScenario.getSteps()).isEqualTo(duplicatedStepsWithAttachments);

    // Verify that duplicateStep was called for each original step
    originalStepsWithAttachments.forEach(originalStep -> {
      verify(tmsStepMapper).duplicateStep(originalStep, newStepsScenario);
    });

    // Verify that duplicateAttachments was called for steps with attachments
    originalStepsWithAttachments.forEach(
        originalStep -> verify(tmsStepAttachmentService).duplicateAttachments(eq(originalStep),
            any()));
  }

  @Test
  void shouldDuplicateStepsWithoutAttachments() {
    // Given
    var originalStepsWithoutAttachments = createOriginalStepsWithoutAttachments();
    var duplicatedStepsWithoutAttachments = createDuplicatedStepsWithoutAttachments();

    var originalStepsList = List.copyOf(originalStepsWithoutAttachments);
    var duplicatedStepsList = List.copyOf(duplicatedStepsWithoutAttachments);

    when(tmsStepMapper.duplicateStep(originalStepsList.get(0), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(0));
    when(tmsStepMapper.duplicateStep(originalStepsList.get(1), newStepsScenario))
        .thenReturn(duplicatedStepsList.get(1));

    // When
    tmsStepService.duplicateSteps(originalStepsWithoutAttachments, newStepsScenario);

    // Then
    verify(tmsStepRepository).saveAll(duplicatedStepsWithoutAttachments);
    assertThat(newStepsScenario.getSteps()).isEqualTo(duplicatedStepsWithoutAttachments);

    // Verify that duplicateStep was called for each original step
    originalStepsWithoutAttachments.forEach(originalStep -> {
      verify(tmsStepMapper).duplicateStep(originalStep, newStepsScenario);
    });

    // Verify that duplicateAttachments was NOT called for steps without attachments
    verify(tmsStepAttachmentService, never()).duplicateAttachments(any(), any());
  }

  @Test
  void shouldNotDuplicateStepsWhenOriginalStepsIsEmpty() {
    // Given
    Collection<TmsStep> emptySteps = Collections.emptyList();

    // When
    tmsStepService.duplicateSteps(emptySteps, newStepsScenario);

    // Then
    verify(tmsStepMapper, never()).duplicateStep(any(), any());
    verify(tmsStepAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldNotDuplicateStepsWhenOriginalStepsIsNull() {
    // When
    tmsStepService.duplicateSteps(null, newStepsScenario);

    // Then
    verify(tmsStepMapper, never()).duplicateStep(any(), any());
    verify(tmsStepAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsStepRepository, never()).saveAll(any());
  }

  @Test
  void shouldDuplicateSingleStepWithAttachments() {
    // Given
    var singleOriginalStep = createSingleOriginalStepWithAttachments();
    var singleDuplicatedStep = createSingleDuplicatedStepWithAttachments();
    Collection<TmsStep> singleStepCollection = Collections.singletonList(singleOriginalStep);
    Set<TmsStep> singleDuplicatedStepSet = Collections.singleton(singleDuplicatedStep);

    when(tmsStepMapper.duplicateStep(singleOriginalStep, newStepsScenario))
        .thenReturn(singleDuplicatedStep);

    // When
    tmsStepService.duplicateSteps(singleStepCollection, newStepsScenario);

    // Then
    verify(tmsStepMapper).duplicateStep(singleOriginalStep, newStepsScenario);
    verify(tmsStepAttachmentService).duplicateAttachments(singleOriginalStep, singleDuplicatedStep);
    verify(tmsStepRepository).saveAll(singleDuplicatedStepSet);
    assertThat(newStepsScenario.getSteps()).isEqualTo(singleDuplicatedStepSet);
  }

  @Test
  void shouldDuplicateSingleStepWithoutAttachments() {
    // Given
    var singleOriginalStep = createSingleOriginalStepWithoutAttachments();
    var singleDuplicatedStep = createSingleDuplicatedStepWithoutAttachments();
    Collection<TmsStep> singleStepCollection = Collections.singletonList(singleOriginalStep);
    Set<TmsStep> singleDuplicatedStepSet = Collections.singleton(singleDuplicatedStep);

    when(tmsStepMapper.duplicateStep(singleOriginalStep, newStepsScenario))
        .thenReturn(singleDuplicatedStep);

    // When
    tmsStepService.duplicateSteps(singleStepCollection, newStepsScenario);

    // Then
    verify(tmsStepMapper).duplicateStep(singleOriginalStep, newStepsScenario);
    verify(tmsStepAttachmentService, never()).duplicateAttachments(any(), any());
    verify(tmsStepRepository).saveAll(singleDuplicatedStepSet);
    assertThat(newStepsScenario.getSteps()).isEqualTo(singleDuplicatedStepSet);
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
    var stepRQ1 = TmsStepRQ.builder()
        .instructions("Step 1 instructions")
        .expectedResult("Step 1 expected result")
        .attachments(Collections.emptyList())
        .build();

    var stepRQ2 = TmsStepRQ.builder()
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

  private Collection<TmsStep> createOriginalStepsWithAttachments() {
    var step1 = new TmsStep();
    step1.setId(10L);
    step1.setInstructions("Original step 1 instructions");
    step1.setExpectedResult("Original step 1 expected result");
    step1.setAttachments(Set.of(createAttachment(1L), createAttachment(2L)));

    var step2 = new TmsStep();
    step2.setId(11L);
    step2.setInstructions("Original step 2 instructions");
    step2.setExpectedResult("Original step 2 expected result");
    step2.setAttachments(Set.of(createAttachment(3L)));

    return Arrays.asList(step1, step2);
  }

  private Set<TmsStep> createDuplicatedStepsWithAttachments() {
    var step1 = new TmsStep();
    step1.setId(20L);
    step1.setInstructions("Duplicated step 1 instructions");
    step1.setExpectedResult("Duplicated step 1 expected result");
    step1.setAttachments(Set.of(createAttachment(4L), createAttachment(5L)));

    var step2 = new TmsStep();
    step2.setId(21L);
    step2.setInstructions("Duplicated step 2 instructions");
    step2.setExpectedResult("Duplicated step 2 expected result");
    step2.setAttachments(Set.of(createAttachment(6L)));

    return new HashSet<>(Arrays.asList(step1, step2));
  }

  private Collection<TmsStep> createOriginalStepsWithoutAttachments() {
    var step1 = new TmsStep();
    step1.setId(10L);
    step1.setInstructions("Original step 1 instructions");
    step1.setExpectedResult("Original step 1 expected result");
    step1.setAttachments(Collections.emptySet());

    var step2 = new TmsStep();
    step2.setId(11L);
    step2.setInstructions("Original step 2 instructions");
    step2.setExpectedResult("Original step 2 expected result");
    step2.setAttachments(null);

    return Arrays.asList(step1, step2);
  }

  private Set<TmsStep> createDuplicatedStepsWithoutAttachments() {
    var step1 = new TmsStep();
    step1.setId(20L);
    step1.setInstructions("Duplicated step 1 instructions");
    step1.setExpectedResult("Duplicated step 1 expected result");
    step1.setAttachments(Collections.emptySet());

    var step2 = new TmsStep();
    step2.setId(21L);
    step2.setInstructions("Duplicated step 2 instructions");
    step2.setExpectedResult("Duplicated step 2 expected result");
    step2.setAttachments(null);

    return new HashSet<>(Arrays.asList(step1, step2));
  }

  private TmsStep createSingleOriginalStepWithAttachments() {
    var step = new TmsStep();
    step.setId(30L);
    step.setInstructions("Single original step instructions");
    step.setExpectedResult("Single original step expected result");
    step.setAttachments(Set.of(createAttachment(7L)));
    return step;
  }

  private TmsStep createSingleDuplicatedStepWithAttachments() {
    var step = new TmsStep();
    step.setId(31L);
    step.setInstructions("Single duplicated step instructions");
    step.setExpectedResult("Single duplicated step expected result");
    step.setAttachments(Set.of(createAttachment(8L)));
    return step;
  }

  private TmsStep createSingleOriginalStepWithoutAttachments() {
    var step = new TmsStep();
    step.setId(32L);
    step.setInstructions("Single original step instructions");
    step.setExpectedResult("Single original step expected result");
    step.setAttachments(Collections.emptySet());
    return step;
  }

  private TmsStep createSingleDuplicatedStepWithoutAttachments() {
    var step = new TmsStep();
    step.setId(33L);
    step.setInstructions("Single duplicated step instructions");
    step.setExpectedResult("Single duplicated step expected result");
    step.setAttachments(Collections.emptySet());
    return step;
  }

  private TmsAttachment createAttachment(Long id) {
    var attachment = new TmsAttachment();
    attachment.setId(id);
    // Set other properties as needed for your attachment entity
    return attachment;
  }
}
