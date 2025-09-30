package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.tms.TmsAttribute;
import com.epam.ta.reportportal.entity.tms.TmsManualScenario;
import com.epam.ta.reportportal.entity.tms.TmsManualScenarioAttribute;
import com.epam.ta.reportportal.entity.tms.TmsManualScenarioAttributeId;
import com.epam.ta.reportportal.entity.tms.TmsManualScenarioPreconditions;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseVersion;
import com.epam.ta.reportportal.dao.tms.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.ta.reportportal.core.tms.service.factory.TmsManualScenarioImplServiceFactory;
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
class TmsManualScenarioServiceImplTest {

  @Mock
  private TmsManualScenarioRepository tmsManualScenarioRepository;

  @Mock
  private TmsManualScenarioAttributeService tmsManualScenarioAttributeService;

  @Mock
  private TmsManualScenarioImplServiceFactory tmsManualScenarioImplServiceFactory;

  @Mock
  private TmsManualScenarioMapper tmsManualScenarioMapper;

  @Mock
  private TmsManualScenarioImplService tmsManualScenarioImplService;

  @Mock
  private TmsManualScenarioPreconditionsService tmsManualScenarioPreconditionsService;

  @InjectMocks
  private TmsManualScenarioServiceImpl tmsManualScenarioService;

