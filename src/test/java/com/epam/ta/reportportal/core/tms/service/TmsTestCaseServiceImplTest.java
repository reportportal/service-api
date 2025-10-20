package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.tms.TmsTestCase;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseExecution;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseVersion;
import com.epam.ta.reportportal.entity.tms.TmsTestFolder;
import com.epam.ta.reportportal.dao.tms.TmsTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.dao.tms.filterable.TmsTestCaseFilterableRepository;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestCaseExporter;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.importer.TmsTestCaseImporter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseServiceImplTest {

  @Mock
  private TmsTestCaseMapper tmsTestCaseMapper;

  @Mock
  private TmsTestCaseRepository tmsTestCaseRepository;

  @Mock
  private TmsTestCaseFilterableRepository tmsTestCaseFilterableRepository;

  @Mock
  private TmsTestCaseAttributeService tmsTestCaseAttributeService;

  @Mock
  private TmsTestCaseVersionService tmsTestCaseVersionService;

  @Mock
  private TmsTestFolderService tmsTestFolderService;

  @Mock
  private TmsTestCaseImporterFactory importerFactory;

  @Mock
  private TmsTestCaseExporterFactory exporterFactory;

  @Mock
  private TmsTestCaseImporter importer;

  @Mock
  private TmsTestCaseExporter exporter;

  @Mock
  private TmsTestPlanTestCaseRepository tmsTestPlanTestCaseRepository;

  @Mock
  private TmsTestCaseExecutionService tmsTestCaseExecutionService;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private TmsTestCaseServiceImpl sut;

  private TmsTestCaseRQ testCaseRQ;
  private TmsTestCase testCase;
  private TmsTestCaseRS testCaseRS;
  private TmsTestCaseVersion testCaseVersion;
  private TmsTestCaseExecution testCaseExecution;
  private TmsTextManualScenarioRQ textManualScenarioRQ;
  private TmsStepsManualScenarioRQ stepsManualScenarioRQ;
  private NewTestFolderRQ newTestFolderRQ;
  private TmsTestFolderRS testFolderRS;
  private TmsTestFolder testFolder;
  private List<TmsTestCaseAttributeRQ> attributes;
  private long projectId;
  private Long testCaseId;
  private Long testFolderId;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    testCaseId = 2L;
    testFolderId = 4L;

    attributes = new ArrayList<>();
    var attribute = new TmsTestCaseAttributeRQ();
    attribute.setValue("value");
    attribute.setId(3L);
    attributes.add(attribute);

    textManualScenarioRQ = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .executionEstimationTime(30)
        .linkToRequirements("http://requirements.com")
        .instructions("Test instructions")
        .expectedResult("Expected result")
        .build();

    stepsManualScenarioRQ = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(45)
        .linkToRequirements("http://requirements.com")
        .steps(Collections.emptyList())
        .build();

    newTestFolderRQ = new NewTestFolderRQ();
    newTestFolderRQ.setName("Test Folder");

    testFolderRS = new TmsTestFolderRS();
    testFolderRS.setId(testFolderId);

    testFolder = new TmsTestFolder();
    testFolder.setId(testFolderId);
    testFolder.setName("Test Folder");

    testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case");
    testCaseRQ.setDescription("Description");
    testCaseRQ.setTestFolder(newTestFolderRQ);
    testCaseRQ.setAttributes(attributes);
    testCaseRQ.setManualScenario(textManualScenarioRQ);

    testCase = new TmsTestCase();
    testCase.setId(testCaseId);
    testCase.setName("Test Case");
    testCase.setDescription("Description");
    testCase.setTestFolder(testFolder);

    testCaseVersion = new TmsTestCaseVersion();
    testCaseVersion.setId(1L);
    testCaseVersion.setTestCase(testCase);

    testCaseExecution = new TmsTestCaseExecution();
    testCaseExecution.setId(1L);

    testCaseRS = new TmsTestCaseRS();
    testCaseRS.setId(testCaseId);
    testCaseRS.setName("Test Case");
    testCaseRS.setDescription("Description");

    // Setup TmsTestFolderService injection
    sut.setTmsTestFolderService(tmsTestFolderService);
  }

  @Test
  void getTestCaseByProjectId_ShouldReturnListOfTestCases() {
    // Given
    var testCases = List.of(testCase);
    when(tmsTestCaseRepository.findByProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.getTestCaseByProjectId(projectId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.get(0));
    verify(tmsTestCaseRepository).findByProjectId(projectId);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void getById_WhenTestCaseExists_ShouldReturnTestCase() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestCaseVersionService.getDefaultVersion(testCaseId)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);

    // When
    var result = sut.getById(projectId, testCaseId);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestCaseVersionService).getDefaultVersion(testCaseId);
    verify(tmsTestCaseExecutionService).getLastTestCaseExecution(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion, testCaseExecution);
  }

  @Test
  void getById_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.getById(projectId, testCaseId));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
  }

  @Test
  void create_WithTestFolder_ShouldCreateAndReturnTestCase() {
    // Given
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void create_WithTestFolderId_ShouldCreateAndReturnTestCase() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);
    testCaseWithFolderIdRQ.setAttributes(attributes);
    testCaseWithFolderIdRQ.setManualScenario(textManualScenarioRQ);

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseWithFolderIdRQ,
        testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseWithFolderIdRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseWithFolderIdRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void create_WithStepsManualScenario_ShouldCreateAndReturnTestCase() {
    // Given
    var testCaseWithStepsRQ = new TmsTestCaseRQ();
    testCaseWithStepsRQ.setName("Test Case");
    testCaseWithStepsRQ.setDescription("Description");
    testCaseWithStepsRQ.setTestFolder(newTestFolderRQ);
    testCaseWithStepsRQ.setAttributes(attributes);
    testCaseWithStepsRQ.setManualScenario(stepsManualScenarioRQ);

    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseWithStepsRQ, testFolderId)).thenReturn(
        testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        stepsManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseWithStepsRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseWithStepsRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, stepsManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void create_WithNewTestFolder_ShouldCreateFolderAndTestCase() {
    // Given
    var testFolderName = "New Folder";
    var newTestFolderRQ = new NewTestFolderRQ();
    newTestFolderRQ.setName(testFolderName);

    var testCaseRQWithNewFolder = new TmsTestCaseRQ();
    testCaseRQWithNewFolder.setName("Test Case");
    testCaseRQWithNewFolder.setTestFolder(newTestFolderRQ);
    testCaseRQWithNewFolder.setAttributes(attributes);
    testCaseRQWithNewFolder.setManualScenario(textManualScenarioRQ);

    var newFolderId = 10L;
    var newFolderRS = new TmsTestFolderRS();
    newFolderRS.setId(newFolderId);

    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        newFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQWithNewFolder,
        newFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQWithNewFolder);

    // Then
    assertNotNull(result);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQWithNewFolder, newFolderId);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void create_WithNonExistentTestFolderId_ShouldThrowReportPortalException() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.create(projectId, testCaseWithFolderIdRQ));
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository, never()).save(any());
  }

  @Test
  void create_WithoutTags_ShouldNotCreateAttributes() {
    // Given
    var testCaseRQWithoutTags = new TmsTestCaseRQ();
    testCaseRQWithoutTags.setName("Test Case");
    testCaseRQWithoutTags.setTestFolder(newTestFolderRQ);
    testCaseRQWithoutTags.setManualScenario(textManualScenarioRQ);

    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(
        tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQWithoutTags, testFolderId)).thenReturn(
        testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQWithoutTags);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService, never()).createTestCaseAttributes(any(), any());
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void update_WhenTestCaseExists_ShouldUpdateAndReturnTestCase() {
    // Given
    var convertedTestCase = new TmsTestCase();
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(
        convertedTestCase);
    when(tmsTestCaseVersionService.updateDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).update(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).updateTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).updateDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseExecutionService).getLastTestCaseExecution(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion, testCaseExecution);
  }

  @Test
  void update_WhenTestCaseDoesNotExist_ShouldCreateNewTestCase() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.empty());
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void update_WithTestFolderId_ShouldUpdateAndReturnTestCase() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);
    testCaseWithFolderIdRQ.setAttributes(attributes);
    testCaseWithFolderIdRQ.setManualScenario(textManualScenarioRQ);

    var convertedTestCase = new TmsTestCase();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseWithFolderIdRQ,
        testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseVersionService.updateDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseWithFolderIdRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseWithFolderIdRQ, testFolderId);
    verify(tmsTestCaseMapper).update(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).updateTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).updateDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseExecutionService).getLastTestCaseExecution(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion, testCaseExecution);
  }

  @Test
  void update_WithNonExistentTestFolderId_ShouldThrowReportPortalException() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.update(projectId, testCaseId, testCaseWithFolderIdRQ));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper, never()).update(any(), any());
  }

  @Test
  void patch_WhenTestCaseExists_ShouldPatchAndReturnTestCase() {
    // Given
    var convertedTestCase = new TmsTestCase();
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(
        convertedTestCase);
    when(tmsTestCaseVersionService.patchDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);

    // When
    var result = sut.patch(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).create(eq(projectId), any(NewTestFolderRQ.class));
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).patch(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).patchDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseExecutionService).getLastTestCaseExecution(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion, testCaseExecution);
  }

  @Test
  void patch_WithTestFolderId_ShouldPatchAndReturnTestCase() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);
    testCaseWithFolderIdRQ.setAttributes(attributes);
    testCaseWithFolderIdRQ.setManualScenario(textManualScenarioRQ);

    var convertedTestCase = new TmsTestCase();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseWithFolderIdRQ,
        testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseVersionService.patchDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(testCaseId)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);

    // When
    var result = sut.patch(projectId, testCaseId, testCaseWithFolderIdRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseWithFolderIdRQ, testFolderId);
    verify(tmsTestCaseMapper).patch(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).patchDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseExecutionService).getLastTestCaseExecution(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion, testCaseExecution);
  }

  @Test
  void patch_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, testCaseId, testCaseRQ));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
  }

  @Test
  void patch_WithNonExistentTestFolderId_ShouldThrowReportPortalException() {
    // Given
    var testCaseWithFolderIdRQ = new TmsTestCaseRQ();
    testCaseWithFolderIdRQ.setName("Test Case");
    testCaseWithFolderIdRQ.setDescription("Description");
    testCaseWithFolderIdRQ.setTestFolderId(testFolderId);

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, testCaseId, testCaseWithFolderIdRQ));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper, never()).patch(any(), any());
  }

  @Test
  void delete_ShouldDeleteTestCase() {
    // When
    sut.delete(projectId, testCaseId);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestCaseId(testCaseId);
    verify(tmsTestCaseVersionService).deleteAllByTestCaseId(testCaseId);
    verify(tmsTestPlanTestCaseRepository).deleteAllByTestCaseId(testCaseId);
    verify(tmsTestCaseRepository).deleteById(testCaseId);
  }

  @Test
  void deleteByTestFolderId_ShouldDeleteAllTestCasesInFolder() {
    // Given
    var folderId = 5L;

    // When
    sut.deleteByTestFolderId(projectId, folderId);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestFolderId(projectId, folderId);
    verify(tmsTestCaseVersionService).deleteAllByTestFolderId(projectId, folderId);
    verify(tmsTestPlanTestCaseRepository).deleteAllByTestFolderId(projectId, folderId);
    verify(tmsTestCaseRepository).deleteTestCasesByFolderId(projectId, folderId);
  }

  @Test
  void delete_WithBatchDeleteRequest_ShouldDeleteAllTestCases() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    // When
    sut.delete(projectId, deleteRequest);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsTestCaseVersionService).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsTestPlanTestCaseRepository).deleteAllByTestCaseIds(testCaseIds);
    verify(tmsTestCaseRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void delete_WithSingleTestCaseId_ShouldDeleteTestCase() {
    // Given
    var singleTestCaseId = List.of(1L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(singleTestCaseId)
        .build();

    // When
    sut.delete(projectId, deleteRequest);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestCaseIds(singleTestCaseId);
    verify(tmsTestCaseVersionService).deleteAllByTestCaseIds(singleTestCaseId);
    verify(tmsTestPlanTestCaseRepository).deleteAllByTestCaseIds(singleTestCaseId);
    verify(tmsTestCaseRepository).deleteAllByTestCaseIds(singleTestCaseId);
  }

  @Test
  void patch_WithBatchPatchRequest_ShouldCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;
    var priority = "HIGH";

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(priority)
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, testFolderId, priority);
  }

  @Test
  void patch_WithOnlyTestFolderId_ShouldOnlyCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(null)
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(any());
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, testFolderId, null);
  }

  @Test
  void patch_WithOnlyPriority_ShouldOnlyCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var priority = "HIGH";

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(null)
        .priority(priority)
        .build();

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(any());
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
    verify(tmsTestFolderService, never()).existsById(any(Long.class), any());
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, null, priority);
  }

  @Test
  void patch_WithNullValuesOnly_ShouldNotCallAnyPatchMethods() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(null)
        .priority(null)
        .build();

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(any());
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
    verify(tmsTestFolderService, never()).existsById(any(Long.class), any());
    verify(tmsTestCaseRepository, never()).patch(any(Long.class), any(), any(), any());
  }

  @Test
  void patch_WithBatchRequestAndNonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 999L;
    var priority = "HIGH";

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(priority)
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class, () -> sut.patch(projectId, patchRequest));

    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository, never()).patch(any(Long.class), any(), any(), any());
    verify(tmsTestCaseRepository, never()).findAllById(any());
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
  }

  @Test
  void patch_NonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 999L;

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class, () -> sut.patch(projectId, patchRequest));

    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository, never()).patch(any(Long.class), any(), any(), any());
  }

  @Test
  void importFromFile_ShouldImportAndCreateTestCases() {
    // Given
    var file = new MockMultipartFile("test.csv", "test content".getBytes());
    var importedTestCaseRQs = List.of(testCaseRQ);

    when(importerFactory.getImporter(file)).thenReturn(importer);
    when(importer.importFromFile(file)).thenReturn(importedTestCaseRQs);

    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.importFromFile(projectId, testFolderId, null, file);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.getFirst());
    verify(importerFactory).getImporter(file);
    verify(importer).importFromFile(file);
    verify(tmsTestFolderService).resolveTestFolderRQ(testCaseRQ, testFolderId, null);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void importFromFile_WithTestFolderName_ShouldImportAndCreateTestCases() {
    // Given
    var file = new MockMultipartFile("test.json", "test content".getBytes());
    var testFolderName = "Test Folder";
    var importedTestCaseRQs = List.of(testCaseRQ);

    when(importerFactory.getImporter(file)).thenReturn(importer);
    when(importer.importFromFile(file)).thenReturn(importedTestCaseRQs);
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class)))
        .thenReturn(testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.importFromFile(projectId, null, testFolderName, file);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.getFirst());
    verify(importerFactory).getImporter(file);
    verify(importer).importFromFile(file);
    verify(tmsTestFolderService).resolveTestFolderRQ(testCaseRQ, null, testFolderName);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void importFromFile_WithBothFolderParameters_ShouldImportAndCreateTestCases() {
    // Given
    var file = new MockMultipartFile("test.json", "test content".getBytes());
    var testFolderName = "Test Folder";
    var importedTestCaseRQs = List.of(testCaseRQ);

    when(importerFactory.getImporter(file)).thenReturn(importer);
    when(importer.importFromFile(file)).thenReturn(importedTestCaseRQs);
    when(tmsTestFolderService.create(eq(projectId), any(NewTestFolderRQ.class))).thenReturn(
        testFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase,
        textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.importFromFile(projectId, testFolderId, testFolderName, file);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.getFirst());
    verify(importerFactory).getImporter(file);
    verify(importer).importFromFile(file);
    verify(tmsTestFolderService).resolveTestFolderRQ(testCaseRQ, testFolderId, testFolderName);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void exportToFile_WithSpecificIds_ShouldExportSpecificTestCases() {
    // Given
    var testCaseIds = List.of(1L, 2L);
    var format = "JSON";
    var includeAttachments = true;

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L)).thenReturn(
        Optional.of(testCase));
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 2L)).thenReturn(
        Optional.of(testCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(testCaseVersion);
    when(tmsTestCaseVersionService.getDefaultVersion(2L)).thenReturn(testCaseVersion);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(1L)).thenReturn(testCaseExecution);
    when(tmsTestCaseExecutionService.getLastTestCaseExecution(2L)).thenReturn(testCaseExecution);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion, testCaseExecution)).thenReturn(testCaseRS);
    when(exporterFactory.getExporter(format)).thenReturn(exporter);

    // When
    sut.exportToFile(projectId, testCaseIds, format, includeAttachments, response);

    // Then
    verify(exporterFactory).getExporter(format);
    verify(exporter).export(any(List.class), eq(includeAttachments), eq(response));
  }

  @Test
  void exportToFile_WithoutIds_ShouldExportAllTestCases() {
    // Given
    var format = "CSV";
    var includeAttachments = false;
    var testCases = List.of(testCase);

    when(tmsTestCaseRepository.findByProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);
    when(exporterFactory.getExporter(format)).thenReturn(exporter);

    // When
    sut.exportToFile(projectId, null, format, includeAttachments, response);

    // Then
    verify(tmsTestCaseRepository).findByProjectId(projectId);
    verify(exporterFactory).getExporter(format);
    verify(exporter).export(any(List.class), eq(includeAttachments), eq(response));
  }

  @Test
  void exportToFile_WithEmptyIds_ShouldExportAllTestCases() {
    // Given
    var emptyIds = Collections.<Long>emptyList();
    var format = "JSON";
    var includeAttachments = false;
    var testCases = List.of(testCase);

    when(tmsTestCaseRepository.findByProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);
    when(exporterFactory.getExporter(format)).thenReturn(exporter);

    // When
    sut.exportToFile(projectId, emptyIds, format, includeAttachments, response);

    // Then
    verify(tmsTestCaseRepository).findByProjectId(projectId);
    verify(exporterFactory).getExporter(format);
    verify(exporter).export(any(List.class), eq(includeAttachments), eq(response));
  }

  @Test
  void getTestCasesByCriteria_WithContent_ShouldReturnPagedResults() {
    // Given
    var filter = mock(Filter.class);
    var pageable = PageRequest.of(0, 10);
    var testCaseIds = List.of(testCaseId);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 1);
    var testCases = List.of(testCase);
    var defaultVersions = Map.of(testCaseId, testCaseVersion);
    var lastExecutions = Map.of(testCaseId, testCaseExecution);
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 1);

    when(tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseExecutionService.getLastTestCasesExecutionsByTestCaseIds(testCaseIds)).thenReturn(lastExecutions);
    when(tmsTestCaseMapper.convert(testCases, defaultVersions, lastExecutions, pageable, 1L)).thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, filter, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter, pageable);
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseExecutionService).getLastTestCasesExecutionsByTestCaseIds(testCaseIds);
    verify(tmsTestCaseMapper).convert(testCases, defaultVersions, lastExecutions, pageable, 1L);
  }

  @Test
  void getTestCasesByCriteria_WithNoContent_ShouldReturnEmptyPage() {
    // Given
    var filter = mock(Filter.class);
    var pageable = PageRequest.of(0, 10);
    var emptyPage = new PageImpl<Long>(Collections.emptyList(), pageable, 0);

    when(tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(emptyPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, filter, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(0, result.getContent().size());
    verify(tmsTestCaseFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter, pageable);
    verify(tmsTestCaseVersionService, never()).getDefaultVersions(any());
    verify(tmsTestCaseRepository, never()).findByProjectIdAndIds(any(Long.class), any());
    verify(tmsTestCaseExecutionService, never()).getLastTestCasesExecutionsByTestCaseIds(any());
    verify(tmsTestCaseMapper, never()).convert(any(List.class), any(Map.class), any(Map.class), any(), any(Long.class));
  }

  @Test
  void getTestCasesByCriteria_WithNullFilter_ShouldReturnPagedResults() {
    // Given
    var pageable = PageRequest.of(0, 20);
    var testCaseIds = List.of(testCaseId);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 1);
    var testCases = List.of(testCase);
    var defaultVersions = Map.of(testCaseId, testCaseVersion);
    var lastExecutions = Map.of(testCaseId, testCaseExecution);
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 1);

    when(tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(eq(projectId), any(), eq(pageable)))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseExecutionService.getLastTestCasesExecutionsByTestCaseIds(testCaseIds)).thenReturn(lastExecutions);
    when(tmsTestCaseMapper.convert(testCases, defaultVersions, lastExecutions, pageable, 1L)).thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, null, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseFilterableRepository).findIdsByProjectIdAndFilter(eq(projectId), any(), eq(pageable));
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseExecutionService).getLastTestCasesExecutionsByTestCaseIds(testCaseIds);
    verify(tmsTestCaseMapper).convert(testCases, defaultVersions, lastExecutions, pageable, 1L);
  }

  @Test
  void getTestCasesByCriteria_WithMultipleTestCases_ShouldReturnOrderedResults() {
    // Given
    var filter = mock(Filter.class);
    var pageable = PageRequest.of(0, 10);

    var testCase1 = new TmsTestCase();
    testCase1.setId(1L);
    var testCase2 = new TmsTestCase();
    testCase2.setId(2L);
    var testCase3 = new TmsTestCase();
    testCase3.setId(3L);

    // IDs в определенном порядке из БД
    var testCaseIds = List.of(3L, 1L, 2L);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 3);

    // Тест-кейсы возвращаются из репозитория в произвольном порядке
    var testCases = List.of(testCase1, testCase2, testCase3);
    var defaultVersions = Map.of(
        1L, testCaseVersion,
        2L, testCaseVersion,
        3L, testCaseVersion
    );
    var lastExecutions = Map.of(
        1L, testCaseExecution,
        2L, testCaseExecution,
        3L, testCaseExecution
    );
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 3);

    when(tmsTestCaseFilterableRepository.findIdsByProjectIdAndFilter(projectId, filter, pageable))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseExecutionService.getLastTestCasesExecutionsByTestCaseIds(testCaseIds)).thenReturn(lastExecutions);
    when(tmsTestCaseMapper.convert(any(List.class), eq(defaultVersions), eq(lastExecutions), eq(pageable), eq(3L)))
        .thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, filter, pageable);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseFilterableRepository).findIdsByProjectIdAndFilter(projectId, filter, pageable);
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseExecutionService).getLastTestCasesExecutionsByTestCaseIds(testCaseIds);
    verify(tmsTestCaseMapper).convert(any(List.class), eq(defaultVersions), eq(lastExecutions), eq(pageable), eq(3L));
  }

  @Test
  void deleteAttributesFromTestCase_WhenTestCaseExists_ShouldDeleteAttributes() {
    // Given
    var attributeIds = Arrays.asList(1L, 2L, 3L);
    when(
        tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(
        true);

    // When
    sut.deleteAttributesFromTestCase(projectId, testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);
  }

  @Test
  void deleteAttributesFromTestCase_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    var attributeIds = Arrays.asList(1L, 2L, 3L);
    when(
        tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(
        false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.deleteAttributesFromTestCase(projectId, testCaseId, attributeIds));

    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdAndAttributeIds(any(), any());
  }

  @Test
  void deleteAttributesFromTestCase_WithSingleAttribute_ShouldDeleteAttributes() {
    // Given
    var attributeIds = List.of(1L);
    when(
        tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(
        true);

    // When
    sut.deleteAttributesFromTestCase(projectId, testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);
  }

  @Test
  void patchTestCaseAttributes_WithBothAddAndRemove_ShouldExecuteBothOperations() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToRemove = Arrays.asList(1L, 2L);
    var attributesToAdd = Arrays.asList(4L, 5L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributesToAdd)
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(
        eq(testCaseIds), eq(Set.of(1L, 2L)));
    verify(tmsTestCaseAttributeService).addAttributesToTestCases(
        eq(testCaseIds), eq(Set.of(4L, 5L)));
  }

  @Test
  void patchTestCaseAttributes_WithOnlyAdd_ShouldOnlyExecuteAddOperation() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToAdd = Arrays.asList(4L, 5L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(null)
        .attributeIdsToAdd(attributesToAdd)
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
    verify(tmsTestCaseAttributeService).addAttributesToTestCases(
        eq(testCaseIds), eq(Set.of(4L, 5L)));
  }

  @Test
  void patchTestCaseAttributes_WithOnlyRemove_ShouldOnlyExecuteRemoveOperation() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToRemove = Arrays.asList(1L, 2L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(null)
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(
        eq(testCaseIds), eq(Set.of(1L, 2L)));
    verify(tmsTestCaseAttributeService, never()).addAttributesToTestCases(any(), any());
  }

  @Test
  void patchTestCaseAttributes_WithIntersectingAttributes_ShouldExcludeIntersection() {
    // Given - тестовый случай из требований: attributesToRemove: [1,2,3], attributesToAdd: [2,3,4]
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToRemove = Arrays.asList(1L, 2L, 3L);
    var attributesToAdd = Arrays.asList(2L, 3L, 4L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributesToAdd)
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    // Должен удалить только атрибут 1 (исключив пересечение 2,3)
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(
        eq(testCaseIds), eq(Set.of(1L)));
    // Должен добавить только атрибут 4 (исключив пересечение 2,3)
    verify(tmsTestCaseAttributeService).addAttributesToTestCases(
        eq(testCaseIds), eq(Set.of(4L)));
  }

  @Test
  void patchTestCaseAttributes_WithEmptyLists_ShouldNotExecuteAnyOperations() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(Collections.emptyList())
        .attributeIdsToAdd(Collections.emptyList())
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
    verify(tmsTestCaseAttributeService, never()).addAttributesToTestCases(any(), any());
  }

  @Test
  void patchTestCaseAttributes_WithNonExistentTestCase_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToAdd = Arrays.asList(4L, 5L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(null)
        .attributeIdsToAdd(attributesToAdd)
        .build();

    var existingTestCaseIds = List.of(1L, 2L); // Отсутствует ID 3L

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.patchTestCaseAttributes(projectId, patchRequest));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
    verify(tmsTestCaseAttributeService, never()).addAttributesToTestCases(any(), any());
  }

  @Test
  void patchTestCaseAttributes_WithCompleteIntersection_ShouldNotExecuteAnyOperations() {
    // Given - случай когда все атрибуты пересекаются
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToRemove = Arrays.asList(1L, 2L);
    var attributesToAdd = Arrays.asList(1L, 2L);

    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributesToAdd)
        .build();

    var existingTestCaseIds = List.of(1L, 2L, 3L);
    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.patchTestCaseAttributes(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
    verify(tmsTestCaseAttributeService, never()).addAttributesToTestCases(any(), any());
  }

  @Test
  void validateTestCasesExist_WhenAllTestCasesExist_ShouldNotThrowException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var existingTestCaseIds = List.of(1L, 2L, 3L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    assertDoesNotThrow(() -> sut.validateTestCasesExist(projectId, testCaseIds));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void validateTestCasesExist_WhenSomeTestCasesDoNotExist_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var existingTestCaseIds = List.of(1L, 2L); // Missing ID 3L

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.validateTestCasesExist(projectId, testCaseIds));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void validateTestCasesExist_WhenNoTestCasesExist_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.validateTestCasesExist(projectId, testCaseIds));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void validateTestCasesExist_WithSingleTestCase_WhenExists_ShouldNotThrowException() {
    // Given
    var testCaseIds = List.of(1L);
    var existingTestCaseIds = List.of(1L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    assertDoesNotThrow(() -> sut.validateTestCasesExist(projectId, testCaseIds));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void validateTestCasesExist_WithSingleTestCase_WhenDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = List.of(1L);
    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.validateTestCasesExist(projectId, testCaseIds));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void validateTestCasesExist_WithEmptyList_ShouldNotThrowException() {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();
    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, emptyTestCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    assertDoesNotThrow(() -> sut.validateTestCasesExist(projectId, emptyTestCaseIds));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, emptyTestCaseIds);
  }

  @Test
  void validateTestCasesExist_WithMultipleNonExistentTestCases_ShouldThrowExceptionWithAllMissingIds() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    var existingTestCaseIds = List.of(2L, 4L); // Missing IDs: 1L, 3L, 5L

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.validateTestCasesExist(projectId, testCaseIds));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    assertTrue(exception.getMessage().contains("1"));
    assertTrue(exception.getMessage().contains("3"));
    assertTrue(exception.getMessage().contains("5"));
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
  }

  @Test
  void duplicate_WithValidTestCaseIds_ShouldDuplicateTestCases() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var targetFolderId = 10L;
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(targetFolderId)
        .build();

    var existingTestCaseIds = List.of(1L, 2L);
    var duplicatedTestCaseRS1 = new TmsTestCaseRS();
    duplicatedTestCaseRS1.setId(11L);
    var duplicatedTestCaseRS2 = new TmsTestCaseRS();
    duplicatedTestCaseRS2.setId(12L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);
    when(tmsTestFolderService.resolveTargetFolderId(projectId, targetFolderId, null))
        .thenReturn(targetFolderId);

    // Mock для первого тест-кейса
    var originalTestCase1 = new TmsTestCase();
    originalTestCase1.setId(1L);
    originalTestCase1.setName("Test Case 1");
    originalTestCase1.setTestFolder(testFolder);
    var duplicatedTestCase1 = new TmsTestCase();
    duplicatedTestCase1.setId(11L);
    duplicatedTestCase1.setName("Test Case 1 (Copy)");
    var originalVersion1 = new TmsTestCaseVersion();
    var duplicatedVersion1 = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase1));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion1);
    when(tmsTestFolderService.getEntityById(projectId, targetFolderId)).thenReturn(testFolder);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase1, testFolder))
        .thenReturn(duplicatedTestCase1);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase1)).thenReturn(duplicatedTestCase1);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase1, originalVersion1))
        .thenReturn(duplicatedVersion1);
    when(tmsTestCaseMapper.convert(duplicatedTestCase1, duplicatedVersion1))
        .thenReturn(duplicatedTestCaseRS1);

    // Mock для второго тест-кейса
    var originalTestCase2 = new TmsTestCase();
    originalTestCase2.setId(2L);
    originalTestCase2.setName("Test Case 2");
    originalTestCase2.setTestFolder(testFolder);
    var duplicatedTestCase2 = new TmsTestCase();
    duplicatedTestCase2.setId(12L);
    duplicatedTestCase2.setName("Test Case 2 (Copy)");
    var originalVersion2 = new TmsTestCaseVersion();
    var duplicatedVersion2 = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 2L))
        .thenReturn(Optional.of(originalTestCase2));
    when(tmsTestCaseVersionService.getDefaultVersion(2L)).thenReturn(originalVersion2);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase2, testFolder))
        .thenReturn(duplicatedTestCase2);
    when(tmsTestCaseRepository.save(duplicatedTestCase2)).thenReturn(duplicatedTestCase2);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase2, originalVersion2))
        .thenReturn(duplicatedVersion2);
    when(tmsTestCaseMapper.convert(duplicatedTestCase2, duplicatedVersion2))
        .thenReturn(duplicatedTestCaseRS2);

    // When
    var result = sut.duplicate(projectId, duplicateRequest);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(duplicatedTestCaseRS1.getId(), result.get(0).getId());
    assertEquals(duplicatedTestCaseRS2.getId(), result.get(1).getId());

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestFolderService).resolveTargetFolderId(projectId, targetFolderId, null);
    verify(tmsTestCaseRepository).save(duplicatedTestCase1);
    verify(tmsTestCaseRepository).save(duplicatedTestCase2);
    verify(tmsTestCaseVersionService).duplicateDefaultVersion(duplicatedTestCase1,
        originalVersion1);
    verify(tmsTestCaseVersionService).duplicateDefaultVersion(duplicatedTestCase2,
        originalVersion2);
  }

  @Test
  void duplicate_WithTestFolder_ShouldDuplicateTestCases() {
    // Given
    var testCaseIds = List.of(1L);
    var testFolder = new NewTestFolderRQ();
    testFolder.setName("New Target Folder");
    var targetFolderId = 15L;

    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolder(testFolder)
        .build();

    var existingTestCaseIds = List.of(1L);
    var duplicatedTestCaseRS = new TmsTestCaseRS();
    duplicatedTestCaseRS.setId(20L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);
    when(tmsTestFolderService.resolveTargetFolderId(projectId, null, testFolder))
        .thenReturn(targetFolderId);

    var originalTestCase = new TmsTestCase();
    originalTestCase.setId(1L);
    originalTestCase.setName("Original Test Case");
    originalTestCase.setTestFolder(this.testFolder);
    var duplicatedTestCase = new TmsTestCase();
    duplicatedTestCase.setId(20L);
    duplicatedTestCase.setName("Original Test Case (Copy)");
    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestFolderService.getEntityById(projectId, targetFolderId)).thenReturn(this.testFolder);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase, this.testFolder))
        .thenReturn(duplicatedTestCase);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase)).thenReturn(duplicatedTestCase);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedVersion))
        .thenReturn(duplicatedTestCaseRS);

    // When
    var result = sut.duplicate(projectId, duplicateRequest);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(duplicatedTestCaseRS.getId(), result.get(0).getId());

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestFolderService).resolveTargetFolderId(projectId, null, testFolder);
    verify(tmsTestCaseRepository).save(duplicatedTestCase);
    verify(tmsTestCaseVersionService).duplicateDefaultVersion(duplicatedTestCase, originalVersion);
  }

  @Test
  void duplicate_WithEmptyTagsTaggedTestCase_ShouldDuplicateTestCaseWithoutEmptyTags() {
    // Given
    var testCaseIds = List.of(1L);
    var targetFolderId = 10L;
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(targetFolderId)
        .build();

    var existingTestCaseIds = List.of(1L);
    var duplicatedTestCaseRS = new TmsTestCaseRS();
    duplicatedTestCaseRS.setId(11L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);
    when(tmsTestFolderService.resolveTargetFolderId(projectId, targetFolderId, null))
        .thenReturn(targetFolderId);

    var originalTestCase = new TmsTestCase();
    originalTestCase.setId(1L);
    originalTestCase.setAttributes(Set.of()); // Empty tags
    originalTestCase.setTestFolder(testFolder);
    var duplicatedTestCase = new TmsTestCase();
    duplicatedTestCase.setId(11L);
    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestFolderService.getEntityById(projectId, targetFolderId)).thenReturn(testFolder);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase, testFolder))
        .thenReturn(duplicatedTestCase);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase)).thenReturn(duplicatedTestCase);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedVersion))
        .thenReturn(duplicatedTestCaseRS);

    // When
    var result = sut.duplicate(projectId, duplicateRequest);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestFolderService).resolveTargetFolderId(projectId, targetFolderId, null);
    verify(tmsTestCaseRepository).save(duplicatedTestCase);
    verify(tmsTestCaseVersionService).duplicateDefaultVersion(duplicatedTestCase, originalVersion);
    verify(tmsTestCaseAttributeService, never()).duplicateTestCaseAttributes(any(), any());
  }

  @Test
  void duplicate_WithNonExistentTestCase_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = List.of(999L);
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(10L)
        .build();

    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    assertThrows(ReportPortalException.class, () -> sut.duplicate(projectId, duplicateRequest));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestFolderService, never()).resolveTargetFolderId(any(Long.class), any(), any());
    verify(tmsTestCaseRepository, never()).save(any());
  }

  @Test
  void duplicate_WithMissingOriginalTestCase_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = List.of(1L);
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(10L)
        .build();

    var existingTestCaseIds = List.of(1L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);
    when(tmsTestFolderService.resolveTargetFolderId(projectId, 10L, null))
        .thenReturn(10L);
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class, () -> sut.duplicate(projectId, duplicateRequest));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestFolderService).resolveTargetFolderId(projectId, 10L, null);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, 1L);
    verify(tmsTestCaseRepository, never()).save(any());
  }

  @Test
  void duplicate_WithEmptyTestCaseIds_ShouldReturnEmptyList() {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(emptyTestCaseIds)
        .testFolderId(10L)
        .build();

    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, emptyTestCaseIds))
        .thenReturn(existingTestCaseIds);
    when(tmsTestFolderService.resolveTargetFolderId(projectId, 10L, null))
        .thenReturn(10L);

    // When
    var result = sut.duplicate(projectId, duplicateRequest);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, emptyTestCaseIds);
    verify(tmsTestFolderService).resolveTargetFolderId(projectId, 10L, null);
    verify(tmsTestCaseRepository, never()).save(any());
  }

  @Test
  void duplicateTestCase_WithUniqueNameGeneration_ShouldGenerateUniqueName() {
    // Given
    var originalTestCase = new TmsTestCase();
    originalTestCase.setId(1L);
    originalTestCase.setName("Test Case");
    originalTestCase.setTestFolder(testFolder);

    var duplicatedTestCase = new TmsTestCase();
    duplicatedTestCase.setId(11L);
    duplicatedTestCase.setName("Test Case-copy");

    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestFolderService.getEntityById(projectId, testFolderId)).thenReturn(testFolder);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase, testFolder))
        .thenReturn(duplicatedTestCase);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(projectId, "Test Case-copy", testFolderId))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase)).thenReturn(duplicatedTestCase);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedVersion))
        .thenReturn(testCaseRS);

    // When
    var result = sut.duplicateTestCase(projectId, 1L, testFolderId);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseRepository).existsByNameAndTestFolder(projectId, "Test Case-copy", testFolderId);
    verify(tmsTestCaseRepository).save(duplicatedTestCase);
  }

  @Test
  void duplicateTestCase_WithExistingName_ShouldGenerateIncrementalName() {
    // Given
    var originalTestCase = new TmsTestCase();
    originalTestCase.setId(1L);
    originalTestCase.setName("Test Case");
    originalTestCase.setTestFolder(testFolder);

    var duplicatedTestCase = new TmsTestCase();
    duplicatedTestCase.setId(11L);

    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestFolderService.getEntityById(projectId, testFolderId)).thenReturn(testFolder);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase, testFolder))
        .thenReturn(duplicatedTestCase);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(projectId, "Test Case-copy", testFolderId))
        .thenReturn(true);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(projectId, "Test Case-copy-1", testFolderId))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase)).thenReturn(duplicatedTestCase);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.convert(duplicatedTestCase, duplicatedVersion))
        .thenReturn(testCaseRS);

    // When
    var result = sut.duplicateTestCase(projectId, 1L, testFolderId);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseRepository).existsByNameAndTestFolder(projectId, "Test Case-copy", testFolderId);
    verify(tmsTestCaseRepository).existsByNameAndTestFolder(projectId, "Test Case-copy-1", testFolderId);
    verify(tmsTestCaseRepository).save(duplicatedTestCase);
  }

  @Test
  void duplicateTestCases_WithAllSuccessful_ShouldReturnSuccessResult() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);

    var originalTestCase1 = new TmsTestCase();
    originalTestCase1.setId(1L);
    originalTestCase1.setName("Test Case 1");
    originalTestCase1.setTestFolder(testFolder);

    var duplicatedTestCase1 = new TmsTestCase();
    duplicatedTestCase1.setId(11L);
    duplicatedTestCase1.setName("Test Case 1-copy");

    var originalTestCase2 = new TmsTestCase();
    originalTestCase2.setId(2L);
    originalTestCase2.setName("Test Case 2");
    originalTestCase2.setTestFolder(testFolder);

    var duplicatedTestCase2 = new TmsTestCase();
    duplicatedTestCase2.setId(12L);
    duplicatedTestCase2.setName("Test Case 2-copy");

    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    var expectedResult = new BatchTestCaseOperationResultRS();
    expectedResult.setSuccessTestCaseIds(Arrays.asList(11L, 12L));
    expectedResult.setErrors(Collections.emptyList());

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase1));
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 2L))
        .thenReturn(Optional.of(originalTestCase2));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestCaseVersionService.getDefaultVersion(2L)).thenReturn(originalVersion);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase1, testFolder))
        .thenReturn(duplicatedTestCase1);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase2, testFolder))
        .thenReturn(duplicatedTestCase2);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(any(TmsTestCase.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(tmsTestCaseVersionService.duplicateDefaultVersion(any(TmsTestCase.class), eq(originalVersion)))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.toBatchOperationResult(Arrays.asList(11L, 12L), Collections.emptyList()))
        .thenReturn(expectedResult);

    // When
    var result = sut.duplicateTestCases(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getSuccessTestCaseIds().size());
    assertTrue(result.getErrors().isEmpty());
    verify(tmsTestCaseRepository, times(2)).save(any(TmsTestCase.class));
    verify(tmsTestCaseMapper).toBatchOperationResult(Arrays.asList(11L, 12L), Collections.emptyList());
  }

  @Test
  void duplicateTestCases_WithSomeFailures_ShouldReturnPartialResult() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);

    var originalTestCase1 = new TmsTestCase();
    originalTestCase1.setId(1L);
    originalTestCase1.setName("Test Case 1");
    originalTestCase1.setTestFolder(testFolder);

    var duplicatedTestCase1 = new TmsTestCase();
    duplicatedTestCase1.setId(11L);
    duplicatedTestCase1.setName("Test Case 1-copy");

    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    var expectedResult = new BatchTestCaseOperationResultRS();
    expectedResult.setSuccessTestCaseIds(List.of(11L));
    expectedResult.setErrors(new ArrayList<>());

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase1));
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 2L))
        .thenReturn(Optional.empty());
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase1, testFolder))
        .thenReturn(duplicatedTestCase1);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase1)).thenReturn(duplicatedTestCase1);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase1, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.toBatchOperationResult(eq(List.of(11L)), anyList()))
        .thenReturn(expectedResult);

    // When
    var result = sut.duplicateTestCases(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getSuccessTestCaseIds().size());
    assertEquals(11L, result.getSuccessTestCaseIds().getFirst());
    verify(tmsTestCaseRepository).save(duplicatedTestCase1);
    verify(tmsTestCaseMapper).toBatchOperationResult(eq(List.of(11L)), anyList());
  }

  @Test
  void duplicateTestCases_WithAttributes_ShouldDuplicateAttributes() {
    // Given
    var testCaseIds = List.of(1L);

    var originalTestCase = new TmsTestCase();
    originalTestCase.setId(1L);
    originalTestCase.setName("Test Case");
    originalTestCase.setTestFolder(testFolder);
    originalTestCase.setAttributes(Set.of(new com.epam.ta.reportportal.entity.tms.TmsTestCaseAttribute()));

    var duplicatedTestCase = new TmsTestCase();
    duplicatedTestCase.setId(11L);
    duplicatedTestCase.setName("Test Case-copy");

    var originalVersion = new TmsTestCaseVersion();
    var duplicatedVersion = new TmsTestCaseVersion();

    var expectedResult = new BatchTestCaseOperationResultRS();
    expectedResult.setSuccessTestCaseIds(List.of(11L));
    expectedResult.setErrors(Collections.emptyList());

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L))
        .thenReturn(Optional.of(originalTestCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(originalVersion);
    when(tmsTestCaseMapper.duplicateTestCase(originalTestCase, testFolder))
        .thenReturn(duplicatedTestCase);
    when(tmsTestCaseRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(testFolderId)))
        .thenReturn(false);
    when(tmsTestCaseRepository.save(duplicatedTestCase)).thenReturn(duplicatedTestCase);
    when(tmsTestCaseVersionService.duplicateDefaultVersion(duplicatedTestCase, originalVersion))
        .thenReturn(duplicatedVersion);
    when(tmsTestCaseMapper.toBatchOperationResult(List.of(11L), Collections.emptyList()))
        .thenReturn(expectedResult);

    // When
    var result = sut.duplicateTestCases(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    verify(tmsTestCaseAttributeService).duplicateTestCaseAttributes(originalTestCase, duplicatedTestCase);
    verify(tmsTestCaseMapper).toBatchOperationResult(List.of(11L), Collections.emptyList());
  }

  @Test
  void existsById_WhenTestCaseExists_ShouldReturnTrue() {
    // Given
    when(tmsTestCaseRepository.existsByIdAndProjectId(testCaseId, projectId)).thenReturn(true);

    // When
    var result = sut.existsById(projectId, testCaseId);

    // Then
    assertTrue(result);
    verify(tmsTestCaseRepository).existsByIdAndProjectId(testCaseId, projectId);
  }

  @Test
  void existsById_WhenTestCaseDoesNotExist_ShouldReturnFalse() {
    // Given
    when(tmsTestCaseRepository.existsByIdAndProjectId(testCaseId, projectId)).thenReturn(false);

    // When
    var result = sut.existsById(projectId, testCaseId);

    // Then
    assertFalse(result);
    verify(tmsTestCaseRepository).existsByIdAndProjectId(testCaseId, projectId);
  }

  @Test
  void getExistingTestCaseIds_WithNullList_ShouldReturnEmptyList() {
    // When
    var result = sut.getExistingTestCaseIds(projectId, null);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseRepository, never()).findExistingTestCaseIds(any(), any());
  }

  @Test
  void getExistingTestCaseIds_WithEmptyList_ShouldReturnEmptyList() {
    // Given
    var emptyList = Collections.<Long>emptyList();

    // When
    var result = sut.getExistingTestCaseIds(projectId, emptyList);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseRepository, never()).findExistingTestCaseIds(any(), any());
  }

  @Test
  void getExistingTestCaseIds_WithAllExistingIds_ShouldReturnAllIds() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var existingIds = Arrays.asList(1L, 2L, 3L);

    when(tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(existingIds);

    // When
    var result = sut.getExistingTestCaseIds(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.containsAll(existingIds));
    verify(tmsTestCaseRepository).findExistingTestCaseIds(projectId, testCaseIds);
  }

  @Test
  void getExistingTestCaseIds_WithSomeExistingIds_ShouldReturnOnlyExistingIds() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L, 4L);
    var existingIds = Arrays.asList(1L, 3L);

    when(tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(existingIds);

    // When
    var result = sut.getExistingTestCaseIds(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(1L));
    assertTrue(result.contains(3L));
    assertFalse(result.contains(2L));
    assertFalse(result.contains(4L));
    verify(tmsTestCaseRepository).findExistingTestCaseIds(projectId, testCaseIds);
  }

  @Test
  void getExistingTestCaseIds_WithNoExistingIds_ShouldReturnEmptyList() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var existingIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(existingIds);

    // When
    var result = sut.getExistingTestCaseIds(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tmsTestCaseRepository).findExistingTestCaseIds(projectId, testCaseIds);
  }

  @Test
  void getExistingTestCaseIds_WithSingleId_ShouldReturnCorrectResult() {
    // Given
    var testCaseIds = List.of(1L);
    var existingIds = List.of(1L);

    when(tmsTestCaseRepository.findExistingTestCaseIds(projectId, testCaseIds))
        .thenReturn(existingIds);

    // When
    var result = sut.getExistingTestCaseIds(projectId, testCaseIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst());
    verify(tmsTestCaseRepository).findExistingTestCaseIds(projectId, testCaseIds);
  }
}
