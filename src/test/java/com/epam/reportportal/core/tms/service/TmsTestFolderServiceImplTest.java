package com.epam.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.statistics.FolderDuplicationStatistics;
import com.epam.reportportal.core.tms.statistics.TestCaseDuplicationStatistics;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.dao.tms.enhanced.TmsTestFolderWithTestCaseCountRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderWithCountOfTestCases;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestFolderRepository;
import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.mapper.TmsTestFolderMapper;
import com.epam.reportportal.core.tms.mapper.exporter.TmsTestFolderExporter;
import com.epam.reportportal.core.tms.mapper.factory.TmsTestFolderExporterFactory;
import com.epam.reportportal.core.tms.validation.TestFolderIdValidator;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for TmsTestFolderServiceImpl.
 * Tests CRUD operations, hierarchy management, duplication, and export functionality.
 */
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
  private final String testFolderName = "Test Folder Name";

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
  private TestFolderIdValidator testFolderIdValidator;
  @Mock
  private TmsTestFolderWithTestCaseCountRepository tmsTestFolderWithTestCaseCountRepository;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private Filter filter;
  @InjectMocks
  private TmsTestFolderServiceImpl sut;

  private TmsTestFolder testFolder;
  private TmsTestFolder parentTestFolder;
  private TmsTestFolder subFolder1;
  private TmsTestFolder subFolder2;
  private TmsTestFolderRQ testFolderRQ;
  private TmsTestFolderRQ testFolderRQWithParentId;
  private TmsTestFolderRQ testFolderRQWithNestedParentFolder;
  private TmsTestFolderRQ testFolderRQWithRootParentFolder;
  private TmsTestFolderRS testFolderRS;
  private TmsTestFolder rootFolder;
  private TmsTestFolder firstSubfolder;
  private TmsTestFolder secondSubfolder;
  private TmsTestFolder subSubFolder;
  private List<TmsTestFolder> allFolders;
  private NewTestFolderRQ newTestFolderRQ;
  private TmsTestCaseRQ testCaseRQ;
  private Project project;

  @BeforeEach
  void setUp() {
    project = new Project();
    project.setId(projectId);

    parentTestFolder = new TmsTestFolder();
    parentTestFolder.setId(3L);
    parentTestFolder.setName("Parent Folder");
    parentTestFolder.setDescription("Parent Description");
    parentTestFolder.setProject(project);
    parentTestFolder.setSubFolders(new ArrayList<>());

    // Create subfolders for testing
    subFolder1 = new TmsTestFolder();
    subFolder1.setId(4L);
    subFolder1.setName("Sub Folder 1");
    subFolder1.setParentTestFolder(parentTestFolder);
    subFolder1.setSubFolders(new ArrayList<>());

    subFolder2 = new TmsTestFolder();
    subFolder2.setId(5L);
    subFolder2.setName("Sub Folder 2");
    subFolder2.setParentTestFolder(parentTestFolder);
    subFolder2.setSubFolders(new ArrayList<>());

    parentTestFolder.getSubFolders().addAll(Arrays.asList(subFolder1, subFolder2));

    testFolder = new TmsTestFolder();
    testFolder.setId(testFolderId);
    testFolder.setName("Test Folder");
    testFolder.setDescription("Test Description");
    testFolder.setProject(project);
    testFolder.setParentTestFolder(parentTestFolder);
    testFolder.setSubFolders(new ArrayList<>());

    // Test folder RQ with existing parent ID
    testFolderRQWithParentId = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolderId(3L)
        .build();

    // Test folder RQ with new nested parent folder to create
    testFolderRQWithNestedParentFolder = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent Folder")
            .parentTestFolderId(5L) // This parent will have parent with ID 5
            .build())
        .build();

    // Test folder RQ with new root parent folder to create
    testFolderRQWithRootParentFolder = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent Folder")
            .build())
        .build();

    // Test folder RQ without parent
    testFolderRQ = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .build();

    testFolderRS = TmsTestFolderRS.builder()
        .id(testFolderId)
        .name("Test Folder")
        .description("Test Description")
        .countOfTestCases(0L)
        .build();

    newTestFolderRQ = NewTestFolderRQ.builder()
        .name(testFolderName)
        .build();

    testCaseRQ = TmsTestCaseRQ.builder()
        .name("Test Case")
        .description("Test case description")
        .build();

    // Setup for hierarchy tests
    rootFolder = new TmsTestFolder();
    rootFolder.setId(rootFolderId);
    rootFolder.setName("Root Folder");
    rootFolder.setDescription("Root folder description");
    rootFolder.setProject(project);
    rootFolder.setSubFolders(new ArrayList<>());

    firstSubfolder = new TmsTestFolder();
    firstSubfolder.setId(firstSubFolderId);
    firstSubfolder.setName("Sub Folder 1");
    firstSubfolder.setDescription("Sub folder 1 description");
    firstSubfolder.setProject(project);
    firstSubfolder.setParentTestFolder(rootFolder);
    firstSubfolder.setSubFolders(new ArrayList<>());

    secondSubfolder = new TmsTestFolder();
    secondSubfolder.setId(secondSubFolderId);
    secondSubfolder.setName("Sub Folder 2");
    secondSubfolder.setDescription("Sub folder 2 description");
    secondSubfolder.setProject(project);
    secondSubfolder.setParentTestFolder(rootFolder);
    secondSubfolder.setSubFolders(new ArrayList<>());

    subSubFolder = new TmsTestFolder();
    subSubFolder.setId(subSubFolderId);
    subSubFolder.setName("Sub Sub Folder");
    subSubFolder.setDescription("Sub sub folder description");
    subSubFolder.setProject(project);
    subSubFolder.setParentTestFolder(firstSubfolder);
    subSubFolder.setSubFolders(new ArrayList<>());

    allFolders = Arrays.asList(rootFolder, firstSubfolder, secondSubfolder, subSubFolder);

    sut.setTmsTestCaseService(tmsTestCaseService);
  }

  @Test
  void testCreateWithoutParent() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, testFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());
    assertEquals(testFolderRS.getName(), result.getName());
    assertEquals(testFolderRS.getDescription(), result.getDescription());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQ);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testCreateWithExistingParentId() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithParentId)).thenReturn(testFolder);
    when(tmsTestFolderRepository.existsByIdAndProjectId(3L, projectId)).thenReturn(true);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, testFolderRQWithParentId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());
    assertEquals(testFolderRS.getName(), result.getName());
    assertEquals(testFolderRS.getDescription(), result.getDescription());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQWithParentId);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(3L, projectId);
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testCreateWithNewNestedParentFolder() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithNestedParentFolder)).thenReturn(testFolder);
    when(tmsTestFolderRepository.existsByIdAndProjectId(5L, projectId)).thenReturn(true);
    when(tmsTestFolderMapper.convertToTestFolder(projectId, testFolderRQWithNestedParentFolder.getParentTestFolder()))
        .thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class))).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, testFolderRQWithNestedParentFolder);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQWithNestedParentFolder);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(5L, projectId);
    verify(tmsTestFolderMapper).convertToTestFolder(projectId, testFolderRQWithNestedParentFolder.getParentTestFolder());
    verify(tmsTestFolderRepository, times(2)).save(any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testCreateWithNewRootParentFolder() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithRootParentFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertToTestFolder(projectId, testFolderRQWithRootParentFolder.getParentTestFolder()))
        .thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class))).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, testFolderRQWithRootParentFolder);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQWithRootParentFolder);
    verify(tmsTestFolderMapper).convertToTestFolder(projectId, testFolderRQWithRootParentFolder.getParentTestFolder());
    verify(tmsTestFolderRepository, times(2)).save(any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testCreateWithBothParentIdAndParentFolder() {
    // Arrange
    TmsTestFolderRQ rqWithBothParents = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolderId(3L)
        .parentTestFolder(NewTestFolderRQ.builder().name("Parent Folder").build())
        .build();

    when(tmsTestFolderMapper.convertFromRQ(projectId, rqWithBothParents)).thenReturn(testFolder);

    // Act & Assert
    assertThrows(ReportPortalException.class, () ->
        sut.create(projectId, rqWithBothParents));

    verify(tmsTestFolderMapper).convertFromRQ(projectId, rqWithBothParents);
    verify(tmsTestFolderRepository, never()).save(any(TmsTestFolder.class));
  }

  @Test
  void testCreateWithParentFolderButNoName() {
    // Arrange
    TmsTestFolderRQ rqWithEmptyParentName = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(NewTestFolderRQ.builder().build()) // no name set
        .build();

    when(tmsTestFolderMapper.convertFromRQ(projectId, rqWithEmptyParentName)).thenReturn(testFolder);

    // Act & Assert
    assertThrows(ReportPortalException.class, () ->
        sut.create(projectId, rqWithEmptyParentName));

    verify(tmsTestFolderMapper).convertFromRQ(projectId, rqWithEmptyParentName);
    verify(tmsTestFolderRepository, never()).save(any(TmsTestFolder.class));
  }

  @Test
  void testUpdate() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithParentId)).thenReturn(
        new TmsTestFolder());
    when(tmsTestFolderRepository.existsByIdAndProjectId(3L, projectId)).thenReturn(true);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQWithParentId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).existsByIdAndProjectId(3L, projectId);
    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testUpdateWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.empty());
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithParentId)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.existsByIdAndProjectId(3L, projectId)).thenReturn(true);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQWithParentId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper, never()).update(any(), any());
    verify(tmsTestFolderRepository).existsByIdAndProjectId(3L, projectId);
    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQWithParentId);
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
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
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, rqWithNullParent);

    // Assert
    assertNotNull(result);
    assertNull(testFolder.getParentTestFolder());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testUpdateWithNewNestedParentFolder() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderRepository.existsByIdAndProjectId(5L, projectId))
        .thenReturn(true);
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithNestedParentFolder)).thenReturn(
        new TmsTestFolder());
    when(tmsTestFolderMapper.convertToTestFolder(projectId, testFolderRQWithNestedParentFolder.getParentTestFolder()))
        .thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class))).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQWithNestedParentFolder);

    // Assert
    assertNotNull(result);

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(5L, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertToTestFolder(projectId, testFolderRQWithNestedParentFolder.getParentTestFolder());
    verify(tmsTestFolderRepository, times(2)).save(any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testUpdateWithNewRootParentFolder() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQWithRootParentFolder)).thenReturn(
        new TmsTestFolder());
    when(tmsTestFolderMapper.convertToTestFolder(projectId, testFolderRQWithRootParentFolder.getParentTestFolder()))
        .thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class))).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQWithRootParentFolder);

    // Assert
    assertNotNull(result);

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).update(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertToTestFolder(projectId, testFolderRQWithRootParentFolder.getParentTestFolder());
    verify(tmsTestFolderRepository, times(2)).save(any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
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
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.patch(projectId, testFolderId, patchRQ);

    // Assert
    assertNotNull(result);

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper).patch(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testPatchWithParentId() {
    // Arrange
    TmsTestFolderRQ patchRQWithParentId = TmsTestFolderRQ.builder()
        .parentTestFolderId(3L)
        .build();

    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));
    when(tmsTestFolderRepository.existsByIdAndProjectId(3L, projectId)).thenReturn(true);
    when(tmsTestFolderMapper.convertFromRQ(projectId, patchRQWithParentId)).thenReturn(new TmsTestFolder());
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.patch(projectId, testFolderId, patchRQWithParentId);

    // Assert
    assertNotNull(result);

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(3L, projectId);
    verify(tmsTestFolderMapper).patch(eq(testFolder), any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testPatchWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.patch(projectId, testFolderId, testFolderRQ));

    assertTrue(exception.getMessage().contains(
        String.format("Test Folder with id: %d for project: %d", testFolderId, projectId)));

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
    verify(tmsTestFolderMapper, never()).patch(any(), any());
    verify(tmsTestFolderRepository, never()).save(any());
  }

  @Test
  void testGetById() {
    // Arrange
    TmsTestFolderWithCountOfTestCases folderWithCount = new TmsTestFolderWithCountOfTestCases(
        parentTestFolder, 3L);

    when(tmsTestFolderRepository.findByIdWithCountOfTestCases(projectId, 3L))
        .thenReturn(Optional.of(folderWithCount));
    when(tmsTestFolderMapper.convertFromTmsTestFolderWithCountOfTestCasesToRS(folderWithCount))
        .thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.getById(projectId, 3L);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdWithCountOfTestCases(projectId, 3L);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderWithCountOfTestCasesToRS(folderWithCount);
  }

  @Test
  void testGetByIdWhenFolderNotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdWithCountOfTestCases(projectId, testFolderId))
        .thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.getById(projectId, testFolderId));

    assertTrue(exception.getMessage().contains(
        String.format("'Test Folder with id: %d for project: %d'", testFolderId, projectId)));

    verify(tmsTestFolderRepository).findByIdWithCountOfTestCases(projectId, testFolderId);
    verify(tmsTestFolderMapper, never()).convertFromTmsTestFolderWithCountOfTestCasesToRS(any());
  }

  @Test
  void testGetFoldersByCriteria() {
    // Arrange
    TmsTestFolderWithCountOfTestCases folder1 = new TmsTestFolderWithCountOfTestCases(testFolder,
        2L);
    TmsTestFolderWithCountOfTestCases folder2 = new TmsTestFolderWithCountOfTestCases(
        parentTestFolder, 1L);

    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> folderPage =
        new PageImpl<>(Arrays.asList(folder1, folder2), pageable, 2);

    Page<TmsTestFolderRS> folderRSPage = new Page<>(
        Arrays.asList(testFolderRS, testFolderRS),
        10L, 0L, 2L
    );

    when(tmsTestFolderWithTestCaseCountRepository.findAllByProjectIdAndFilterWithCountOfTestCases(
        projectId, filter, pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderMapper.convert(folderPage))
        .thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByCriteria(projectId, filter, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    verify(tmsTestFolderWithTestCaseCountRepository).findAllByProjectIdAndFilterWithCountOfTestCases(
        projectId, filter, pageable);
    verify(tmsTestFolderMapper).convert(folderPage);
  }

  @Test
  void testGetFoldersByCriteriaEmptyPage() {
    // Arrange
    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> emptyPage =
        new PageImpl<>(Collections.emptyList(), pageable, 0);

    Page<TmsTestFolderRS> emptyRSPage = new Page<>(
        Collections.emptyList(), 10L, 0L, 0L
    );

    when(tmsTestFolderWithTestCaseCountRepository.findAllByProjectIdAndFilterWithCountOfTestCases(
        projectId, filter, pageable))
        .thenReturn(emptyPage);
    when(tmsTestFolderMapper.convert(emptyPage))
        .thenReturn(emptyRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByCriteria(projectId, filter, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());

    verify(tmsTestFolderWithTestCaseCountRepository).findAllByProjectIdAndFilterWithCountOfTestCases(
        projectId, filter, pageable);
    verify(tmsTestFolderMapper).convert(emptyPage);
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
  void testExistsById_WhenFolderExists() {
    // Arrange
    when(tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId))
        .thenReturn(true);

    // Act
    var result = sut.existsById(projectId, testFolderId);

    // Assert
    assertTrue(result);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(testFolderId, projectId);
  }

  @Test
  void testExistsById_WhenFolderDoesNotExist() {
    // Arrange
    when(tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId))
        .thenReturn(false);

    // Act
    var result = sut.existsById(projectId, testFolderId);

    // Assert
    assertFalse(result);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(testFolderId, projectId);
  }

  @Test
  void testCreateWithNewTestFolderRQ() {
    // Arrange
    when(tmsTestFolderMapper.convertToRQ(newTestFolderRQ)).thenReturn(testFolderRQ);
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, newTestFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderMapper).convertToRQ(newTestFolderRQ);
    verify(tmsTestFolderMapper).convertFromRQ(projectId, testFolderRQ);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
  }

  @Test
  void testResolveTestFolderRQ_WithTestFolderId() {
    // Arrange
    doNothing().when(testFolderIdValidator).validate(testFolderId, null);

    // Act
    sut.resolveTestFolderRQ(testCaseRQ, testFolderId, null);

    // Assert
    assertEquals(testFolderId, testCaseRQ.getTestFolderId());
    assertNull(testCaseRQ.getTestFolder());

    verify(testFolderIdValidator).validate(testFolderId, null);
  }

  @Test
  void testResolveTestFolderRQ_WithTestFolderName() {
    // Arrange
    doNothing().when(testFolderIdValidator).validate(null, testFolderName);
    when(tmsTestFolderMapper.convertToTmsTestCaseTestFolderRQ(testFolderName))
        .thenReturn(newTestFolderRQ);

    // Act
    sut.resolveTestFolderRQ(testCaseRQ, null, testFolderName);

    // Assert
    assertNull(testCaseRQ.getTestFolderId());
    assertEquals(newTestFolderRQ, testCaseRQ.getTestFolder());

    verify(testFolderIdValidator).validate(null, testFolderName);
    verify(tmsTestFolderMapper).convertToTmsTestCaseTestFolderRQ(testFolderName);
  }

  @Test
  void testResolveTestFolderRQ_WithNullRequest() {
    // Arrange
    doNothing().when(testFolderIdValidator).validate(testFolderId, testFolderName);

    // Act
    sut.resolveTestFolderRQ(null, testFolderId, testFolderName);

    // Assert
    verify(testFolderIdValidator).validate(testFolderId, testFolderName);
    verify(tmsTestFolderMapper, never()).convertToTmsTestCaseTestFolderRQ(any());
  }

  @Test
  void testResolveTestFolderRQ_ValidationFailure() {
    // Arrange
    doThrow(ReportPortalException.class).when(testFolderIdValidator)
        .validate(testFolderId, testFolderName);

    // Act & Assert
    assertThrows(ReportPortalException.class, () ->
        sut.resolveTestFolderRQ(testCaseRQ, testFolderId, testFolderName));

    verify(testFolderIdValidator).validate(testFolderId, testFolderName);
    verify(tmsTestFolderMapper, never()).convertToTmsTestCaseTestFolderRQ(any());
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

    assertNotNull(result.getSubFolders());
    assertEquals(2, result.getSubFolders().size());

    TmsTestFolder resultSubFolder1 = result.getSubFolders().stream()
        .filter(f -> f.getId().equals(firstSubFolderId))
        .findFirst()
        .orElse(null);

    assertNotNull(resultSubFolder1);
    assertNotNull(resultSubFolder1.getSubFolders());
    assertEquals(1, resultSubFolder1.getSubFolders().size());
    assertEquals(subSubFolderId, resultSubFolder1.getSubFolders().getFirst().getId());

    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderRepository).findAllById(folderIds);
  }

  @Test
  void testFindFolderWithFullHierarchy_EmptyIdsList() {
    // Arrange
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.findFolderWithFullHierarchy(projectId, rootFolderId));

    assertTrue(exception.getMessage().contains(
        String.format("'Test Folder with id: %d for project: %d'", rootFolderId, projectId)));

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
        .thenReturn(Arrays.asList(rootFolder, firstSubfolder, secondSubfolder));

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.findFolderWithFullHierarchy(projectId, nonExistentFolderId));

    assertTrue(exception.getMessage().contains(
        String.format("'Test Folder with id: %d for project: %d'", nonExistentFolderId,
            projectId)));

    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, nonExistentFolderId);
    verify(tmsTestFolderRepository).findAllById(folderIds);
  }

  @Test
  void testExportFolderById_Success() {
    // Arrange
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId,
        subSubFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(allFolders);
    when(tmsTestFolderExporterFactory.getExporter(fileType)).thenReturn(tmsTestFolderExporter);
    doNothing().when(tmsTestFolderExporter).export(any(TmsTestFolder.class), eq(mockResponse));

    // Act
    sut.exportFolderById(projectId, rootFolderId, fileType, mockResponse);

    // Assert
    ArgumentCaptor<TmsTestFolder> folderCaptor = ArgumentCaptor.forClass(TmsTestFolder.class);
    verify(tmsTestFolderExporter).export(folderCaptor.capture(), eq(mockResponse));

    TmsTestFolder exportedFolder = folderCaptor.getValue();
    assertNotNull(exportedFolder);
    assertEquals(rootFolderId, exportedFolder.getId());

    assertNotNull(exportedFolder.getSubFolders());
    assertEquals(2, exportedFolder.getSubFolders().size());

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
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.exportFolderById(projectId, rootFolderId, fileType, mockResponse));

    assertTrue(exception.getMessage().contains(
        String.format("'Test Folder with id: %d for project: %d'", rootFolderId, projectId)));

    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderExporter, never()).export(any(), any());
  }

  @Test
  void testExportFolderById_ComplexHierarchy() {
    // Arrange
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    TmsTestFolder subSubFolder2 = new TmsTestFolder();
    subSubFolder2.setId(14L);
    subSubFolder2.setName("Sub Sub Folder 2");
    subSubFolder2.setParentTestFolder(secondSubfolder);
    subSubFolder2.setSubFolders(new ArrayList<>());

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

    assertEquals(2, exportedFolder.getSubFolders().size());

    TmsTestFolder resultSubFolder2 = exportedFolder.getSubFolders().stream()
        .filter(f -> f.getId().equals(secondSubFolderId))
        .findFirst()
        .orElse(null);

    assertNotNull(resultSubFolder2);
    assertNotNull(resultSubFolder2.getSubFolders());
    assertEquals(1, resultSubFolder2.getSubFolders().size());
    assertEquals(14L, resultSubFolder2.getSubFolders().getFirst().getId());
  }

  @Test
  void testResolveTargetFolderId_WithExistingFolderId() {
    // Arrange
    when(tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId))
        .thenReturn(true);

    // Act
    Long result = sut.resolveTargetFolderId(projectId, testFolderId, null);

    // Assert
    assertEquals(testFolderId, result);
    verify(tmsTestFolderRepository).existsByIdAndProjectId(testFolderId, projectId);
  }

  @Test
  void testResolveTargetFolderId_WithNonExistingFolderId() {
    // Arrange
    when(tmsTestFolderRepository.existsByIdAndProjectId(testFolderId, projectId))
        .thenReturn(false);

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.resolveTargetFolderId(projectId, testFolderId, null));

    assertTrue(exception.getMessage().contains(
        String.format("Test Folder with id: %d for project: %d", testFolderId, projectId)));

    verify(tmsTestFolderRepository).existsByIdAndProjectId(testFolderId, projectId);
  }

  @Test
  void testResolveTargetFolderId_WithNewFolder() {
    // Arrange
    NewTestFolderRQ newFolderRQ = NewTestFolderRQ.builder()
        .name("New Folder")
        .build();

    TmsTestFolder newFolder = new TmsTestFolder();
    newFolder.setId(testFolderId);

    when(tmsTestFolderMapper.convertFromName(projectId, "New Folder"))
        .thenReturn(newFolder);
    when(tmsTestFolderRepository.save(newFolder))
        .thenReturn(newFolder);

    // Act
    Long result = sut.resolveTargetFolderId(projectId, null, newFolderRQ);

    // Assert
    assertEquals(testFolderId, result);
    verify(tmsTestFolderMapper).convertFromName(projectId, "New Folder");
    verify(tmsTestFolderRepository).save(newFolder);
  }

  @Test
  void testResolveTargetFolderId_WithNewFolderAndParent() {
    // Arrange
    Long parentFolderId = 5L;
    NewTestFolderRQ newFolderRQ = NewTestFolderRQ.builder()
        .name("New Folder")
        .parentTestFolderId(parentFolderId)
        .build();

    TmsTestFolder newFolder = new TmsTestFolder();
    newFolder.setId(testFolderId);

    when(tmsTestFolderMapper.convertFromName(projectId, "New Folder"))
        .thenReturn(newFolder);
    when(tmsTestFolderRepository.existsByIdAndProjectId(parentFolderId, projectId))
        .thenReturn(true);
    when(tmsTestFolderMapper.convertFromId(parentFolderId))
        .thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(newFolder))
        .thenReturn(newFolder);

    // Act
    Long result = sut.resolveTargetFolderId(projectId, null, newFolderRQ);

    // Assert
    assertEquals(testFolderId, result);
    verify(tmsTestFolderMapper).convertFromName(projectId, "New Folder");
    verify(tmsTestFolderRepository).existsByIdAndProjectId(parentFolderId, projectId);
    verify(tmsTestFolderMapper).convertFromId(parentFolderId);
    verify(tmsTestFolderRepository).save(newFolder);
  }

  @Test
  void testResolveTargetFolderId_WithBothIdAndName() {
    // Arrange
    NewTestFolderRQ newFolderRQ = NewTestFolderRQ.builder()
        .name("New Folder")
        .build();

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.resolveTargetFolderId(projectId, testFolderId, newFolderRQ));

    assertTrue(exception.getMessage().contains(
        "Either target folder id or target folder name should be set"));
  }

  @Test
  void testResolveTargetFolderId_WithEmptyFolderName() {
    // Arrange
    NewTestFolderRQ newFolderRQ = NewTestFolderRQ.builder()
        .build(); // no name set

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.resolveTargetFolderId(projectId, null, newFolderRQ));

    assertTrue(exception.getMessage().contains(
        "Either target folder id or target folder name should be set"));
  }

  @Test
  void testResolveTargetFolderId_WithBothNull() {
    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.resolveTargetFolderId(projectId, null, null));

    assertTrue(exception.getMessage().contains(
        "Either target folder id or target folder name must be provided"));
  }

  @Test
  void testGetEntityById_Success() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.of(testFolder));

    // Act
    TmsTestFolder result = sut.getEntityById(projectId, testFolderId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolder.getId(), result.getId());
    assertEquals(testFolder.getName(), result.getName());

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
  }

  @Test
  void testGetEntityById_NotFound() {
    // Arrange
    when(tmsTestFolderRepository.findByIdAndProjectId(testFolderId, projectId))
        .thenReturn(Optional.empty());

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.getEntityById(projectId, testFolderId));

    assertTrue(exception.getMessage().contains(
        String.format("Test Folder with id: %d for project: %d", testFolderId, projectId)));

    verify(tmsTestFolderRepository).findByIdAndProjectId(testFolderId, projectId);
  }

  // ==================== DUPLICATION TESTS ====================

  @Test
  void testDuplicateFolder_Success() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");
    duplicatedFolder.setDescription(rootFolder.getDescription());
    duplicatedFolder.setProject(project);

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .countOfTestCases(5L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Arrays.asList(rootFolderId, firstSubFolderId, secondSubFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Arrays.asList(rootFolder, firstSubfolder, secondSubfolder));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(null)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    // Mock test case duplication
    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(anyLong()))
        .thenReturn(Arrays.asList(1L, 2L));
    BatchTestCaseOperationResultRS testCaseResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .successTestCaseIds(Arrays.asList(10L, 11L))
        .errors(Collections.emptyList())
        .build();
    when(tmsTestCaseService.duplicateTestCases(eq(projectId), any(TmsTestFolder.class),
        anyList()))
        .thenReturn(testCaseResult);

    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(5L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(5L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(100L, result.getId());
    assertEquals("Duplicated Folder", result.getName());

    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, rootFolderId);
    verify(tmsTestFolderMapper).duplicateTestFolder(eq(rootFolder), eq(null));
    verify(tmsTestFolderRepository).countTestCasesByFolderId(100L);
    verify(tmsTestFolderMapper).convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(5L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    );
  }

  @Test
  void testDuplicateFolder_WithExistingParentId() {
    // Arrange
    Long targetParentId = 50L;
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(targetParentId)
        .build();

    TmsTestFolder targetParent = new TmsTestFolder();
    targetParent.setId(targetParentId);

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");
    duplicatedFolder.setParentTestFolder(targetParent);

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .parentFolderId(targetParentId)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock parent validation
    when(tmsTestFolderRepository.existsByIdAndProjectId(targetParentId, projectId))
        .thenReturn(true);
    when(tmsTestFolderRepository.findByIdAndProjectId(targetParentId, projectId))
        .thenReturn(Optional.of(targetParent));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(targetParent)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(),
        eq(targetParentId)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(targetParentId, result.getParentFolderId());

    verify(tmsTestFolderRepository).existsByIdAndProjectId(targetParentId, projectId);
    verify(tmsTestFolderRepository).findByIdAndProjectId(targetParentId, projectId);
  }

  @Test
  void testDuplicateFolder_WithNewParentFolder() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent")
            .build())
        .build();

    TmsTestFolder newParent = new TmsTestFolder();
    newParent.setId(200L);
    newParent.setName("New Parent");

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");
    duplicatedFolder.setParentTestFolder(newParent);

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .parentFolderId(200L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock parent creation
    when(tmsTestFolderMapper.convertFromName(eq(projectId), eq("New Parent")))
        .thenReturn(newParent);
    when(tmsTestFolderRepository.save(newParent)).thenReturn(newParent);
    when(tmsTestFolderRepository.findByIdAndProjectId(200L, projectId))
        .thenReturn(Optional.of(newParent));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(newParent)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(200L)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(duplicatedFolder))
        .thenReturn(duplicatedFolder);

    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(200L, result.getParentFolderId());

    verify(tmsTestFolderMapper).convertFromName(eq(projectId), eq("New Parent"));
    verify(tmsTestFolderRepository).save(newParent);
  }

  @Test
  void testDuplicateFolder_WithNewParentFolderAndGrandparent() {
    // Arrange
    Long grandparentId = 50L;
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent")
            .parentTestFolderId(grandparentId)
            .build())
        .build();

    TmsTestFolder grandparent = new TmsTestFolder();
    grandparent.setId(grandparentId);

    TmsTestFolder newParent = new TmsTestFolder();
    newParent.setId(200L);
    newParent.setName("New Parent");

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");
    duplicatedFolder.setParentTestFolder(newParent);

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .parentFolderId(200L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock parent creation with grandparent
    when(tmsTestFolderRepository.existsByIdAndProjectId(grandparentId, projectId))
        .thenReturn(true);
    when(tmsTestFolderMapper.convertFromName(eq(projectId), eq("New Parent")))
        .thenReturn(newParent);
    when(tmsTestFolderMapper.convertFromId(grandparentId))
        .thenReturn(grandparent);
    when(tmsTestFolderRepository.save(newParent)).thenReturn(newParent);
    when(tmsTestFolderRepository.findByIdAndProjectId(200L, projectId))
        .thenReturn(Optional.of(newParent));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(newParent)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(200L)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(duplicatedFolder))
        .thenReturn(duplicatedFolder);

    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(200L, result.getParentFolderId());

    verify(tmsTestFolderRepository).existsByIdAndProjectId(grandparentId, projectId);
    verify(tmsTestFolderMapper).convertFromName(eq(projectId), eq("New Parent"));
    verify(tmsTestFolderMapper).convertFromId(grandparentId);
    verify(tmsTestFolderRepository).save(newParent);
  }

  @Test
  void testDuplicateFolder_WithNonExistentParentId() {
    // Arrange
    Long nonExistentParentId = 999L;
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(nonExistentParentId)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    when(tmsTestFolderRepository.existsByIdAndProjectId(nonExistentParentId, projectId))
        .thenReturn(false);

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicateFolder(projectId, rootFolderId, duplicateRQ));

    assertTrue(exception.getMessage().contains(
        String.format("Test Folder with id: %d for project: %d", nonExistentParentId, projectId)));

    verify(tmsTestFolderRepository).existsByIdAndProjectId(nonExistentParentId, projectId);
  }

  @Test
  void testDuplicateFolder_WithNewParentFolderHavingNonExistentGrandparent() {
    // Arrange
    Long nonExistentGrandparentId = 999L;
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(NewTestFolderRQ.builder()
            .name("New Parent")
            .parentTestFolderId(nonExistentGrandparentId)
            .build())
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    when(tmsTestFolderRepository.existsByIdAndProjectId(nonExistentGrandparentId, projectId))
        .thenReturn(false);

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicateFolder(projectId, rootFolderId, duplicateRQ));

    assertTrue(exception.getMessage().contains(
        String.format("Test Folder with id: %d for project: %d", nonExistentGrandparentId,
            projectId)));

    verify(tmsTestFolderRepository).existsByIdAndProjectId(nonExistentGrandparentId, projectId);
  }

  @Test
  void testDuplicateFolder_WithBothParentOptions() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(50L)
        .parentTestFolder(NewTestFolderRQ.builder().name("New Parent").build())
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicateFolder(projectId, rootFolderId, duplicateRQ));

    assertTrue(exception.getMessage().contains(
        "Either parent folder id or parent folder name should be set"));
  }

  @Test
  void testDuplicateFolder_WithParentFolderButNoName() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(NewTestFolderRQ.builder().build()) // no name
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicateFolder(projectId, rootFolderId, duplicateRQ));

    assertTrue(exception.getMessage().contains(
        "Either parent folder id or parent folder name should be set"));
  }

  @Test
  void testDuplicateFolder_SourceFolderNotFound() {
    // Arrange
    Long nonExistentFolderId = 999L;
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, nonExistentFolderId))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    var exception = assertThrows(ReportPortalException.class, () ->
        sut.duplicateFolder(projectId, nonExistentFolderId, duplicateRQ));

    assertTrue(exception.getMessage().contains(
        String.format("'Test Folder with id: %d for project: %d'", nonExistentFolderId,
            projectId)));

    verify(tmsTestFolderRepository).findAllFolderIdsInHierarchy(projectId, nonExistentFolderId);
  }

  @Test
  void testDuplicateFolder_WithNameConflict_GeneratesUniqueName() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Existing Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Existing Folder-1");

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Existing Folder-1")
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock name conflict
    when(tmsTestFolderRepository.existsByNameAndTestFolder(projectId, "Existing Folder", null))
        .thenReturn(true);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(projectId, "Existing Folder-1", null))
        .thenReturn(false);

    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(anyLong()))
        .thenReturn(Collections.emptyList());
    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals("Existing Folder-1", result.getName());

    verify(tmsTestFolderRepository).existsByNameAndTestFolder(projectId, "Existing Folder", null);
    verify(tmsTestFolderRepository).existsByNameAndTestFolder(projectId, "Existing Folder-1",
        null);
  }

  @Test
  void testDuplicateFolder_WithTestCases_DuplicatesAndMovesToNewFolder() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");

    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    BatchTestCaseOperationResultRS testCaseResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(3)
        .successCount(3)
        .failureCount(0)
        .successTestCaseIds(Arrays.asList(10L, 11L, 12L))
        .errors(Collections.emptyList())
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .countOfTestCases(3L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(null)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    // Mock test case duplication - now passes entities instead of IDs
    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(rootFolderId))
        .thenReturn(testCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, duplicatedFolder, testCaseIds))
        .thenReturn(testCaseResult);

    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(3L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(3L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(3L, result.getCountOfTestCases());

    verify(tmsTestFolderRepository).findTestCaseIdsByFolderId(rootFolderId);
    verify(tmsTestCaseService).duplicateTestCases(projectId, duplicatedFolder, testCaseIds);
    // Note: updateTestCaseFolder is no longer called, it's handled inside duplicateTestCases
    verify(tmsTestFolderRepository, never()).updateTestCaseFolder(anyList(), anyLong());
  }

  @Test
  void testDuplicateFolder_WithTestCaseDuplicationFailure_RecordsErrors() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");

    List<Long> testCaseIds = Arrays.asList(1L, 2L);
    BatchTestCaseOperationResultRS testCaseResult = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(1)
        .failureCount(1)
        .successTestCaseIds(List.of(10L))
        .errors(List.of(new com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationError(
            2L, "Failed to duplicate test case")))
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .countOfTestCases(1L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(null)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    // Mock test case duplication with partial failure
    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(rootFolderId))
        .thenReturn(testCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, duplicatedFolder, testCaseIds))
        .thenReturn(testCaseResult);

    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(1L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(1L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);

    verify(tmsTestCaseService).duplicateTestCases(projectId, duplicatedFolder, testCaseIds);
  }

  @Test
  void testDuplicateFolder_WithTestCaseDuplicationException_RecordsAllTestCasesAsErrors() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Folder");

    List<Long> testCaseIds = Arrays.asList(1L, 2L);

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Folder")
        .countOfTestCases(0L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(null)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    // Mock test case duplication with exception
    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(rootFolderId))
        .thenReturn(testCaseIds);
    when(tmsTestCaseService.duplicateTestCases(projectId, duplicatedFolder, testCaseIds))
        .thenThrow(new RuntimeException("Test case duplication failed"));

    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);

    verify(tmsTestCaseService).duplicateTestCases(projectId, duplicatedFolder, testCaseIds);
  }

  @Test
  void testDuplicateFolder_NoTestCases_Success() {
    // Arrange
    TmsTestFolderRQ duplicateRQ = TmsTestFolderRQ.builder()
        .name("Duplicated Empty Folder")
        .build();

    TmsTestFolder duplicatedFolder = new TmsTestFolder();
    duplicatedFolder.setId(100L);
    duplicatedFolder.setName("Duplicated Empty Folder");

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(100L)
        .name("Duplicated Empty Folder")
        .countOfTestCases(0L)
        .build();

    // Mock hierarchy loading
    List<Long> folderIds = Collections.singletonList(rootFolderId);
    when(tmsTestFolderRepository.findAllFolderIdsInHierarchy(projectId, rootFolderId))
        .thenReturn(folderIds);
    when(tmsTestFolderRepository.findAllById(folderIds))
        .thenReturn(Collections.singletonList(rootFolder));

    // Mock duplication
    when(tmsTestFolderMapper.duplicateTestFolder(eq(rootFolder), eq(null)))
        .thenReturn(duplicatedFolder);
    when(tmsTestFolderRepository.existsByNameAndTestFolder(eq(projectId), anyString(), eq(null)))
        .thenReturn(false);
    when(tmsTestFolderRepository.save(any(TmsTestFolder.class)))
        .thenReturn(duplicatedFolder);

    // Mock no test cases
    when(tmsTestFolderRepository.findTestCaseIdsByFolderId(rootFolderId))
        .thenReturn(Collections.emptyList());

    when(tmsTestFolderRepository.countTestCasesByFolderId(100L)).thenReturn(0L);
    when(tmsTestFolderMapper.convertToDuplicateTmsTestFolderRS(
        eq(duplicatedFolder),
        eq(0L),
        any(FolderDuplicationStatistics.class),
        any(TestCaseDuplicationStatistics.class)
    )).thenReturn(expectedResponse);

    // Act
    DuplicateTmsTestFolderRS result = sut.duplicateFolder(projectId, rootFolderId, duplicateRQ);

    // Assert
    assertNotNull(result);
    assertEquals(0L, result.getCountOfTestCases());

    verify(tmsTestFolderRepository).findTestCaseIdsByFolderId(rootFolderId);
    verify(tmsTestCaseService, never()).duplicateTestCases(anyLong(), any(TmsTestFolder.class),
        anyList());
  }

  // ==================== TEST PLAN FOLDER RETRIEVAL TESTS ====================

  @Test
  void testGetFoldersByTestPlanId_Success() {
    // Arrange
    Long testPlanId = 15L;
    TmsTestFolderWithCountOfTestCases folder1 = new TmsTestFolderWithCountOfTestCases(testFolder,
        2L);
    TmsTestFolderWithCountOfTestCases folder2 = new TmsTestFolderWithCountOfTestCases(
        parentTestFolder, 1L);

    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> folderPage =
        new PageImpl<>(Arrays.asList(folder1, folder2), pageable, 2);

    Page<TmsTestFolderRS> folderRSPage = new Page<>(
        Arrays.asList(testFolderRS, testFolderRS),
        10L, 0L, 2L
    );

    when(tmsTestFolderRepository.findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderMapper.convert(folderPage))
        .thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByTestPlanId(projectId, testPlanId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, pageable);
    verify(tmsTestFolderMapper).convert(folderPage);
  }

  @Test
  void testGetFoldersByTestPlanId_EmptyPage() {
    // Arrange
    Long testPlanId = 15L;
    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> emptyPage =
        new PageImpl<>(Collections.emptyList(), pageable, 0);

    Page<TmsTestFolderRS> emptyRSPage = new Page<>(
        Collections.emptyList(), 10L, 0L, 0L
    );

    when(tmsTestFolderRepository.findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, pageable))
        .thenReturn(emptyPage);
    when(tmsTestFolderMapper.convert(emptyPage))
        .thenReturn(emptyRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByTestPlanId(projectId, testPlanId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, pageable);
    verify(tmsTestFolderMapper).convert(emptyPage);
  }

  @Test
  void testGetFoldersByTestPlanId_WithMultiplePages() {
    // Arrange
    Long testPlanId = 20L;
    Pageable secondPage = PageRequest.of(1, 10);

    TmsTestFolderWithCountOfTestCases folder1 = new TmsTestFolderWithCountOfTestCases(testFolder,
        5L);

    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> folderPage =
        new PageImpl<>(Collections.singletonList(folder1), secondPage, 15);

    Page<TmsTestFolderRS> folderRSPage = new Page<>(
        Collections.singletonList(testFolderRS),
        10L, 1L, 15L
    );

    when(tmsTestFolderRepository.findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, secondPage))
        .thenReturn(folderPage);
    when(tmsTestFolderMapper.convert(folderPage))
        .thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByTestPlanId(projectId, testPlanId, secondPage);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(1L, result.getPage().getNumber());
    assertEquals(15L, result.getPage().getTotalElements());

    verify(tmsTestFolderRepository).findAllByProjectIdAndTestPlanIdWithCountOfTestCases(
        projectId, testPlanId, secondPage);
    verify(tmsTestFolderMapper).convert(folderPage);
  }
}
