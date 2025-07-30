package com.epam.ta.reportportal.core.tms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseVersionRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseDefaultVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseVersionMapper;
import com.epam.ta.reportportal.core.tms.service.factory.TmsManualScenarioServiceFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
  private TmsManualScenarioServiceFactory tmsManualScenarioServiceFactory;

  @Mock
  private TmsTestCaseVersionRepository tmsTestCaseVersionRepository;

  @Mock
  private TmsManualScenarioService tmsManualScenarioService;

  @InjectMocks
  private TmsTestCaseVersionServiceImpl tmsTestCaseVersionService;

  private TmsTestCase testCase;
  private TmsTestCaseDefaultVersionRQ defaultVersionRQ;
  private TmsTestCaseVersion testCaseVersion;
  private TmsManualScenario manualScenario;

  @BeforeEach
  void setUp() {
    testCase = createTestCase();
    defaultVersionRQ = createDefaultVersionRQ();
    testCaseVersion = createTestCaseVersion();
    manualScenario = createManualScenario();
  }

  @Test
  void shouldCreateDefaultTestCaseVersion() {
    // Given
    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultVersionRQ)).thenReturn(testCaseVersion);
    when(tmsManualScenarioServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioService);
    when(tmsManualScenarioService.createTmsManualScenario(testCaseVersion, defaultVersionRQ.getManualScenario()))
        .thenReturn(manualScenario);

    // When
    tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, defaultVersionRQ);

    // Then
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion(defaultVersionRQ);
    verify(tmsManualScenarioService).createTmsManualScenario(testCaseVersion, defaultVersionRQ.getManualScenario());
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);

    assertThat(testCase.getVersions()).contains(testCaseVersion);
    assertThat(testCaseVersion.getTestCase()).isEqualTo(testCase);
    assertThat(testCaseVersion.getManualScenario()).isEqualTo(manualScenario);
    assertThat(manualScenario.getTestCaseVersion()).isEqualTo(testCaseVersion);
  }

  @Test
  void shouldUpdateExistingDefaultTestCaseVersion() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    testCase.setVersions(Collections.singleton(existingVersion));

    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultVersionRQ)).thenReturn(testCaseVersion);
    when(tmsManualScenarioServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioService);
    when(tmsManualScenarioService.updateTmsManualScenario(existingVersion, defaultVersionRQ.getManualScenario()))
        .thenReturn(manualScenario);

    // When
    tmsTestCaseVersionService.updateDefaultTestCaseVersion(testCase, defaultVersionRQ);

    // Then
    verify(tmsTestCaseVersionMapper).update(existingVersion, testCaseVersion);
    verify(tmsManualScenarioService).updateTmsManualScenario(existingVersion, defaultVersionRQ.getManualScenario());
    verify(tmsTestCaseVersionRepository).save(existingVersion);

    assertThat(existingVersion.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldCreateNewDefaultVersionWhenNoneExists() {
    // Given
    testCase.setVersions(new HashSet<>());

    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultVersionRQ)).thenReturn(testCaseVersion);
    when(tmsManualScenarioServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioService);
    when(tmsManualScenarioService.createTmsManualScenario(testCaseVersion, defaultVersionRQ.getManualScenario()))
        .thenReturn(manualScenario);

    // When
    tmsTestCaseVersionService.updateDefaultTestCaseVersion(testCase, defaultVersionRQ);

    // Then
    verify(tmsTestCaseVersionMapper).createDefaultTestCaseVersion(defaultVersionRQ);
    verify(tmsManualScenarioService).createTmsManualScenario(testCaseVersion, defaultVersionRQ.getManualScenario());
    verify(tmsTestCaseVersionRepository).save(testCaseVersion);
  }

  @Test
  void shouldPatchExistingDefaultTestCaseVersion() {
    // Given
    var existingVersion = createExistingDefaultVersion();
    testCase.setVersions(Collections.singleton(existingVersion));

    when(tmsTestCaseVersionMapper.createDefaultTestCaseVersion(defaultVersionRQ)).thenReturn(testCaseVersion);
    when(tmsManualScenarioServiceFactory.getTmsManualScenarioService(TmsManualScenarioType.TEXT))
        .thenReturn(tmsManualScenarioService);
    when(tmsManualScenarioService.patchTmsManualScenario(existingVersion, defaultVersionRQ.getManualScenario()))
        .thenReturn(manualScenario);

    // When
    tmsTestCaseVersionService.patchDefaultTestCaseVersion(testCase, defaultVersionRQ);

    // Then
    verify(tmsTestCaseVersionMapper).patch(existingVersion, testCaseVersion);
    verify(tmsManualScenarioService).patchTmsManualScenario(existingVersion, defaultVersionRQ.getManualScenario());
    verify(tmsTestCaseVersionRepository).save(existingVersion);

    assertThat(existingVersion.getManualScenario()).isEqualTo(manualScenario);
  }

  @Test
  void shouldThrowNotFoundExceptionWhenPatchingNonExistentDefaultVersion() {
    // Given
    testCase.setVersions(new HashSet<>());

    // When & Then
    var exception = assertThrows(ReportPortalException.class, () ->
        tmsTestCaseVersionService.patchDefaultTestCaseVersion(testCase, defaultVersionRQ));

    assertThat(exception.getMessage()).contains("Default test case version for test case", "not found");
    verify(tmsTestCaseVersionRepository, never()).save(any());
  }

  @Test
  void shouldDeleteAllByTestCaseId() {
    // When
    tmsTestCaseVersionService.deleteAllByTestCaseId(123L);

    // Then
    verify(tmsTestCaseVersionRepository).deleteAllByTestCaseId(123L);
  }

  @Test
  void shouldDeleteAllByTestCaseIds() {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);

    // When
    tmsTestCaseVersionService.deleteAllByTestCaseIds(testCaseIds);

    // Then
    verify(tmsTestCaseVersionRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void shouldNotDeleteWhenTestCaseIdsIsEmpty() {
    // When
    tmsTestCaseVersionService.deleteAllByTestCaseIds(Collections.emptyList());

    // Then
    verify(tmsTestCaseVersionRepository, never()).deleteAllByTestCaseIds(any());
  }

  @Test
  void shouldDeleteAllByTestFolderId() {
    // When
    tmsTestCaseVersionService.deleteAllByTestFolderId(1L, 123L);

    // Then
    verify(tmsTestCaseVersionRepository).deleteTestCaseVersionsByTestFolderId(1L, 123L);
  }

  // Helper methods
  private TmsTestCase createTestCase() {
    var testCase = new TmsTestCase();
    testCase.setId(1L);
    testCase.setName("Test Case");
    testCase.setVersions(new HashSet<>());
    return testCase;
  }

  private TmsTestCaseDefaultVersionRQ createDefaultVersionRQ() {
    var versionRQ = new TmsTestCaseDefaultVersionRQ();
    versionRQ.setName("Default Version");

    var manualScenarioRQ = new TmsTextManualScenarioRQ();
    manualScenarioRQ.setManualScenarioType(TmsManualScenarioType.TEXT);
    versionRQ.setManualScenario(manualScenarioRQ);

    return versionRQ;
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
}
