package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestPlanTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestCaseMapper;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseExporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestCaseImporterFactory;
import com.epam.ta.reportportal.core.tms.mapper.importer.TmsTestCaseImporter;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestCaseExporter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private HttpServletResponse response;

  @InjectMocks
  private TmsTestCaseServiceImpl sut;

  private TmsTestCaseRQ testCaseRQ;
  private TmsTestCase testCase;
  private TmsTestCaseRS testCaseRS;
  private TmsTestCaseVersion testCaseVersion;
  private TmsTextManualScenarioRQ textManualScenarioRQ;
  private TmsStepsManualScenarioRQ stepsManualScenarioRQ;
  private TmsTestCaseTestFolderRQ testFolderRQ;
  private TmsTestFolderRS testFolderRS;
  private List<TmsAttributeRQ> attributes;
  private long projectId;
  private Long testCaseId;
  private Long testFolderId;
  private Long testPlanId;

  @BeforeEach
  void setUp() {
    projectId = 1L;
    testCaseId = 2L;
    testFolderId = 4L;
    testPlanId = 5L;

    attributes = new ArrayList<>();
    var attribute = new TmsAttributeRQ();
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

    testFolderRQ = new TmsTestCaseTestFolderRQ();
    testFolderRQ.setId(testFolderId);

    testFolderRS = new TmsTestFolderRS();
    testFolderRS.setId(testFolderId);

    testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case");
    testCaseRQ.setDescription("Description");
    testCaseRQ.setTestFolder(testFolderRQ);
    testCaseRQ.setTags(attributes);
    testCaseRQ.setManualScenario(textManualScenarioRQ);

    testCase = new TmsTestCase();
    testCase.setId(testCaseId);
    testCase.setName("Test Case");
    testCase.setDescription("Description");

    testCaseVersion = new TmsTestCaseVersion();
    testCaseVersion.setId(1L);
    testCaseVersion.setTestCase(testCase);

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
    when(tmsTestCaseRepository.findByTestFolder_ProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.getTestCaseByProjectId(projectId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.get(0));
    verify(tmsTestCaseRepository).findByTestFolder_ProjectId(projectId);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void getById_WhenTestCaseExists_ShouldReturnTestCase() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseVersionService.getDefaultVersion(testCaseId)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.getById(projectId, testCaseId);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestCaseVersionService).getDefaultVersion(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void getById_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.getById(projectId, testCaseId));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
  }

  @Test
  void create_ShouldCreateAndReturnTestCase() {
    // Given
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
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
    testCaseWithStepsRQ.setTestFolder(testFolderRQ);
    testCaseWithStepsRQ.setTags(attributes);
    testCaseWithStepsRQ.setManualScenario(stepsManualScenarioRQ);

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseWithStepsRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, stepsManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseWithStepsRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
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
    var newTestFolderRQ = new TmsTestCaseTestFolderRQ();
    newTestFolderRQ.setName(testFolderName);

    var testCaseRQWithNewFolder = new TmsTestCaseRQ();
    testCaseRQWithNewFolder.setName("Test Case");
    testCaseRQWithNewFolder.setTestFolder(newTestFolderRQ);
    testCaseRQWithNewFolder.setTags(attributes);
    testCaseRQWithNewFolder.setManualScenario(textManualScenarioRQ);

    var newFolderId = 10L;
    var newFolderRS = new TmsTestFolderRS();
    newFolderRS.setId(newFolderId);

    when(tmsTestFolderService.create(projectId, testFolderName)).thenReturn(newFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQWithNewFolder, newFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQWithNewFolder);

    // Then
    assertNotNull(result);
    verify(tmsTestFolderService).create(projectId, testFolderName);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQWithNewFolder, newFolderId);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void create_WithNonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.create(projectId, testCaseRQ));
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository, never()).save(any());
  }

  @Test
  void create_WithoutTags_ShouldNotCreateAttributes() {
    // Given
    var testCaseRQWithoutTags = new TmsTestCaseRQ();
    testCaseRQWithoutTags.setName("Test Case");
    testCaseRQWithoutTags.setTestFolder(testFolderRQ);
    testCaseRQWithoutTags.setManualScenario(textManualScenarioRQ);

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQWithoutTags, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
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
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseVersionService.updateDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).update(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).updateTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).updateDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void update_WhenTestCaseDoesNotExist_ShouldCreateNewTestCase() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.empty());
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void update_WithNonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.update(projectId, testCaseId, testCaseRQ));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper, never()).update(any(), any());
  }

  @Test
  void patch_WhenTestCaseExists_ShouldPatchAndReturnTestCase() {
    // Given
    var convertedTestCase = new TmsTestCase();
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseVersionService.patchDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.patch(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).patch(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).patchDefaultTestCaseVersion(testCase, textManualScenarioRQ);
    verify(tmsTestCaseMapper).convert(testCase, testCaseVersion);
  }

  @Test
  void patch_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, testCaseId, testCaseRQ));
    verify(tmsTestCaseRepository).findByProjectIdAndId(projectId, testCaseId);
  }

  @Test
  void patch_WithBatchRequestTagsAndNonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, testCaseId, testCaseRQ));
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
  void patch_WithBatchPatchRequestAndTags_ShouldPatchTagsAndCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;
    var priority = "HIGH";
    var tags = List.of(attributes.get(0));
    var testCases = Arrays.asList(testCase);

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(priority)
        .tags(tags)
        .build();

    when(tmsTestCaseRepository.findAllById(testCaseIds)).thenReturn(testCases);
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCases, tags);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, testFolderId, priority);
  }

  @Test
  void patch_WithBatchPatchRequestWithoutTags_ShouldOnlyCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;
    var priority = "HIGH";

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(priority)
        .tags(null)
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, testFolderId, priority);
  }

  @Test
  void patch_WithBatchPatchRequestWithEmptyTags_ShouldOnlyCallRepositoryPatch() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;
    var priority = "HIGH";

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(priority)
        .tags(Collections.emptyList())
        .build();

    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).patch(projectId, testCaseIds, testFolderId, priority);
  }

  @Test
  void patch_WithOnlyTags_ShouldOnlyPatchTags() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var tags = List.of(attributes.get(0));
    var testCases = Arrays.asList(testCase);

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(null)
        .priority(null)
        .tags(tags)
        .build();

    when(tmsTestCaseRepository.findAllById(testCaseIds)).thenReturn(testCases);

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCases, tags);
    verify(tmsTestFolderService, never()).existsById(any(Long.class), any());
    verify(tmsTestCaseRepository, never()).patch(any(Long.class), any(), any(), any());
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
        .tags(null)
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
        .tags(null)
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
        .tags(null)
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
        .tags(null)
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
  void patch_WithTagsAndNonExistentTestFolder_ShouldThrowReportPortalException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 999L;
    var tags = List.of(attributes.getFirst());
    var testCases = Collections.singletonList(testCase);

    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .priority(null)
        .tags(tags)
        .build();

    when(tmsTestCaseRepository.findAllById(testCaseIds)).thenReturn(testCases);
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class, () -> sut.patch(projectId, patchRequest));

    verify(tmsTestCaseRepository).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCases, tags);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository, never()).patch(any(Long.class), any(), any(), any());
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
  void importFromFile_ShouldImportAndCreateTestCases() {
    // Given
    var file = new MockMultipartFile("test.csv", "test content".getBytes());
    var importedTestCaseRQs = List.of(testCaseRQ);

    when(importerFactory.getImporter(file)).thenReturn(importer);
    when(importer.importFromFile(file)).thenReturn(importedTestCaseRQs);
    when(tmsTestFolderService.existsById(projectId, testFolderId)).thenReturn(true);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseVersionService.createDefaultTestCaseVersion(testCase, textManualScenarioRQ)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);

    // When
    var result = sut.importFromFile(projectId, file);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.get(0));
    verify(importerFactory).getImporter(file);
    verify(importer).importFromFile(file);
    verify(tmsTestFolderService).existsById(projectId, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
  }

  @Test
  void exportToFile_WithSpecificIds_ShouldExportSpecificTestCases() {
    // Given
    var testCaseIds = List.of(1L, 2L);
    var format = "JSON";
    var includeAttachments = true;

    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 1L)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseRepository.findByProjectIdAndId(projectId, 2L)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseVersionService.getDefaultVersion(1L)).thenReturn(testCaseVersion);
    when(tmsTestCaseVersionService.getDefaultVersion(2L)).thenReturn(testCaseVersion);
    when(tmsTestCaseMapper.convert(testCase, testCaseVersion)).thenReturn(testCaseRS);
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

    when(tmsTestCaseRepository.findByTestFolder_ProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);
    when(exporterFactory.getExporter(format)).thenReturn(exporter);

    // When
    sut.exportToFile(projectId, null, format, includeAttachments, response);

    // Then
    verify(tmsTestCaseRepository).findByTestFolder_ProjectId(projectId);
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

    when(tmsTestCaseRepository.findByTestFolder_ProjectId(projectId)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);
    when(exporterFactory.getExporter(format)).thenReturn(exporter);

    // When
    sut.exportToFile(projectId, emptyIds, format, includeAttachments, response);

    // Then
    verify(tmsTestCaseRepository).findByTestFolder_ProjectId(projectId);
    verify(exporterFactory).getExporter(format);
    verify(exporter).export(any(List.class), eq(includeAttachments), eq(response));
  }

  @Test
  void getTestCasesByCriteria_WithContent_ShouldReturnPagedResults() {
    // Given
    var search = "test search";
    var testFolderId = 5L;
    var pageable = PageRequest.of(0, 10);
    var testCaseIds = List.of(testCaseId);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 1);
    var testCases = List.of(testCase);
    var defaultVersions = Map.of(testCaseId, testCaseVersion);
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 1);

    when(tmsTestCaseRepository.findIdsByCriteria(projectId, search, testFolderId, testPlanId,
        pageable))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCases, defaultVersions, pageable)).thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, search, testFolderId, testPlanId, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseRepository).findIdsByCriteria(projectId, search, testFolderId, testPlanId,
        pageable);
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseMapper).convert(testCases, defaultVersions, pageable);
  }

  @Test
  void getTestCasesByCriteria_WithNoContent_ShouldReturnEmptyPage() {
    // Given
    var search = "test search";
    var testFolderId = 5L;
    var pageable = PageRequest.of(0, 10);
    var emptyPage = new PageImpl<Long>(Collections.emptyList(), pageable, 0);

    when(tmsTestCaseRepository.findIdsByCriteria(projectId, search, testFolderId, testPlanId,
        pageable))
        .thenReturn(emptyPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, search, testFolderId, testPlanId, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(0, result.getContent().size());
    verify(tmsTestCaseRepository).findIdsByCriteria(projectId, search, testFolderId, testPlanId,
        pageable);
    verify(tmsTestCaseVersionService, never()).getDefaultVersions(any());
    verify(tmsTestCaseRepository, never()).findByProjectIdAndIds(any(Long.class), any());
    verify(tmsTestCaseMapper, never()).convert(any(List.class), any(Map.class), any());
  }

  @Test
  void getTestCasesByCriteria_WithNullParameters_ShouldReturnPagedResults() {
    // Given
    var pageable = PageRequest.of(0, 20);
    var testCaseIds = List.of(testCaseId);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 1);
    var testCases = List.of(testCase);
    var defaultVersions = Map.of(testCaseId, testCaseVersion);
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 1);

    when(tmsTestCaseRepository.findIdsByCriteria(projectId, null, null, null, pageable))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCases, defaultVersions, pageable)).thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, null, null, null, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseRepository).findIdsByCriteria(projectId, null, null, null, pageable);
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseMapper).convert(testCases, defaultVersions, pageable);
  }

  @Test
  void getTestCasesByCriteria_WithTestPlanIdOnly_ShouldReturnFilteredResults() {
    // Given
    var pageable = PageRequest.of(0, 10);
    var testCaseIds = List.of(testCaseId);
    var testCaseIdsPage = new PageImpl<>(testCaseIds, pageable, 1);
    var testCases = List.of(testCase);
    var defaultVersions = Map.of(testCaseId, testCaseVersion);
    var convertedPage = new PageImpl<>(List.of(testCaseRS), pageable, 1);

    when(tmsTestCaseRepository.findIdsByCriteria(projectId, null, null, testPlanId, pageable))
        .thenReturn(testCaseIdsPage);
    when(tmsTestCaseVersionService.getDefaultVersions(testCaseIds)).thenReturn(defaultVersions);
    when(tmsTestCaseRepository.findByProjectIdAndIds(projectId, testCaseIds)).thenReturn(testCases);
    when(tmsTestCaseMapper.convert(testCases, defaultVersions, pageable)).thenReturn(convertedPage);

    // When
    var result = sut.getTestCasesByCriteria(projectId, null, null, testPlanId, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseRepository).findIdsByCriteria(projectId, null, null, testPlanId, pageable);
    verify(tmsTestCaseVersionService).getDefaultVersions(testCaseIds);
    verify(tmsTestCaseRepository).findByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseMapper).convert(testCases, defaultVersions, pageable);
  }

  @Test
  void deleteTagsFromTestCase_WhenTestCaseExists_ShouldDeleteTags() {
    // Given
    var attributeIds = Arrays.asList(1L, 2L, 3L);
    when(tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(true);

    // When
    sut.deleteTagsFromTestCase(projectId, testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);
  }

  @Test
  void deleteTagsFromTestCase_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    var attributeIds = Arrays.asList(1L, 2L, 3L);
    when(tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(false);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.deleteTagsFromTestCase(projectId, testCaseId, attributeIds));

    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdAndAttributeIds(any(), any());
  }

  @Test
  void deleteTagsFromTestCase_WithSingleAttribute_ShouldDeleteTags() {
    // Given
    var attributeIds = List.of(1L);
    when(tmsTestCaseRepository.existsByTestFolder_Project_IdAndId(projectId, testCaseId)).thenReturn(true);

    // When
    sut.deleteTagsFromTestCase(projectId, testCaseId, attributeIds);

    // Then
    verify(tmsTestCaseRepository).existsByTestFolder_Project_IdAndId(projectId, testCaseId);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdAndAttributeIds(testCaseId, attributeIds);
  }

  @Test
  void deleteTagsFromTestCases_WhenAllTestCasesExist_ShouldDeleteTags() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributeIds = Arrays.asList(4L, 5L, 6L);
    var existingTestCaseIds = List.of(1L, 2L, 3L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
  }

  @Test
  void deleteTagsFromTestCases_WhenSomeTestCasesDoNotExist_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributeIds = Arrays.asList(4L, 5L, 6L);
    var existingTestCaseIds = List.of(1L, 2L); // Missing ID 3L

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
  }

  @Test
  void deleteTagsFromTestCases_WhenNoTestCasesExist_ShouldThrowNotFoundException() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributeIds = Arrays.asList(4L, 5L, 6L);
    var existingTestCaseIds = Collections.<Long>emptyList();

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds));

    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService, never()).deleteByTestCaseIdsAndAttributeIds(any(), any());
  }

  @Test
  void deleteTagsFromTestCases_WithSingleTestCaseAndAttribute_ShouldDeleteTags() {
    // Given
    var testCaseIds = List.of(1L);
    var attributeIds = List.of(4L);
    var existingTestCaseIds = List.of(1L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
  }

  @Test
  void deleteTagsFromTestCases_WithMultipleTestCasesAndSingleAttribute_ShouldDeleteTags() {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L, 4L);
    var attributeIds = List.of(5L);
    var existingTestCaseIds = List.of(1L, 2L, 3L, 4L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
  }

  @Test
  void deleteTagsFromTestCases_WithSingleTestCaseAndMultipleAttributes_ShouldDeleteTags() {
    // Given
    var testCaseIds = List.of(1L);
    var attributeIds = Arrays.asList(4L, 5L, 6L, 7L);
    var existingTestCaseIds = List.of(1L);

    when(tmsTestCaseRepository.findExistingIdsByProjectIdAndIds(projectId, testCaseIds))
        .thenReturn(existingTestCaseIds);

    // When
    sut.deleteTagsFromTestCases(projectId, testCaseIds, attributeIds);

    // Then
    verify(tmsTestCaseRepository).findExistingIdsByProjectIdAndIds(projectId, testCaseIds);
    verify(tmsTestCaseAttributeService).deleteByTestCaseIdsAndAttributeIds(testCaseIds, attributeIds);
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
}
