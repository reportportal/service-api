package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.ta.reportportal.core.tms.service.factory.TmsManualScenarioImplServiceFactory;
import java.util.Collections;
import java.util.List;
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
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.createTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario, textScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);

    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldCreateTmsManualScenarioWithStepsType() {
    // Given
    when(tmsManualScenarioMapper.createTmsManualScenario(stepsScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.STEPS))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.createTmsManualScenario(testCaseVersion, stepsScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(stepsScenarioRQ);
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario, stepsScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);
  }

  @Test
  void shouldUpdateExistingTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(existingManualScenario))
        .thenReturn(existingManualScenario);

    // When
    var result = tmsManualScenarioService.updateTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).update(existingManualScenario, manualScenario);
    verify(tmsManualScenarioAttributeService).updateAttributes(eq(existingManualScenario), any());
    verify(tmsManualScenarioImplService).updateTmsManualScenarioImpl(existingManualScenario, textScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldCreateNewTmsManualScenarioWhenExistingDoesNotExist() {
    // Given
    testCaseVersion.setManualScenario(null);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(manualScenario))
        .thenReturn(manualScenario);

    // When
    var result = tmsManualScenarioService.updateTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(manualScenario);
    verify(tmsManualScenarioMapper).createTmsManualScenario(textScenarioRQ);
    verify(tmsManualScenarioAttributeService).createAttributes(eq(manualScenario), any());
    verify(tmsManualScenarioImplService).createTmsManualScenarioImpl(manualScenario, textScenarioRQ);
    verify(tmsManualScenarioRepository).save(manualScenario);
  }

  @Test
  void shouldPatchExistingTmsManualScenario() {
    // Given
    var existingManualScenario = createExistingManualScenario();
    testCaseVersion.setManualScenario(existingManualScenario);

    when(tmsManualScenarioMapper.createTmsManualScenario(textScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsManualScenarioImplServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioImplService);
    when(tmsManualScenarioRepository.save(existingManualScenario))
        .thenReturn(existingManualScenario);

    // When
    var result = tmsManualScenarioService.patchTmsManualScenario(testCaseVersion, textScenarioRQ);

    // Then
    assertThat(result).isEqualTo(existingManualScenario);
    verify(tmsManualScenarioMapper).patch(existingManualScenario, manualScenario);
    verify(tmsManualScenarioAttributeService).patchAttributes(eq(existingManualScenario), any());
    verify(tmsManualScenarioImplService).patchTmsManualScenarioImpl(existingManualScenario, textScenarioRQ);
    verify(tmsManualScenarioRepository).save(existingManualScenario);
  }

  @Test
  void shouldThrowExceptionWhenPatchingNonExistentManualScenario() {
    // Given
    testCaseVersion.setManualScenario(null);

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        tmsManualScenarioService.patchTmsManualScenario(testCaseVersion, textScenarioRQ));

    assertThat(exception.getMessage()).contains("Manual Scenario for the test case version with id");
    verify(tmsManualScenarioRepository, never()).save(any());
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioService(any());
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
    verify(tmsManualScenarioAttributeService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsManualScenarioRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsManualScenarioService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioImplServices();
    verify(tmsManualScenarioAttributeService, never()).deleteAllByTestCaseIds(any());
    verify(tmsManualScenarioRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    tmsManualScenarioService.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsManualScenarioImplServiceFactory, never()).getTmsManualScenarioImplServices();
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
    verify(tmsManualScenarioAttributeService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsManualScenarioRepository).deleteAllByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsTestCaseVersion createTestCaseVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(1L);
    version.setName("Test Version");
    return version;
  }

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

  private TmsStepsManualScenarioRQ createStepsScenarioRQ() {
    return TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(45)
        .linkToRequirements("http://requirements.com")
        .steps(Collections.emptyList())
        .tags(Collections.emptyList())
        .build();
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    scenario.setExecutionEstimationTime(30);
    scenario.setLinkToRequirements("http://requirements.com");
    return scenario;
  }

  private TmsManualScenario createExistingManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(99L);
    scenario.setExecutionEstimationTime(60);
    scenario.setLinkToRequirements("http://existing-requirements.com");
    return scenario;
  }
}