  private TmsTestCaseVersion testCaseVersion;
  private TmsTextManualScenarioRQ textScenarioRQ;
  private TmsStepsManualScenarioRQ stepsScenarioRQ;
  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    testCaseVersion = createTestCaseVersion();
    textScenarioRQ = createTextScenarioRQ();
    stepsScenarioRQ = createStepsScenarioRQ();
    manualScenario = createManualScenario();
  }

  @Test
  void shouldCreateTmsManualScenarioWithTextType() {
    // Given
    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.createTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsManualScenarioPreconditionsService).createPreconditions(eq(manualScenario), any());
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario,
        textScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);

    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldCreateTmsManualScenarioWithStepsType() {
    // Given
    when(tmsManualScenarioMapper.createTmsManualScenario(stepsScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(
        TmsManualScenarioType.STEPS))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.createTmsManualScenario(testCaseVersion, stepsScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(stepsScenarioRQ);
    verify(tmsManualScenarioPreconditionsService).createPreconditions(eq(manualScenario), any());
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario,
        stepsScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);
  }

  @Test
  void shouldUpdateExistingTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(existingManualScenario))
        .thenReturn(existingManualScenario);

    // When
    var result = tmsManualScenarioService.updateTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).update(existingManualScenario, manualScenario);
    verify(tmsManualScenarioPreconditionsService).updatePreconditions(eq(existingManualScenario), any());
    verify(tmsManualScenarioAttributeService).updateAttributes(eq(existingManualScenario), any());
    verify(tmsManualScenarioImplService).updateTmsManualScenarioImpl(existingManualScenario,
        textScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldCreateNewTmsManualScenarioWhenExistingDoesNotExist() {
    // Given
    testCaseVersion.setManualScenario(null);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.updateTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsManualScenarioPreconditionsService).createPreconditions(eq(manualScenario), any());
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario,
        textScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);
  }

  @Test
  void shouldPatchExistingTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(existingManualScenario))
        .thenReturn(existingManualScenario);

    // When
    var result = tmsManualScenarioService.patchTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).patch(existingManualScenario, manualScenario);
    verify(tmsManualScenarioPreconditionsService).patchPreconditions(eq(existingManualScenario), any());
    verify(tmsManualScenarioAttributeService).patchAttributes(eq(existingManualScenario), any());
    verify(tmsManualScenarioImplService).patchTmsManualScenarioImpl(existingManualScenario,
        textScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldThrowExceptionWhenPatchingNonExistentManualScenario() {
    // Given
    testCaseVersion.setManualScenario(null);

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        tmsManualScenarioService.patchTmsManualScenario(testCaseVersion, textScenarioRQ));

    assertThat(exception.getMessage()).contains(
        "Manual Scenario for the test case version with id");
    verify(tmsManualScenarioRepository, never()).save(any());
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioService(
        any(TmsManualScenarioType.class));
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // Given
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioImplServices())
        .thenReturn(List.of(tmsManualScenarioImplService));

    // When
    tmsManualScenarioService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsManualScenarioImplService).deleteAllByTestCaseId(123L);
    verify(tmsManualScenarioPreconditionsService).deleteAllByTestCaseId(123L);
    verify(tmsManualScenarioAttributeService).deleteAllByTestCaseId(123L);
    verify(tmsManualScenarioRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioImplServices())
        .thenReturn(List.of(tmsManualScenarioImplService));

    // When
    tmsManualScenarioService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsManualScenarioImplService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioPreconditionsService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioAttributeService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioImplServices();
    verify(tmsManualScenarioPreconditionsService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioAttributeService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsManualScenarioService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioImplServices();
    verify(tmsManualScenarioPreconditionsService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioAttributeService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // Given
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioImplServices())
        .thenReturn(List.of(tmsManualScenarioImplService));

    // When
    tmsManualScenarioService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsManualScenarioImplService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioPreconditionsService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioAttributeService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioRepository).deleteAllByTestFolderId(1L, 123L);
  }

  @Test
  void shouldDuplicateManualScenarioWithPreconditionsAndAttributes() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithPreconditionsAndAttributes();
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(originalScenario.getType()))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioMapper).duplicateManualScenario(originalScenario, newVersion);
    verify(tmsManualScenarioRepository).save(duplicatedScenario);
    verify(tmsManualScenarioPreconditionsService).duplicatePreconditions(originalScenario,
        duplicatedScenario);
    verify(tmsManualScenarioAttributeService).duplicateAttributes(originalScenario,
        duplicatedScenario);
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithAttributesButNoPreconditions() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithAttributes();
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(originalScenario.getType()))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioMapper).duplicateManualScenario(originalScenario, newVersion);
    verify(tmsManualScenarioRepository).save(duplicatedScenario);
    verify(tmsManualScenarioPreconditionsService, never()).duplicatePreconditions(any(), any());
    verify(tmsManualScenarioAttributeService).duplicateAttributes(originalScenario,
        duplicatedScenario);
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithPreconditionsButNoAttributes() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithPreconditions();
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(originalScenario.getType()))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioMapper).duplicateManualScenario(originalScenario, newVersion);
    verify(tmsManualScenarioRepository).save(duplicatedScenario);
    verify(tmsManualScenarioPreconditionsService).duplicatePreconditions(originalScenario,
        duplicatedScenario);
    verify(tmsManualScenarioAttributeService, never()).duplicateAttributes(any(), any());
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithoutPreconditionsAndAttributes() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithoutPreconditionsAndAttributes();
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(originalScenario.getType()))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioMapper).duplicateManualScenario(originalScenario, newVersion);
    verify(tmsManualScenarioRepository).save(duplicatedScenario);
    verify(tmsManualScenarioPreconditionsService, never()).duplicatePreconditions(any(), any());
    verify(tmsManualScenarioAttributeService, never()).duplicateAttributes(any(), any());
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithEmptyAttributes() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithEmptyAttributes();
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(originalScenario.getType()))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioMapper).duplicateManualScenario(originalScenario, newVersion);
    verify(tmsManualScenarioRepository).save(duplicatedScenario);
    verify(tmsManualScenarioPreconditionsService, never()).duplicatePreconditions(any(), any());
    verify(tmsManualScenarioAttributeService, never()).duplicateAttributes(any(), any());
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithTextType() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithPreconditionsAndAttributes();
    originalScenario.setType(
        com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(
        tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(
            com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioImplServiceFactory).getTmsManualScenarioService(
        com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  @Test
  void shouldDuplicateManualScenarioWithStepsType() {
    // Given
    var newVersion = createNewTestCaseVersion();
    var originalScenario = createOriginalScenarioWithPreconditionsAndAttributes();
    originalScenario.setType(
        com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.STEPS);
    var duplicatedScenario = createDuplicatedManualScenario();

    when(tmsManualScenarioMapper.duplicateManualScenario(originalScenario, newVersion))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioRepository.save(duplicatedScenario))
        .thenReturn(duplicatedScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(
        com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.STEPS))
        .thenReturn(tmsManualScenarioImplService);

    // When
    var result = tmsManualScenarioService.duplicateManualScenario(newVersion, originalScenario);

    // Then
    assertThat(result).isEqualTo(duplicatedScenario);
    verify(tmsManualScenarioImplServiceFactory).getTmsManualScenarioService(
        com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.STEPS);
    verify(tmsManualScenarioImplService).duplicateManualScenarioImpl(duplicatedScenario,
        originalScenario);
  }

  // Helper methods
  private TmsTestCaseVersion createTestCaseVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(1L);
    version.setName("Test Version");
    return version;
  }

  private TmsTestCaseVersion createNewTestCaseVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(2L);
    version.setName("New Test Version");
    return version;
  }

  private TmsTextManualScenarioRQ createTextScenarioRQ() {
    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(30)
        .linkToRequirements("http://requirements.com")
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .preconditions(createPreconditionsRQ())
        .attributes(Collections.emptyList())
        .build();
  }

  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    return TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(45)
        .linkToRequirements("http://requirements.com")
        .steps(Collections.emptyList())
        .preconditions(createPreconditionsRQ())
        .attributes(Collections.emptyList())
        .build();
  }

  private TmsManualScenarioPreconditionsRQ createPreconditionsRQ() {
    return TmsManualScenarioPreconditionsRQ.builder()
        .value("Test preconditions")
        .attachments(Collections.emptyList())
        .build();
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setExecutionEstimationTime(30);
    scenario.setLinkToRequirements("http://requirements.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    return scenario;
  }

  private TmsManualScenario createExistingManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(99L);
    scenario.setExecutionEstimationTime(60);
    scenario.setLinkToRequirements("http://existing-requirements.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    return scenario;
  }

  private TmsManualScenario createOriginalScenarioWithPreconditionsAndAttributes() {
    var scenario = new TmsManualScenario();
    scenario.setId(10L);
    scenario.setExecutionEstimationTime(45);
    scenario.setLinkToRequirements("http://original-requirements.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);

    var preconditions = new TmsManualScenarioPreconditions();
    preconditions.setId(1L);
    preconditions.setManualScenario(scenario);
    scenario.setPreconditions(preconditions);

    var attribute = createTmsManualScenarioAttribute(10L, 1L, "test-value");
    scenario.setAttributes(Set.of(attribute));

    return scenario;
  }

  private TmsManualScenario createOriginalScenarioWithAttributes() {
    var scenario = new TmsManualScenario();
    scenario.setId(10L);
    scenario.setExecutionEstimationTime(45);
    scenario.setLinkToRequirements("http://original-requirements.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);

    scenario.setPreconditions(null);

    var attribute = createTmsManualScenarioAttribute(10L, 1L, "test-value");
    scenario.setAttributes(Set.of(attribute));

    return scenario;
  }

  private TmsManualScenario createOriginalScenarioWithPreconditions() {
    var scenario = new TmsManualScenario();
    scenario.setId(11L);
    scenario.setExecutionEstimationTime(50);
    scenario.setLinkToRequirements("http://original-requirements-with-preconditions.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.STEPS);

    var preconditions = new TmsManualScenarioPreconditions();
    preconditions.setId(2L);
    preconditions.setManualScenario(scenario);
    scenario.setPreconditions(preconditions);

    scenario.setAttributes(null);
    return scenario;
  }

  private TmsManualScenario createOriginalScenarioWithoutPreconditionsAndAttributes() {
    var scenario = new TmsManualScenario();
    scenario.setId(11L);
    scenario.setExecutionEstimationTime(50);
    scenario.setLinkToRequirements("http://original-requirements-no-attr.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.STEPS);
    scenario.setPreconditions(null);
    scenario.setAttributes(null);
    return scenario;
  }

  private TmsManualScenario createOriginalScenarioWithEmptyAttributes() {
    var scenario = new TmsManualScenario();
    scenario.setId(12L);
    scenario.setExecutionEstimationTime(55);
    scenario.setLinkToRequirements("http://original-requirements-empty-attr.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    scenario.setPreconditions(null);
    scenario.setAttributes(Collections.emptySet());
    return scenario;
  }

  private TmsManualScenario createDuplicatedManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(20L);
    scenario.setExecutionEstimationTime(45);
    scenario.setLinkToRequirements("http://duplicated-requirements.com");
    scenario.setType(com.epam.ta.reportportal.entity.tms.enums.TmsManualScenarioType.TEXT);
    return scenario;
  }

  private TmsManualScenarioAttribute createTmsManualScenarioAttribute(Long manualScenarioId,
      Long attributeId, String value) {
    var attributeIdObj = new TmsManualScenarioAttributeId(manualScenarioId, attributeId);

    var tmsAttribute = new TmsAttribute();
    tmsAttribute.setId(attributeId);

    var scenarioAttribute = new TmsManualScenarioAttribute();
    scenarioAttribute.setId(attributeIdObj);
    scenarioAttribute.setAttribute(tmsAttribute);
    scenarioAttribute.setValue(value);

    return scenarioAttribute;
  }
}
