package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.identity.TestCaseHashGenerator;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.mapper.SuiteTestItemBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TmsTestFolderTestItemFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TmsTestFolderTestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.base.model.Page;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TestFolderItemServiceImplTest {

  private final Long projectId = 1L;
  private final Long launchId = 100L;
  private final Long folderId = 200L;
  private final Long itemId = 300L;
  @Mock
  private TestItemRepository testItemRepository;
  @Mock
  private TmsTestFolderTestItemRepository testFolderTestItemRepository;
  @Mock
  private TmsTestFolderTestItemFilterableRepository tmsTestFolderTestItemFilterableRepository;
  @Mock
  private TmsTestFolderService tmsTestFolderService;
  @Mock
  private SuiteTestItemBuilder suiteItemBuilder;
  @Mock
  private TestCaseHashGenerator testCaseHashGenerator;
  @InjectMocks
  private TestFolderItemServiceImpl sut;
  private Launch launch;
  private TestItem suiteItem;
  private TmsTestFolder testFolder;

  @BeforeEach
  void setUp() {
    launch = new Launch();
    launch.setId(launchId);
    launch.setProjectId(projectId);

    suiteItem = new TestItem();
    suiteItem.setItemId(itemId);
    suiteItem.setLaunchId(launchId);
    suiteItem.setPath(String.valueOf(itemId));

    testFolder = new TmsTestFolder();
    testFolder.setId(folderId);
    testFolder.setName("Folder Name");
  }

  // -------------------------------------------------------------------------
  // FIND OR CREATE SUITE ITEM
  // -------------------------------------------------------------------------

  @Test
  void findTestFolderItem_WhenExists_ShouldReturnExistingSuite() {
    when(testItemRepository.findSuiteItemInLaunchForFolder(launchId, folderId))
        .thenReturn(Optional.of(suiteItem));

    var result = sut.findTestFolderItem(projectId, folderId, launch);

    assertNotNull(result);
    assertEquals(itemId, result.getItemId());
    verify(testItemRepository).findSuiteItemInLaunchForFolder(launchId, folderId);
    verify(tmsTestFolderService, never()).getEntityById(anyLong(), anyLong());
  }

  @Test
  void findTestFolderItem_WhenDoesNotExist_ShouldCreateNewSuite() {
    when(testItemRepository.findSuiteItemInLaunchForFolder(launchId, folderId))
        .thenReturn(Optional.empty());

    when(tmsTestFolderService.getEntityById(projectId, folderId)).thenReturn(testFolder);
    when(suiteItemBuilder.buildSuiteItem(testFolder, launch, folderId)).thenReturn(suiteItem);
    when(testCaseHashGenerator.generate(any(), any(), any())).thenReturn(12345);
    when(testItemRepository.save(suiteItem)).thenReturn(suiteItem);

    when(testFolderTestItemRepository.existsByTestFolderIdAndTestItemId(folderId, itemId))
        .thenReturn(false);

    var result = sut.findTestFolderItem(projectId, folderId, launch);

    assertNotNull(result);
    assertEquals(itemId, result.getItemId());
    verify(testItemRepository).save(suiteItem);
    verify(testFolderTestItemRepository).save(any(TmsTestFolderTestItem.class));
  }

  @Test
  void createTestFolderSuiteItem_WithParentFolder_ShouldRecursivelyCreateParentSuite() {
    // Setup nested folder
    TmsTestFolder parentFolder = new TmsTestFolder();
    parentFolder.setId(500L);
    parentFolder.setName("Parent Folder");

    testFolder.setParentTestFolder(parentFolder);

    TestItem parentSuite = new TestItem();
    parentSuite.setItemId(600L);
    parentSuite.setLaunchId(launchId);
    parentSuite.setPath(String.valueOf(600L));

    // When searching for parent suite in recursion, return empty so it gets created
    when(testItemRepository.findSuiteItemInLaunchForFolder(launchId, 500L))
        .thenReturn(Optional.of(parentSuite));

    // Current Folder Creation
    when(tmsTestFolderService.getEntityById(projectId, folderId)).thenReturn(testFolder);
    when(suiteItemBuilder.buildSuiteItem(testFolder, launch, folderId)).thenReturn(suiteItem);
    when(testItemRepository.save(suiteItem)).thenReturn(suiteItem);
    when(testCaseHashGenerator.generate(any(), any(), any())).thenReturn(111);

    sut.createTestFolderSuiteItem(projectId, folderId, launch);

    // Parent suite should be marked as having children
    assertTrue(parentSuite.isHasChildren());
    verify(testItemRepository).save(parentSuite);

    // Child suite path should be updated
    assertEquals("600.300", suiteItem.getPath());
    assertEquals(600L, suiteItem.getParentId());

    // Should save suite again after updating path
    verify(testItemRepository, times(3)).save(any(TestItem.class));
  }

  // -------------------------------------------------------------------------
  // GET SUITE FOLDERS
  // -------------------------------------------------------------------------

  @Test
  void getSuiteFoldersByLaunch_WhenEmpty_ShouldReturnEmptyPage() {
    Filter filter = mock(Filter.class);
    Pageable pageable = PageRequest.of(0, 10);

    when(tmsTestFolderTestItemFilterableRepository.findByFilter(filter, pageable))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    Page<TmsTestFolderRS> result = sut.getSuiteFoldersByLaunch(projectId, launchId, filter,
        pageable);

    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getPage().getTotalElements());
  }

  @Test
  void getSuiteFoldersByLaunch_WithData_ShouldReturnMappedFolders() {
    Filter filter = new Filter(TmsTestFolderTestItem.class, new java.util.ArrayList<>());
    Pageable pageable = PageRequest.of(0, 10);

    TmsTestFolderTestItem folderItem = TmsTestFolderTestItem.builder()
        .testFolderId(folderId)
        .launchId(launchId)
        .testItem(suiteItem)
        .name("Suite Name")
        .description("Suite Desc")
        .build();

    when(tmsTestFolderTestItemFilterableRepository.findByFilter(any(Filter.class), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of(folderItem)));

    when(tmsTestFolderTestItemFilterableRepository.countTestCasesByFolderIdsAndFilter(any(), any()))
        .thenReturn(Map.of(itemId, 5L));

    Page<TmsTestFolderRS> result = sut.getSuiteFoldersByLaunch(projectId, launchId, filter,
        pageable);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());

    TmsTestFolderRS rs = result.getContent().stream().findFirst().orElse(null);
    assertNotNull(rs);
    assertEquals(itemId, rs.getId());
    assertEquals("Suite Name", rs.getName());
    assertEquals("Suite Desc", rs.getDescription());
    assertEquals(5L, rs.getCountOfTestCases());
  }

  // -------------------------------------------------------------------------
  // DELETE
  // -------------------------------------------------------------------------

  @Test
  void deleteTestFolderTestItemByTestItemId_ShouldDeleteLinks() {
    sut.deleteTestFolderTestItemByTestItemId(itemId);
    verify(testFolderTestItemRepository).deleteByTestItem_ItemId(itemId);
  }

  @Test
  void deleteByLaunchId_ShouldDeleteLinks() {
    sut.deleteByLaunchId(launchId);
    verify(testFolderTestItemRepository).deleteByLaunchId(launchId);
  }
}
