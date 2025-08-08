package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseDefaultVersionTestCaseId;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseVersionMapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseVersionServiceImplTest {

  @Mock
  private TmsTestCaseVersionMapper tmsTestCaseVersionMapper;

  @Mock
  private TmsManualScenarioService tmsManualScenarioService;

  @Mock
  private TmsTestCaseVersionRepository tmsTestCaseVersionRepository;

  @InjectMocks
  private TmsTestCaseVersionServiceImpl sut;

  private TmsTestCase testCase;
  private TmsTextManualScenarioRQ textManualScenarioRQ;
  private TmsStepsManualScenarioRQ stepsManualScenarioRQ;
  private TmsTestCaseVersion testCaseVersion;
  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    testCase = createTestCase();
    textManualScenarioRQ = createTextManualScenarioRQ();
    stepsManualScenarioRQ = createStepsManualScenarioRQ();
    testCaseVersion = createTestCaseVersion();
    manualScenario = createManualScenario();
  }

  @Test
  void shouldCreateDefaultTestCaseVersionWithTextScenario() {
    // Given
    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion()).thenReturn(testCaseVersion);
    when(tmsManualScenarioService.createTmsManualScenario(testCaseVersion, textManualScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsTestCaseVersionRepository.save(testCaseVersion)).thenReturn(testCaseVersion);

    // When
    var result = sut.createDefaultTestCaseVersion(testCase, textManualScenarioRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseVersion, result);
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion();
    verify(tmsManualScenarioService).createTmsManualScenario(testCaseVersion, textManualScenarioRQ);
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);

    assertThat(testCase.getVersions()).contains(testCaseVersion);
    assertThat(testCaseVersion.getTestCase()).isEqualTo(testCase);
    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldCreateDefaultTestCaseVersionWithStepsScenario() {
    // Given
    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion()).thenReturn(testCaseVersion);
    when(tmsManualScenarioService.createTmsManualScenario(testCaseVersion, stepsManualScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsTestCaseVersionRepository.save(testCaseVersion)).thenReturn(testCaseVersion);

    // When
    var result = sut.createDefaultTestCaseVersion(testCase, stepsManualScenarioRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseVersion, result);
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion();
    verify(tmsManualScenarioService).createTmsManualScenario(testCaseVersion, stepsManualScenarioRQ);
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);

    assertThat(testCase.getVersions()).contains(testCaseVersion);
    assertThat(testCaseVersion.getTestCase()).isEqualTo(testCase);
    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldCreateDefaultTestCaseVersionWithoutManualScenario() {
    // Given
    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion()).thenReturn(testCaseVersion);
    when(tmsTestCaseVersionRepository.save(testCaseVersion)).thenReturn(testCaseVersion);

    // When
    var result = sut.createDefaultTestCaseVersion(testCase, null);

    // Then
    assertNotNull(result);
    assertEquals(testCaseVersion, result);
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion();
    verify(tmsManualScenarioService, never()).createTmsManualScenario(any(), any());
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);

    assertThat(testCase.getVersions()).contains(testCaseVersion);
    assertThat(testCaseVersion.getTestCase()).isEqualTo(testCase);
    assertThat(testCaseVersion.getManualScenario()).isNull();
  }

  @Test
  void shouldUpdateExistingDefaultTestCaseVersion() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.of(existingVersion));
    when(tmsManualScenarioService.updateTmsManualScenario(existingVersion, textManualScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsTestCaseVersionRepository.save(existingVersion)).thenReturn(existingVersion);

    // When
    var result = sut.updateDefaultTestCaseVersion(testCase, textManualScenarioRQ);

    // Then
    assertNotNull(result);
    assertEquals(existingVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsManualScenarioService).updateTmsManualScenario(existingVersion, textManualScenarioRQ);
    verify(tmsTestCaseVersionRepository).save(existingVersion);

    assertThat(existingVersion.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldUpdateExistingDefaultTestCaseVersionWithoutManualScenario() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.of(existingVersion));

    // When
    var result = sut.updateDefaultTestCaseVersion(testCase, null);

    // Then
    assertNotNull(result);
    assertEquals(existingVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsManualScenarioService, never()).updateTmsManualScenario(any(), any());
    verify(tmsTestCaseVersionRepository, never()).save(any());
  }

  @Test
  void shouldCreateNewDefaultVersionWhenNoneExists() {
    // Given
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.empty());
    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion()).thenReturn(testCaseVersion);
    when(tmsManualScenarioService.createTmsManualScenario(testCaseVersion, textManualScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsTestCaseVersionRepository.save(testCaseVersion)).thenReturn(testCaseVersion);

    // When
    var result = sut.updateDefaultTestCaseVersion(testCase, textManualScenarioRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion();
    verify(tmsManualScenarioService).createTmsManualScenario(testCaseVersion, textManualScenarioRQ);
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);
  }

  @Test
  void shouldPatchExistingDefaultTestCaseVersion() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.of(existingVersion));
    when(tmsManualScenarioService.patchTmsManualScenario(existingVersion, textManualScenarioRQ))
        .thenReturn(manualScenario);
    when(tmsTestCaseVersionRepository.save(existingVersion)).thenReturn(existingVersion);

    // When
    var result = sut.patchDefaultTestCaseVersion(testCase, textManualScenarioRQ);

    // Then
    assertNotNull(result);
    assertEquals(existingVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsManualScenarioService).patchTmsManualScenario(existingVersion, textManualScenarioRQ);
    verify(tmsTestCaseVersionRepository).save(existingVersion);

    assertThat(existingVersion.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldPatchExistingDefaultTestCaseVersionWithoutManualScenario() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.of(existingVersion));

    // When
    var result = sut.patchDefaultTestCaseVersion(testCase, null);

    // Then
    assertNotNull(result);
    assertEquals(existingVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsManualScenarioService, never()).patchTmsManualScenario(any(), any());
    verify(tmsTestCaseVersionRepository, never()).save(any());
  }

  @Test
  void shouldThrowNotFoundExceptionWhenPatchingNonExistentDefaultVersion() {
    // Given
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCase.getId()))
        .thenReturn(Optional.empty());

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.patchDefaultTestCaseVersion(testCase, textManualScenarioRQ));

    assertThat(exception.getMessage()).contains("Default test case version for test case");
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCase.getId());
    verify(tmsTestCaseVersionRepository, never()).save(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    sut.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsManualScenarioService).deleteAllByTestCaseId(123L);
    verify(tmsTestCaseVersionRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    sut.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsManualScenarioService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsTestCaseVersionRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    sut.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsManualScenarioService, never()).deleteAllByTestCaseIds(any());
    verify(tmsTestCaseVersionRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsNull() {
    // When
    sut.deleteAllByTestCaseIds(null);

    // Then
    verify(tmsManualScenarioService, never()).deleteAllByTestCaseIds(any());
    verify(tmsTestCaseVersionRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    sut.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsManualScenarioService).deleteAllByTestFolderId(1L, 123L);
    verify(tmsTestCaseVersionRepository).deleteTestCaseVersionsByTestFolderId(1L, 123L);
  }

  @Test
  void shouldGetDefaultVersion() {
    // Given
    var testCaseId = 123L;
    var defaultVersion = createExistingDefaultVersion();
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCaseId))
        .thenReturn(Optional.of(defaultVersion));

    // When
    var result = sut.getDefaultVersion(testCaseId);

    // Then
    assertNotNull(result);
    assertEquals(defaultVersion, result);
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCaseId);
  }

  @Test
  void shouldThrowNotFoundExceptionWhenDefaultVersionNotFound() {
    // Given
    var testCaseId = 123L;
    when(tmsTestCaseVersionRepository.findDefaultVersionByTestCaseId(testCaseId))
        .thenReturn(Optional.empty());

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.getDefaultVersion(testCaseId));

    assertThat(exception.getMessage()).contains("Default test case version for test case: 123");
    verify(tmsTestCaseVersionRepository).findDefaultVersionByTestCaseId(testCaseId);
  }

  @Test
  void shouldGetDefaultVersions() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);
    var version1 = createTestCaseVersion();
    version1.setId(1L);
    var version2 = createTestCaseVersion();
    version2.setId(2L);
    var version3 = createTestCaseVersion();
    version3.setId(3L);

    var defaultVersionsData = List.of(
        createTestCaseDefaultVersionTestCaseId(1L, version1),
        createTestCaseDefaultVersionTestCaseId(2L, version2),
        createTestCaseDefaultVersionTestCaseId(3L, version3)
    );

    when(tmsTestCaseVersionRepository.findDefaultVersionsByTestCaseIds(testCaseIds))
        .thenReturn(defaultVersionsData);

    // When
    var result = sut.getDefaultVersions(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(version1, result.get(1L));
    assertEquals(version2, result.get(2L));
    assertEquals(version3, result.get(3L));
    verify(tmsTestCaseVersionRepository).findDefaultVersionsByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldReturnEmptyMapWhenDefaultVersionsNotFound() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseVersionRepository.findDefaultVersionsByTestCaseIds(testCaseIds))
        .thenReturn(null);

    // When
    var result = sut.getDefaultVersions(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
    verify(tmsTestCaseVersionRepository).findDefaultVersionsByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldReturnEmptyMapWhenDefaultVersionsIsEmptyList() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseVersionRepository.findDefaultVersionsByTestCaseIds(testCaseIds))
        .thenReturn(Collections.emptyList());

    // When
    var result = sut.getDefaultVersions(testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
    verify(tmsTestCaseVersionRepository).findDefaultVersionsByTestCaseIds(testCaseIds);
  }

  // Helper methods
  private TmsTestCase createTestCase() {
    var testCase = new TmsTestCase();
    testCase.setId(1L);
    testCase.setName("Test Case");
    testCase.setVersions(new HashSet<>());
    return testCase;
  }

  private TmsTextManualScenarioRQ createTextManualScenarioRQ() {
    return TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(30)
        .linkToRequirements("http://requirements.com")
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .build();
  }

  private TmsStepsManualScenarioRQ createStepsManualScenarioRQ() {
    return TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(45)
        .linkToRequirements("http://requirements.com")
        .steps(Collections.emptyList())
        .build();
  }

  private TmsTestCaseVersion createTestCaseVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(1L);
    version.setName("Default Version");
    version.setDefault(true);
    return version;
  }

  private TmsTestCaseVersion createExistingDefaultVersion() {
    var version = new TmsTestCaseVersion();
    version.setId(99L);
    version.setName("Existing Default Version");
    version.setDefault(true);
    return version;
  }

  private TmsManualScenario createManualScenario() {
    var scenario = new TmsManualScenario();
    scenario.setId(1L);
    return scenario;
  }

  private TmsTestCaseDefaultVersionTestCaseId createTestCaseDefaultVersionTestCaseId(Long testCaseId, TmsTestCaseVersion version) {
    var defaultVersionTestCaseId = new TmsTestCaseDefaultVersionTestCaseId();
    defaultVersionTestCaseId.setTestCaseId(testCaseId);
    defaultVersionTestCaseId.setTestCaseVersion(version);
    return defaultVersionTestCaseId;
  }
}
