package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfSubfolders;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestFolderRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ.ParentTmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.exception.NotFoundException;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestFolderMapper;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestFolderExporter;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestFolderExporterFactory;
import com.epam.ta.reportportal.entity.project.Project;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TmsTestFolderServiceImplTest {

  private final long projectId = 1L;
  private final long testFolderId = 2L;
  private final Pageable pageable = PageRequest.of(0, 10);
  // Additional fields for hierarchy tests
  private final Long rootFolderId = 10L;
  private final Long firstSubFolderId = 11L;
  private final Long secondSubFolderId = 12L;
  private final Long subSubFolderId = 13L;
  @Mock
  private TmsTestFolderMapper tmsTestFolderMapper;
  @Mock
  private TmsTestFolderRepository tmsTestFolderRepository;
  @Mock
  private TmsTestCaseService tmsTestCaseService;
  @Mock
  private TmsTestFolderExporterFactory tmsTestFolderExporterFactory;
  @Mock
  private TmsTestFolderExporter tmsTestFolderExporter;
  @Mock
  private HttpServletResponse mockResponse;
  @InjectMocks
  private TmsTestFolderServiceImpl sut;
  private TmsTestFolder testFolder;
  private TmsTestFolder parentTestFolder;
  private TmsTestFolderRQ testFolderRQ;
  private TmsTestFolderRS testFolderRS;
  private TmsTestFolder rootFolder;
  private TmsTestFolder firstSubfolder;
  private TmsTestFolder secondSubfolder;
  private TmsTestFolder subSubFolder;
  private List<TmsTestFolder> allFolders;

  @BeforeEach
  void setUp() {
    Project project = new Project();
    project.setId(projectId);

    parentTestFolder = new TmsTestFolder();
    parentTestFolder.setId(3L);
    parentTestFolder.setName("Parent Folder");
    parentTestFolder.setDescription("Parent Description");
    parentTestFolder.setProject(project);

    testFolder = new TmsTestFolder();
    testFolder.setId(testFolderId);
    testFolder.setName("Test Folder");
    testFolder.setDescription("Test Description");
    testFolder.setProject(project);
    testFolder.setParentTestFolder(parentTestFolder);

    testFolderRQ = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(ParentTmsTestFolderRQ.builder().id(3L).build())
        .build();

    testFolderRS = TmsTestFolderRS.builder()
        .id(testFolderId)
        .name("Test Folder")
        .description("Test Description")
        .countOfSubfolders(0L)
        .build();

    // Setup for hierarchy tests
    rootFolder = new TmsTestFolder();
    rootFolder.setId(rootFolderId);
    rootFolder.setName("Root Folder");
    rootFolder.setDescription("Root folder description");
    rootFolder.setProject(project);

    firstSubfolder = new TmsTestFolder();
    firstSubfolder.setId(firstSubFolderId);
    firstSubfolder.setName("Sub Folder 1");
    firstSubfolder.setDescription("Sub folder 1 description");
    firstSubfolder.setProject(project);
    firstSubfolder.setParentTestFolder(rootFolder);

    secondSubfolder = new TmsTestFolder();
    secondSubfolder.setId(secondSubFolderId);
    secondSubfolder.setName("Sub Folder 2");
    secondSubfolder.setDescription("Sub folder 2 description");
    secondSubfolder.setProject(project);
    secondSubfolder.setParentTestFolder(rootFolder);

    subSubFolder = new TmsTestFolder();
    subSubFolder.setId(subSubFolderId);
    subSubFolder.setName("Sub Sub Folder");
    subSubFolder.setDescription("Sub sub folder description");
    subSubFolder.setProject(project);
    subSubFolder.setParentTestFolder(firstSubfolder);

    allFolders = Arrays.asList(rootFolder, firstSubfolder, secondSubfolder, subSubFolder);
  }

  @Test
  void testCreate() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, testFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());
    assertEquals(testFolderRS.getName(), result.getName());
    assertEquals(testFolderRS.getDescription(), result.getDescription());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQ);
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertToRS(testFolder);
  }

  @Test
  void testCreateWithParentName() {
    // Arrange
    TmsTestFolderRQ rqWithParentName = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(ParentTmsTestFolderRQ.builder().name("Parent Folder").build())
        .build();

    when(tmsTestFolderMapper.convertFromRQ(projectId, rqWithParentName)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromName(projectId, "Parent Folder")).thenReturn(
        parentTestFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class))).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, rqWithParentName);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, rqWithParentName);
    verify(tmsTestFolderMapper).convertFromName(projectId, "Parent Folder");
    verify(tmsTestFolderRepository, times(2)).save(
        any(TmsTestFolder.class)); // Сохраняем родительскую папку и текущую
    verify(tmsTestFolderMapper).convertToRS(testFolder);
  }

  @Test
  void testCreateWithInvalidParent() {
    // Arrange
    TmsTestFolderRQ rqWithInvalidParent = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(ParentTmsTestFolderRQ.builder().id(3L).name("Parent Folder").build())
        .build();

    when(tmsTestFolderMapper.convertFromRQ(projectId, rqWithInvalidParent)).thenReturn(testFolder);

    // Act & Assert
    assertThrows(ValidationException.class, () ->
        sut.create(projectId, rqWithInvalidParent));

    verify(tmsTestFolderMapper).convertFromRQ(projectId, rqWithInvalidParent);
    verify(tmsTestFolderRepository, never()).save(any(TmsTestFolder.class));
  }

  @Test
  void testUpdate() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(
        new TmsTestFolder());
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convert(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convert(testFolder);
  }

  @Test
  void testUpdateWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.empty());
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper, never()).update(any(), any());
    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQ);
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertToRS(testFolder);
  }

  @Test
  void testUpdateWithNullParent() {
    // Arrange
    TmsTestFolderRQ rqWithNullParent = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(null)
        .build();

    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderMapper.convertFromRQ(projectId, rqWithNullParent)).thenReturn(
        new TmsTestFolder());
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convert(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, rqWithNullParent);

    // Assert
    assertNotNull(result);
    assertNull(testFolder.getParentTestFolder());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convert(testFolder);
  }

  @Test
  void testPatch() {
    // Arrange
    TmsTestFolderRQ patchRQ = TmsTestFolderRQ.builder()
        .name("Updated Name")
        .build();

    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderMapper.convertFromRQ(projectId, patchRQ)).thenReturn(new TmsTestFolder());
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convert(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.patch(projectId, testFolderId, patchRQ);

    // Assert
    assertNotNull(result);

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).patch(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convert(testFolder);
  }

  @Test
  void testPatchWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () ->
        sut.patch(projectId, testFolderId, testFolderRQ));

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper, never()).patch(any(), any());
    verify(tmsTestFolderRepository, never()).save(any());
  }

  @Test
  void testGetById() {
    // Arrange
    TmsTestFolderWithCountOfSubfolders folderWithCount = new TmsTestFolderWithCountOfSubfolders(
        testFolder, 3L);

    when(tmsTestFolderRepository.findByIdWithCountOfSubfolders(projectId, testFolderId))
        .thenReturn(Optional.of(folderWithCount));
    when(tmsTestFolderMapper.convertToRS(folderWithCount)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.getById(projectId, testFolderId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdWithCountOfSubfolders(projectId, testFolderId);
    verify(tmsTestFolderMapper).convertToRS(folderWithCount);
  }

  @Test
  void testGetByIdWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdWithCountOfSubfolders(projectId, testFolderId))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NotFoundException.class, () ->
        sut.getById(projectId, testFolderId));

    verify(tmsTestFolderRepository).findByIdWithCountOfSubfolders(projectId, testFolderId);
    verify(tmsTestFolderMapper, never()).convertToRS(any(TmsTestFolderWithCountOfSubfolders.class));
  }

  @Test
  void testGetFoldersByProjectID() {
    // Arrange
    TmsTestFolderWithCountOfSubfolders folder1 = new TmsTestFolderWithCountOfSubfolders(testFolder,
        2L);
    TmsTestFolderWithCountOfSubfolders folder2 = new TmsTestFolderWithCountOfSubfolders(
        parentTestFolder, 1L);

    Page<TmsTestFolderWithCountOfSubfolders> folderPage = new PageImpl<>(
        Arrays.asList(folder1, folder2),
        pageable,
        2
    );

    Page<TmsTestFolderRS> folderRSPage = new PageImpl<>(
        Arrays.asList(testFolderRS, testFolderRS),
        pageable,
        2
    );

    when(tmsTestFolderRepository.findAllByProjectIdWithCountOfSubfolders(projectId, pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderMapper.convertToRS(folderPage)).thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByProjectID(projectId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(2, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByProjectIdWithCountOfSubfolders(projectId, pageable);
    verify(tmsTestFolderMapper).convertToRS(folderPage);
  }

  @Test
  void testGetSubFolders() {
    // Arrange
    TmsTestFolderWithCountOfSubfolders folder = new TmsTestFolderWithCountOfSubfolders(testFolder,
        0L);

    Page<TmsTestFolderWithCountOfSubfolders> folderPage = new PageImpl<>(
        Collections.singletonList(folder),
        pageable,
        1
    );

    Page<TmsTestFolderRS> folderRSPage = new PageImpl<>(
        Collections.singletonList(testFolderRS),
        pageable,
        1
    );

    when(tmsTestFolderRepository.findAllByParentTestFolderIdWithCountOfSubfolders(projectId,
        parentTestFolder.getId(), pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderMapper.convertToRS(folderPage)).thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getSubFolders(projectId, parentTestFolder.getId(), pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByParentTestFolderIdWithCountOfSubfolders(projectId,
        parentTestFolder.getId(), pageable);
    verify(tmsTestFolderMapper).convertToRS(folderPage);
  }

  @Test
  void testDelete() {
    // Arrange
    doNothing().when(tmsTestCaseService).deleteByTestFolderId(projectId, testFolderId);
    doNothing().when(tmsTestFolderRepository)
        .deleteTestFolderWithSubfoldersById(projectId, testFolderId);

    // Act
    sut.delete(projectId, testFolderId);

    // Assert
    verify(tmsTestCaseService).deleteByTestFolderId(projectId, testFolderId);
    verify(tmsTestFolderRepository).deleteTestFolderWithSubfoldersById(projectId, testFolderId);
  }

  @Test
  void testFindFolderWithFullHierarchy_Success() {
    // Arrange
    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId,
        subSubFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(allFolders);

    // Act
    TmsTestFolder result = sut.findFolderWithFullHierarchy(projectId, rootFolderId);

    // Assert
    assertNotNull(result);
    assertEquals(rootFolderId, result.getId());
    assertEquals("Root Folder", result.getName());

    // Verify that the hierarchy is correctly built
    assertNotNull(result.getSubTestFolders());
    assertEquals(2, result.getSubTestFolders().size());

    // Find subFolder1 in the result's subfolders
    TmsTestFolder resultSubFolder1 = result.getSubTestFolders().stream()
        .filter(f -> f.getId().equals(firstSubFolderId))
        .findFirst()
        .orElse(null);

    assertNotNull(resultSubFolder1);
    assertNotNull(resultSubFolder1.getSubTestFolders());
    assertEquals(1, resultSubFolder1.getSubTestFolders().size());
    assertEquals(subSubFolderId, resultSubFolder1.getSubTestFolders().get(0).getId());

    // Verify repository calls
    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderRepository).findAllById(folderIds);
  }

  @Test
  void testFindFolderWithFullHierarchy_EmptyIdsList() {
    // Arrange
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(NotFoundException.class, () ->
        sut.findFolderWithFullHierarchy(projectId, rootFolderId));

    // Verify repository calls
    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderRepository, never()).findAllById(anyList());
  }

  @Test
  void testFindFolderWithFullHierarchy_FolderNotFound() {
    // Arrange
    Long nonExistentFolderId = 999L;
    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, nonExistentFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(
            Arrays.asList(rootFolder, firstSubfolder, secondSubfolder)); // No folder with ID 999

    // Act & Assert
    assertThrows(NotFoundException.class, () ->
        sut.findFolderWithFullHierarchy(projectId, nonExistentFolderId));

    // Verify repository calls
    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, nonExistentFolderId);
    verify(tmsTestFolderRepository).findAllById(folderIds);
  }

  @Test
  void testExportFolderById_Success() {
    // Arrange
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    // Mock the folder hierarchy retrieval
    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId,
        subSubFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(allFolders);

    // Mock the exporter factory and exporter
    when(tmsTestFolderExporterFactory.getExporter(fileType)).thenReturn(tmsTestFolderExporter);
    doNothing().when(tmsTestFolderExporter).export(any(TmsTestFolder.class), eq(mockResponse));

    // Act
    sut.exportFolderById(projectId, rootFolderId, fileType, mockResponse);

    // Assert
    // Capture the folder passed to the exporter
    ArgumentCaptor<TmsTestFolder> folderCaptor = ArgumentCaptor.forClass(TmsTestFolder.class);
    verify(tmsTestFolderExporter).export(folderCaptor.capture(), eq(mockResponse));

    TmsTestFolder exportedFolder = folderCaptor.getValue();
    assertNotNull(exportedFolder);
    assertEquals(rootFolderId, exportedFolder.getId());

    // Verify that the hierarchy is correctly built for export
    assertNotNull(exportedFolder.getSubTestFolders());
    assertEquals(2, exportedFolder.getSubTestFolders().size());

    // Verify repository and factory calls
    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderRepository).findAllById(folderIds);
    verify(tmsTestFolderExporterFactory).getExporter(fileType);
  }

  @Test
  void testExportFolderById_FolderNotFound() {
    // Arrange
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(NotFoundException.class, () ->
        sut.exportFolderById(projectId, rootFolderId, fileType, mockResponse));

    // Verify repository calls
    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderExporter, never()).export(any(), any());
  }

  @Test
  void testExportFolderById_ComplexHierarchy() {
    // Arrange
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    // Create a more complex hierarchy for testing
    TmsTestFolder subSubFolder2 = new TmsTestFolder();
    subSubFolder2.setId(14L);
    subSubFolder2.setName("Sub Sub Folder 2");
    subSubFolder2.setParentTestFolder(secondSubfolder);

    List<TmsTestFolder> complexHierarchy = new ArrayList<>(allFolders);
    complexHierarchy.add(subSubFolder2);

    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId,
        subSubFolderId, 14L);

    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(complexHierarchy);

    when(tmsTestFolderExporterFactory.getExporter(fileType)).thenReturn(tmsTestFolderExporter);
    doNothing().when(tmsTestFolderExporter).export(any(TmsTestFolder.class), eq(mockResponse));

    // Act
    sut.exportFolderById(projectId, rootFolderId, fileType, mockResponse);

    // Assert
    ArgumentCaptor<TmsTestFolder> folderCaptor = ArgumentCaptor.forClass(TmsTestFolder.class);
    verify(tmsTestFolderExporter).export(folderCaptor.capture(), eq(mockResponse));

    TmsTestFolder exportedFolder = folderCaptor.getValue();
    assertNotNull(exportedFolder);

    // Verify the complex hierarchy is built correctly
    assertEquals(2, exportedFolder.getSubTestFolders().size());

    // Find subFolder2 in the result's subfolders
    TmsTestFolder resultSubFolder2 = exportedFolder.getSubTestFolders().stream()
        .filter(f -> f.getId().equals(secondSubFolderId))
        .findFirst()
        .orElse(null);

    assertNotNull(resultSubFolder2);
    assertNotNull(resultSubFolder2.getSubTestFolders());
    assertEquals(1, resultSubFolder2.getSubTestFolders().size());
    assertEquals(14L, resultSubFolder2.getSubTestFolders().get(0).getId());
  }
}
