package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.mapper.SuiteTestItemBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TmsTestFolderTestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
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
public class TestFolderItemService {

  private final TestItemRepository testItemRepository;
  private final TmsTestFolderTestItemRepository testFolderTestItemRepository;
  private final TmsTestFolderService tmsTestFolderService;
  private final SuiteTestItemBuilder suiteItemBuilder;

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
    var suiteItem = createSuiteItem(projectId, testFolderId, launch);
    log.info("Created new SUITE item: {} for test folder: {}", suiteItem.getItemId(), testFolderId);
    return suiteItem;
  }

  /**
   * Creates SUITE item and sets parent SUITE relation if the folder has a parent folder.
   */
  private TestItem createSuiteItem(Long projectId, Long testFolderId,
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
    suiteItem = testItemRepository.save(suiteItem);
    log.trace("Persisted SUITE item with ID: {}", suiteItem.getItemId());

    // If folder has a parent, ensure parent SUITE exists and set parent relation
    if (testFolder != null && testFolder.getParentTestFolder() != null) {
      var parentFolderId = testFolder.getParentTestFolder().getId();
      var parentSuite = findTestFolderItem(projectId, parentFolderId,
          launch); // recursion ensures parent exists

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
  public void removeFolderTestItemLink(Long testItemId) {
    log.debug("Removing test folder-test item links for item: {}", testItemId);
    testFolderTestItemRepository.deleteByTestItem_ItemId(testItemId);
    log.trace("Removed links for item: {}", testItemId);
  }

  /**
   * Retrieves a flat list of SUITE test items mapped to TmsTestFolderRS for a given launch. Uses
   * junction metadata (name/description), counts TEST children, and resolves parent SUITE id.
   *
   * @param launchId the Launch ID
   * @param pageable pagination
   * @return page of TmsTestFolderRS
   */
  @Transactional(readOnly = true)
  public Page<TmsTestFolderRS> getSuiteFoldersByLaunch(
      Long projectId,
      Long launchId,
      Pageable pageable) {
    var suitePage = testFolderTestItemRepository
        .findByLaunchId(launchId, pageable)
        .map(suiteTestItem -> {
          var suiteItem = suiteTestItem.getTestItem();
          var testChildrenCount = 0L;
          Long parentSuiteId = null;

          if (suiteItem != null) {
            testChildrenCount = testItemRepository.countByParentIdAndType(suiteItem.getItemId(),
                TestItemTypeEnum.TEST);
            parentSuiteId = suiteItem.getParentId();
          }

          return TmsTestFolderRS.builder()
              .id(suiteItem != null ? suiteItem.getItemId() : null)
              .name(suiteTestItem.getName() != null ? suiteTestItem.getName()
                  : (suiteItem != null ? suiteItem.getName() : null))
              .description(suiteTestItem.getDescription() != null ? suiteTestItem.getDescription()
                  : (suiteItem != null ? suiteItem.getDescription() : null))
              .countOfTestCases(testChildrenCount)
              .parentFolderId(parentSuiteId)
              .build();
        });
    return PagedResourcesAssembler.<TmsTestFolderRS>pageConverter()
        .apply(new PageImpl<>(
            suitePage.getContent(),
            pageable,
            suitePage.getTotalElements()
        ));
  }
}
