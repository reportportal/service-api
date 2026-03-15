package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_LAUNCH_ID;

import com.epam.reportportal.base.core.item.identity.IdentityUtil;
import com.epam.reportportal.base.core.item.identity.TestCaseHashGenerator;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.mapper.SuiteTestItemBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TmsTestFolderTestItemFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TmsTestFolderTestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.ws.converter.PagedResourcesAssembler;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing SUITE test items (test folder containers in manual launches). Handles
 * creation, retrieval, linking, and listing SUITE items by launch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestFolderItemServiceImpl implements TestFolderItemService {

  private final TestItemRepository testItemRepository;
  private final TmsTestFolderTestItemRepository testFolderTestItemRepository;
  private final TmsTestFolderTestItemFilterableRepository tmsTestFolderTestItemFilterableRepository;
  private final TmsTestFolderService tmsTestFolderService;
  private final SuiteTestItemBuilder suiteItemBuilder;
  private final TestCaseHashGenerator testCaseHashGenerator;

  /**
   * Finds or creates a SUITE item for a test folder in a launch. If the folder has a parent folder,
   * ensures parent SUITE is created and sets parent relation.
   *
   * @param projectId    project ID
   * @param testFolderId test folder ID
   * @param launch       launch entity
   * @return SUITE test item (persisted)
   */
  @Transactional
  @Override
  public TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch) {
    log.debug("Finding or creating SUITE item for test folder: {} in launch: {}",
        testFolderId, launch.getId());

    // Try to find existing SUITE item
    var existingSuite = testItemRepository.findSuiteItemInLaunchForFolder(launch.getId(),
        testFolderId);
    if (existingSuite.isPresent()) {
      log.debug("Found existing SUITE item: {} for test folder: {}",
          existingSuite.get().getItemId(), testFolderId);
      return existingSuite.get();
    }

    // Create new SUITE item
    var suiteItem = createTestFolderSuiteItem(projectId, testFolderId, launch);
    log.debug("Created new SUITE item: {} for test folder: {}", suiteItem.getItemId(),
        testFolderId);
    return suiteItem;
  }

  /**
   * Creates SUITE item and sets parent SUITE relation if the folder has a parent folder.
   */
  @Transactional
  @Override
  public TestItem createTestFolderSuiteItem(Long projectId, Long testFolderId,
      Launch launch) {
    log.debug("Creating SUITE item for test folder: {}", testFolderId);

    // Load test folder metadata
    TmsTestFolder testFolder = null;
    try {
      testFolder = tmsTestFolderService.getEntityById(projectId, testFolderId);
      log.trace("Loaded test folder metadata: {}", testFolder.getName());
    } catch (Exception e) {
      log.warn("Failed to load test folder metadata for: {}, using default name", testFolderId);
    }

    // Create SUITE item without a parent reference
    var suiteItem = suiteItemBuilder.buildSuiteItem(testFolder, launch, testFolderId);
    suiteItem.setTestCaseHash(
        testCaseHashGenerator.generate(
            suiteItem,
            IdentityUtil.getParentIds(suiteItem),
            launch.getProjectId()
        )
    );
    suiteItem = testItemRepository.save(suiteItem);
    log.trace("Persisted SUITE item with ID: {}", suiteItem.getItemId());

    // If folder has a parent, ensure parent SUITE exists and set parent relation
    if (testFolder != null && testFolder.getParentTestFolder() != null) {
      var parentFolderId = testFolder.getParentTestFolder().getId();
      var parentSuite = findTestFolderItem(projectId, parentFolderId,
          launch); // recursion ensures parent exists
      markAsHavingChildren(parentSuite);

      // Set parentId and complete path
      suiteItem.setParentId(parentSuite.getItemId());
      suiteItem.setPath(parentSuite.getPath() + "." + suiteItem.getItemId());
      suiteItem = testItemRepository.save(suiteItem);
    }

    // Create junction record linking SUITE item to test folder
    createFolderTestItem(testFolder, suiteItem);

    return suiteItem;
  }

  /**
   * Creates a junction record linking test folder to SUITE test item. Sets launchId, name and
   * description in junction for per-launch display.
   */
  private void createFolderTestItem(TmsTestFolder testFolder, TestItem suiteItem) {
    if (testFolder == null || suiteItem == null) {
      log.warn("Cannot create folder-item link: testFolder or suiteItem is null");
      return;
    }

    log.debug("Creating test folder-test item link for folder: {} and item: {}",
        testFolder.getId(), suiteItem.getItemId());

    // Check if link already exists (safety check)
    if (testFolderTestItemRepository.existsByTestFolderIdAndTestItemId(testFolder.getId(),
        suiteItem.getItemId())) {
      log.debug("Link already exists between folder: {} and item: {}", testFolder.getId(),
          suiteItem.getItemId());
      return;
    }

    var folderTestItem = TmsTestFolderTestItem.builder()
        .testFolderId(testFolder.getId())
        .launchId(suiteItem.getLaunchId())
        .testItem(suiteItem)
        .name(testFolder.getName())
        .description(testFolder.getDescription())
        .build();

    testFolderTestItemRepository.save(folderTestItem);
    log.trace("Created junction record linking folder: {} to item: {}", testFolder.getId(),
        suiteItem.getItemId());
  }

  /**
   * Marks SUITE item as having stat-aware children.
   */
  @Transactional
  @Override
  public void markAsHavingChildren(TestItem testFolderItem) {
    if (testFolderItem != null && !testFolderItem.isHasChildren()) {
      log.debug("Marking SUITE item: {} as having children", testFolderItem.getItemId());
      testFolderItem.setHasChildren(true);
      testItemRepository.save(testFolderItem);
    }
  }

  /**
   * Removes junction link between test folder and test item.
   */
  @Transactional
  @Override
  public void deleteTestFolderTestItemByTestItemId(Long testItemId) {
    log.debug("Removing test folder-test item links for item: {}", testItemId);
    testFolderTestItemRepository.deleteByTestItem_ItemId(testItemId);
    log.trace("Removed links for item: {}", testItemId);
  }

  /**
   * Retrieves a flat list of SUITE test items mapped to TmsTestFolderRS for a given launch. Uses
   * junction metadata (name/description), counts TEST children, and resolves parent SUITE id.
   *
   * @param launchId the Launch ID
   * @param filter   filter
   * @param pageable pagination
   * @return page of TmsTestFolderRS
   */
  @Transactional(readOnly = true)
  @Override
  public Page<TmsTestFolderRS> getSuiteFoldersByLaunch(
      Long projectId,
      Long launchId,
      Filter filter,
      Pageable pageable) {

    // Ensure filter has launchId
    filter.withCondition(
        FilterCondition.builder()
            .eq(CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_LAUNCH_ID, String.valueOf(launchId))
            .build()
    );

    var suitePage = tmsTestFolderTestItemFilterableRepository.findByFilter(filter, pageable);

    if (suitePage.isEmpty()) {
      return PagedResourcesAssembler.<TmsTestFolderRS>pageConverter()
          .apply(new PageImpl<>(java.util.Collections.emptyList(), pageable, 0));
    }

    var suiteItemIds = suitePage.stream()
        .map(s -> s.getTestItem().getItemId())
        .toList();

    var testCasesCounts = tmsTestFolderTestItemFilterableRepository
        .countTestCasesByFolderIdsAndFilter(suiteItemIds, filter);

    var allParentItemIds = testItemRepository.findAllParentItemIds(launchId, suiteItemIds);
    var missingParentIds = allParentItemIds
        .stream()
        .filter(id -> !suiteItemIds.contains(id))
        .toList();

    List<TmsTestFolderTestItem> missingParents = Collections.emptyList();
    if (!missingParentIds.isEmpty()) {
      missingParents = testFolderTestItemRepository.findAllByLaunchIdAndTestItemItemIdIn(launchId,
          missingParentIds);
    }

    var resultList = new java.util.ArrayList<TmsTestFolderRS>();

    resultList.addAll(suitePage.getContent().stream().map(suiteTestItem -> {
      var suiteItem = suiteTestItem.getTestItem();
      var testCaseCount = 0L;
      Long parentSuiteId = null;

      if (suiteItem != null) {
        testCaseCount = testCasesCounts.getOrDefault(suiteItem.getItemId(), 0L);
        parentSuiteId = suiteItem.getParentId();
      }

      return TmsTestFolderRS.builder()
          .id(suiteItem != null ? suiteItem.getItemId() : null)
          .name(suiteTestItem.getName() != null ? suiteTestItem.getName()
              : (suiteItem != null ? suiteItem.getName() : null))
          .description(suiteTestItem.getDescription() != null ? suiteTestItem.getDescription()
              : (suiteItem != null ? suiteItem.getDescription() : null))
          .countOfTestCases(testCaseCount)
          .parentFolderId(parentSuiteId)
          .build();
    }).toList());

    resultList.addAll(missingParents.stream().map(suiteTestItem -> {
      var suiteItem = suiteTestItem.getTestItem();
      Long parentSuiteId = suiteItem != null ? suiteItem.getParentId() : null;

      return TmsTestFolderRS.builder()
          .id(suiteItem != null ? suiteItem.getItemId() : null)
          .name(suiteTestItem.getName() != null ? suiteTestItem.getName()
              : (suiteItem != null ? suiteItem.getName() : null))
          .description(suiteTestItem.getDescription() != null ? suiteTestItem.getDescription()
              : (suiteItem != null ? suiteItem.getDescription() : null))
          .countOfTestCases(0L)
          .parentFolderId(parentSuiteId)
          .build();
    }).toList());

    return PagedResourcesAssembler.<TmsTestFolderRS>pageConverter()
        .apply(new PageImpl<>(resultList, pageable, suitePage.getTotalElements()));
  }

  @Transactional
  public void deleteByLaunchId(Long launchId) {
    testFolderTestItemRepository.deleteByLaunchId(launchId);
  }
}
