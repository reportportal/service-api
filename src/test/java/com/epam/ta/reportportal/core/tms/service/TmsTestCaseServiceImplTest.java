package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestCaseRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseDefaultVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
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
  private HttpServletResponse response;

  @InjectMocks
  private TmsTestCaseServiceImpl sut;

  private TmsTestCaseRQ testCaseRQ;
  private TmsTestCase testCase;
  private TmsTestCaseRS testCaseRS;
  private TmsTestCaseDefaultVersionRQ testCaseDefaultVersionRQ;
  private TmsTestCaseTestFolderRQ testFolderRQ;
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
    attribute.setAttributeId(3L);
    attributes.add(attribute);

    testCaseDefaultVersionRQ = new TmsTestCaseDefaultVersionRQ();
    testCaseDefaultVersionRQ.setName("Version name");

    testFolderRQ = new TmsTestCaseTestFolderRQ();
    testFolderRQ.setId(testFolderId);

    testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName("Test Case");
    testCaseRQ.setDescription("Description");
    testCaseRQ.setTestFolder(testFolderRQ);
    testCaseRQ.setTags(attributes);
    testCaseRQ.setDefaultVersion(testCaseDefaultVersionRQ);

    testCase = new TmsTestCase();
    testCase.setId(testCaseId);
    testCase.setName("Test Case");
    testCase.setDescription("Description");

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
    when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.getById(projectId, testCaseId);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findById(testCaseId);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void getById_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.getById(projectId, testCaseId));
    verify(tmsTestCaseRepository).findById(testCaseId);
  }

  @Test
  void create_ShouldCreateAndReturnTestCase() {
    // Given
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase,
        testCaseDefaultVersionRQ);
    verify(tmsTestCaseMapper).convert(testCase);
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
    testCaseRQWithNewFolder.setDefaultVersion(testCaseDefaultVersionRQ);

    var newFolderId = 10L;
    var newFolderRS = new TmsTestFolderRS();
    newFolderRS.setId(newFolderId);

    when(tmsTestFolderService.create(projectId, testFolderName)).thenReturn(newFolderRS);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQWithNewFolder, newFolderId)).thenReturn(testCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.create(projectId, testCaseRQWithNewFolder);

    // Then
    assertNotNull(result);
    verify(tmsTestFolderService).create(projectId, testFolderName);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQWithNewFolder, newFolderId);
  }

  @Test
  void update_WhenTestCaseExists_ShouldUpdateAndReturnTestCase() {
    // Given
    var convertedTestCase = new TmsTestCase();
    when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findById(testCaseId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).update(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).updateTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).updateDefaultTestCaseVersion(testCase,
        testCaseDefaultVersionRQ);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void update_WhenTestCaseDoesNotExist_ShouldCreateNewTestCase() {
    // Given
    when(tmsTestCaseRepository.findById(testCaseId)).thenReturn(Optional.empty());
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.update(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findById(testCaseId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseRepository).save(testCase);
    verify(tmsTestCaseAttributeService).createTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).createDefaultTestCaseVersion(testCase,
        testCaseDefaultVersionRQ);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void patch_WhenTestCaseExists_ShouldPatchAndReturnTestCase() {
    // Given
    var convertedTestCase = new TmsTestCase();
    when(tmsTestCaseRepository.findByIdAndProjectId(testCaseId, projectId)).thenReturn(
        Optional.of(testCase));
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(convertedTestCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.patch(projectId, testCaseId, testCaseRQ);

    // Then
    assertNotNull(result);
    assertEquals(testCaseRS, result);
    verify(tmsTestCaseRepository).findByIdAndProjectId(testCaseId, projectId);
    verify(tmsTestCaseMapper).convertFromRQ(projectId, testCaseRQ, testFolderId);
    verify(tmsTestCaseMapper).patch(testCase, convertedTestCase);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCase, attributes);
    verify(tmsTestCaseVersionService).patchDefaultTestCaseVersion(testCase,
        testCaseDefaultVersionRQ);
    verify(tmsTestCaseMapper).convert(testCase);
  }

  @Test
  void patch_WhenTestCaseDoesNotExist_ShouldThrowNotFoundException() {
    // Given
    when(tmsTestCaseRepository.findByIdAndProjectId(testCaseId, projectId)).thenReturn(
        Optional.empty());

    // When/Then
    assertThrows(ReportPortalException.class,
        () -> sut.patch(projectId, testCaseId, testCaseRQ));
    verify(tmsTestCaseRepository).findByIdAndProjectId(testCaseId, projectId);
  }

  @Test
  void delete_ShouldDeleteTestCase() {
    // When
    sut.delete(projectId, testCaseId);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestCaseId(testCaseId);
    verify(tmsTestCaseVersionService).deleteAllByTestCaseId(testCaseId);
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
    verify(tmsTestCaseRepository).deleteAllByTestCaseIds(testCaseIds);
  }

  @Test
  void delete_WithEmptyLocationIds_ShouldStillCallRepositoryMethods() {
    // Given
    var emptyTestCaseIds = Collections.<Long>emptyList();
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(emptyTestCaseIds)
        .build();

    // When
    sut.delete(projectId, deleteRequest);

    // Then
    verify(tmsTestCaseAttributeService).deleteAllByTestCaseIds(emptyTestCaseIds);
    verify(tmsTestCaseVersionService).deleteAllByTestCaseIds(emptyTestCaseIds);
    verify(tmsTestCaseRepository).deleteAllByTestCaseIds(emptyTestCaseIds);
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

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService).patchTestCaseAttributes(testCases, tags);
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

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
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

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(testCaseIds);
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
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

    // When
    sut.patch(projectId, patchRequest);

    // Then
    verify(tmsTestCaseRepository, never()).findAllById(any());
    verify(tmsTestCaseAttributeService, never()).patchTestCaseAttributes(anyList(), any());
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
    verify(tmsTestCaseRepository).deleteAllByTestCaseIds(singleTestCaseId);
  }

  @Test
  void importFromFile_ShouldImportAndCreateTestCases() {
    // Given
    var file = new MockMultipartFile("test.csv", "test content".getBytes());
    var importedTestCaseRQs = List.of(testCaseRQ);

    when(importerFactory.getImporter(file)).thenReturn(importer);
    when(importer.importFromFile(file)).thenReturn(importedTestCaseRQs);
    when(tmsTestCaseMapper.convertFromRQ(projectId, testCaseRQ, testFolderId)).thenReturn(testCase);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.importFromFile(projectId, file);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testCaseRS, result.get(0));
    verify(importerFactory).getImporter(file);
    verify(importer).importFromFile(file);
    verify(tmsTestCaseRepository).save(testCase);
  }

  @Test
  void exportToFile_WithSpecificIds_ShouldExportSpecificTestCases() {
    // Given
    var testCaseIds = List.of(1L, 2L);
    var format = "JSON";
    var includeAttachments = true;

    when(tmsTestCaseRepository.findById(1L)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseRepository.findById(2L)).thenReturn(Optional.of(testCase));
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);
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
  void getTestCasesByCriteria_ShouldReturnPagedResults() {
    // Given
    var search = "test search";
    var testFolderId = 5L;
    var pageable = PageRequest.of(0, 10);
    var testCases = List.of(testCase);
    var springPage = new PageImpl<>(testCases, pageable, 1);

    when(tmsTestCaseRepository.findByCriteria(projectId, search, testFolderId, pageable))
        .thenReturn(springPage);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.getTestCasesByCriteria(projectId, search, testFolderId, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseRepository).findByCriteria(projectId, search, testFolderId, pageable);
  }

  @Test
  void getTestCasesByCriteria_WithNullParameters_ShouldReturnPagedResults() {
    // Given
    var pageable = PageRequest.of(0, 20);
    var testCases = List.of(testCase);
    var springPage = new PageImpl<>(testCases, pageable, 1);

    when(tmsTestCaseRepository.findByCriteria(projectId, null, null, pageable))
        .thenReturn(springPage);
    when(tmsTestCaseMapper.convert(testCase)).thenReturn(testCaseRS);

    // When
    var result = sut.getTestCasesByCriteria(projectId, null, null, pageable);

    // Then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertEquals(1, result.getContent().size());
    verify(tmsTestCaseRepository).findByCriteria(projectId, null, null, pageable);
  }
}
