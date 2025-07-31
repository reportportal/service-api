package com.epam.ta.reportportal.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolder;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderIdWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestFolderWithCountOfTestCases;
import com.epam.ta.reportportal.core.tms.db.repository.TmsTestFolderRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ.ParentTmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.mapper.TmsTestFolderMapper;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestFolderExporter;
import com.epam.ta.reportportal.core.tms.mapper.factory.TmsTestFolderExporterFactory;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.model.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private final Map<Long, Long> emptySubFolderTestCaseCounts = Collections.emptyMap();

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
  private TmsTestFolder subFolder1;
  private TmsTestFolder subFolder2;
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

    testFolderRQ = TmsTestFolderRQ.builder()
        .name("Test Folder")
        .description("Test Description")
        .parentTestFolder(ParentTmsTestFolderRQ.builder().id(3L).build())
        .build();

    testFolderRS = TmsTestFolderRS.builder()
        .id(testFolderId)
        .name("Test Folder")
        .description("Test Description")
        .countOfTestCases(0L)
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
  void testCreate() {
    // Arrange
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
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
    verify(tmsTestFolderMapper).convertFromId(3L);
    verify(tmsTestFolderRepository).save(testFolder);
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
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
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.create(projectId, rqWithParentName);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderMapper).convertFromRQ(projectId, rqWithParentName);
    verify(tmsTestFolderMapper).convertFromName(projectId, "Parent Folder");
    verify(tmsTestFolderRepository, times(2)).save(any(TmsTestFolder.class));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderToRS(testFolder);
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
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.update(projectId, testFolderId, testFolderRQ);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

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
    when(tmsTestFolderMapper.convertFromRQ(projectId, testFolderRQ)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromId(3L)).thenReturn(parentTestFolder);
    when(tmsTestFolderRepository.save(testFolder)).thenReturn(testFolder);
    when(tmsTestFolderMapper.convertFromTmsTestFolderToRS(testFolder)).thenReturn(testFolderRS);

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

    Map<Long, Long> subFolderTestCaseCounts = new HashMap<>();
    subFolderTestCaseCounts.put(4L, 2L); // subFolder1 has 2 test cases
    subFolderTestCaseCounts.put(5L, 1L); // subFolder2 has 1 test case

    List<TmsTestFolderIdWithCountOfTestCases> testCaseCountResults = Arrays.asList(
        new TmsTestFolderIdWithCountOfTestCases(4L, 2L),
        new TmsTestFolderIdWithCountOfTestCases(5L, 1L)
    );

    when(tmsTestFolderRepository.findByIdWithCountOfTestCases(projectId, 3L))
        .thenReturn(Optional.of(folderWithCount));
    when(tmsTestFolderRepository.findByIdWithSubFolders(projectId, 3L))
        .thenReturn(Optional.of(parentTestFolder));
    when(tmsTestFolderRepository.findTestCaseCountsByFolderIds(projectId, Arrays.asList(4L, 5L)))
        .thenReturn(testCaseCountResults);
    when(tmsTestFolderMapper.convertFromTmsTestFolderWithCountOfTestCasesToRS(
        folderWithCount, subFolderTestCaseCounts))
        .thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.getById(projectId, 3L);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdWithCountOfTestCases(projectId, 3L);
    verify(tmsTestFolderRepository).findByIdWithSubFolders(projectId, 3L);
    verify(tmsTestFolderRepository).findTestCaseCountsByFolderIds(projectId, Arrays.asList(4L, 5L));
    verify(tmsTestFolderMapper).convertFromTmsTestFolderWithCountOfTestCasesToRS(
        folderWithCount, subFolderTestCaseCounts);
  }

  @Test
  void testGetByIdWithoutSubFolders() {
    // Arrange
    TmsTestFolder folderWithoutSubFolders = new TmsTestFolder();
    folderWithoutSubFolders.setId(testFolderId);
    folderWithoutSubFolders.setSubFolders(new ArrayList<>());

    TmsTestFolderWithCountOfTestCases folderWithCount = new TmsTestFolderWithCountOfTestCases(
        folderWithoutSubFolders, 3L);

    when(tmsTestFolderRepository.findByIdWithCountOfTestCases(projectId, testFolderId))
        .thenReturn(Optional.of(folderWithCount));
    when(tmsTestFolderRepository.findByIdWithSubFolders(projectId, testFolderId))
        .thenReturn(Optional.of(folderWithoutSubFolders));
    when(tmsTestFolderMapper.convertFromTmsTestFolderWithCountOfTestCasesToRS(
        folderWithCount, emptySubFolderTestCaseCounts))
        .thenReturn(testFolderRS);

    // Act
    TmsTestFolderRS result = sut.getById(projectId, testFolderId);

    // Assert
    assertNotNull(result);
    assertEquals(testFolderRS.getId(), result.getId());

    verify(tmsTestFolderRepository).findByIdWithCountOfTestCases(projectId, testFolderId);
    verify(tmsTestFolderRepository).findByIdWithSubFolders(projectId, testFolderId);
    verify(tmsTestFolderRepository, never()).findTestCaseCountsByFolderIds(eq(projectId),
        anyList());
    verify(tmsTestFolderMapper).convertFromTmsTestFolderWithCountOfTestCasesToRS(
        folderWithCount, emptySubFolderTestCaseCounts);
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
    verify(tmsTestFolderMapper, never()).convertFromTmsTestFolderWithCountOfTestCasesToRS(
        any(TmsTestFolderWithCountOfTestCases.class), anyMap());
  }

  @Test
  void testGetFoldersByProjectID() {
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

    List<Long> folderIds = Arrays.asList(testFolderId, 3L);
    List<TmsTestFolder> foldersWithSubFolders = Arrays.asList(testFolder, parentTestFolder);

    Map<Long, Long> subFolderTestCaseCounts = new HashMap<>();
    subFolderTestCaseCounts.put(4L, 2L);
    subFolderTestCaseCounts.put(5L, 1L);

    List<TmsTestFolderIdWithCountOfTestCases> testCaseCountResults = Arrays.asList(
        new TmsTestFolderIdWithCountOfTestCases(4L, 2L),
        new TmsTestFolderIdWithCountOfTestCases(5L, 1L)
    );

    when(tmsTestFolderRepository.findAllByProjectIdWithCountOfTestCases(projectId, pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderRepository.findByIdsWithSubFolders(folderIds))
        .thenReturn(foldersWithSubFolders);
    when(tmsTestFolderRepository.findTestCaseCountsByFolderIds(projectId, Arrays.asList(4L, 5L)))
        .thenReturn(testCaseCountResults);
    when(tmsTestFolderMapper.convert(folderPage, subFolderTestCaseCounts))
        .thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByProjectID(projectId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByProjectIdWithCountOfTestCases(projectId, pageable);
    verify(tmsTestFolderRepository).findByIdsWithSubFolders(folderIds);
    verify(tmsTestFolderRepository).findTestCaseCountsByFolderIds(projectId, Arrays.asList(4L, 5L));
    verify(tmsTestFolderMapper).convert(folderPage, subFolderTestCaseCounts);
  }

  @Test
  void testGetFoldersByProjectIDEmptyPage() {
    // Arrange
    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> emptyPage =
        new PageImpl<>(Collections.emptyList(), pageable, 0);

    Page<TmsTestFolderRS> emptyRSPage = new Page<>(
        Collections.emptyList(), 10L, 0L, 0L
    );

    when(tmsTestFolderRepository.findAllByProjectIdWithCountOfTestCases(projectId, pageable))
        .thenReturn(emptyPage);
    when(tmsTestFolderMapper.convert(emptyPage))
        .thenReturn(emptyRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getFoldersByProjectID(projectId, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByProjectIdWithCountOfTestCases(projectId, pageable);
    verify(tmsTestFolderRepository, never()).findByIdsWithSubFolders(anyList());
    verify(tmsTestFolderRepository, never()).findTestCaseCountsByFolderIds(eq(projectId), anyList());
    verify(tmsTestFolderMapper).convert(emptyPage);
  }

  @Test
  void testGetSubFolders() {
    // Arrange
    TmsTestFolderWithCountOfTestCases folder = new TmsTestFolderWithCountOfTestCases(testFolder, 0L);

    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> folderPage =
        new PageImpl<>(Collections.singletonList(folder), pageable, 1);

    Page<TmsTestFolderRS> folderRSPage = new Page<>(
        Collections.singletonList(testFolderRS), 10L, 0L, 1L
    );

    List<Long> folderIds = Collections.singletonList(testFolderId);
    List<TmsTestFolder> foldersWithSubFolders = Collections.singletonList(testFolder);

    when(tmsTestFolderRepository.findAllByParentTestFolderIdWithCountOfTestCases(projectId,
        parentTestFolder.getId(), pageable))
        .thenReturn(folderPage);
    when(tmsTestFolderRepository.findByIdsWithSubFolders(folderIds))
        .thenReturn(foldersWithSubFolders);
    when(tmsTestFolderMapper.convert(folderPage, emptySubFolderTestCaseCounts))
        .thenReturn(folderRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getSubFolders(projectId, parentTestFolder.getId(), pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByParentTestFolderIdWithCountOfTestCases(projectId,
        parentTestFolder.getId(), pageable);
    verify(tmsTestFolderRepository).findByIdsWithSubFolders(folderIds);
    verify(tmsTestFolderRepository, never()).findTestCaseCountsByFolderIds(eq(projectId),
        anyList());
    verify(tmsTestFolderMapper).convert(folderPage, emptySubFolderTestCaseCounts);
  }

  @Test
  void testGetSubFoldersEmptyPage() {
    // Arrange
    org.springframework.data.domain.Page<TmsTestFolderWithCountOfTestCases> emptyPage =
        new PageImpl<>(Collections.emptyList(), pageable, 0);

    Page<TmsTestFolderRS> emptyRSPage = new Page<>(
        Collections.emptyList(), 10L, 0L, 0L
    );

    when(tmsTestFolderRepository.findAllByParentTestFolderIdWithCountOfTestCases(projectId,
        parentTestFolder.getId(), pageable))
        .thenReturn(emptyPage);
    when(tmsTestFolderMapper.convert(emptyPage))
        .thenReturn(emptyRSPage);

    // Act
    Page<TmsTestFolderRS> result = sut.getSubFolders(projectId, parentTestFolder.getId(), pageable);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getContent().size());

    verify(tmsTestFolderRepository).findAllByParentTestFolderIdWithCountOfTestCases(projectId,
        parentTestFolder.getId(), pageable);
    verify(tmsTestFolderRepository, never()).findByIdsWithSubFolders(anyList());
    verify(tmsTestFolderRepository, never()).findTestCaseCountsByFolderIds(eq(projectId),
        anyList());
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
}
