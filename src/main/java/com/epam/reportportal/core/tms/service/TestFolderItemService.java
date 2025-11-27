/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.mapper.SuiteItemBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TmsTestFolderTestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing SUITE test items (test folder containers in manual launches).
 * Handles creation, retrieval, and linking of SUITE items to test folders.
 *
 * @author ReportPortal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestFolderItemService {

  private final TestItemRepository testItemRepository;
  private final TmsTestFolderTestItemRepository testFolderTestItemRepository;
  private final TmsTestFolderService tmsTestFolderService;
  private final SuiteItemBuilder suiteItemBuilder;

  /**
   * Finds or creates a SUITE item for a test folder in a launch.
   * If SUITE item already exists for this folder in this launch, returns existing one.
   * Otherwise, creates new SUITE item and links it to the test folder.
   *
   * @param projectId project ID
   * @param testFolderId test folder ID
   * @param launch launch entity
   * @return SUITE test item (persisted)
   */
  @Transactional
  public TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch) {
    log.debug("Finding or creating SUITE item for test folder: {} in launch: {}",
        testFolderId, launch.getId());

    // Try to find existing SUITE item
    var existingSuite = testItemRepository.findSuiteItemInLaunchForFolder(
        launch.getId(), testFolderId
    );

    if (existingSuite.isPresent()) {
      log.debug("Found existing SUITE item: {} for test folder: {}",
          existingSuite.get().getItemId(), testFolderId);
      return existingSuite.get();
    }

    // Create new SUITE item
    var suiteItem = createSuiteItem(projectId, testFolderId, launch);
    log.info("Created new SUITE item: {} for test folder: {}", suiteItem.getItemId(),
        testFolderId);

    return suiteItem;
  }

  /**
   * Creates a new SUITE item for a test folder.
   * Loads test folder metadata and creates SUITE item with proper initialization.
   * Creates junction record linking SUITE item to test folder.
   *
   * @param projectId project ID
   * @param testFolderId test folder ID
   * @param launch launch entity
   * @return created SUITE test item (persisted)
   */
  private TestItem createSuiteItem(Long projectId, Long testFolderId, Launch launch) {
    log.debug("Creating SUITE item for test folder: {}", testFolderId);

    // Load test folder metadata
    TmsTestFolder testFolder = null;
    try {
      testFolder = tmsTestFolderService.getEntityById(projectId, testFolderId);
      log.trace("Loaded test folder metadata: {}", testFolder.getName());
    } catch (Exception e) {
      log.warn("Failed to load test folder metadata for: {}, using default name", testFolderId);
    }

    // Build SUITE item
    var suiteItem = suiteItemBuilder.buildSuiteItem(testFolder, launch, testFolderId);

    // Persist SUITE item
    suiteItem = testItemRepository.save(suiteItem);
    log.trace("Persisted SUITE item with ID: {}", suiteItem.getItemId());

    // Create junction record linking SUITE item to test folder
    createFolderTestItemLink(testFolder, suiteItem);

    log.info("Successfully created SUITE item: {} for test folder: {}", suiteItem.getItemId(),
        testFolderId);
    return suiteItem;
  }

  /**
   * Creates a junction record linking test folder to SUITE test item.
   *
   * @param testFolder test folder entity
   * @param suiteItem SUITE test item
   */
  private void createFolderTestItemLink(TmsTestFolder testFolder, TestItem suiteItem) {
    log.debug("Creating test folder-test item link for folder: {} and item: {}",
        testFolder.getId(), suiteItem.getItemId());

    // Check if link already exists (safety check)
    if (testFolderTestItemRepository.existsByTestFolderIdAndTestItemId(
        testFolder.getId(), suiteItem.getItemId())) {
      log.debug("Link already exists between folder: {} and item: {}",
          testFolder.getId(), suiteItem.getItemId());
      return;
    }

    // Create and save junction record
    var folderTestItem = TmsTestFolderTestItem.builder()
        .testFolderId(testFolder.getId())
        .testItem(suiteItem)
        .build();

    testFolderTestItemRepository.save(folderTestItem);
    log.trace("Created junction record linking folder: {} to item: {}",
        testFolder.getId(), suiteItem.getItemId());
  }

  /**
   * Marks SUITE item as having stats-aware children.
   *
   * @param testFolderItem SUITE test item
   */
  @Transactional
  public void markAsHavingChildren(TestItem testFolderItem) {
    if (!testFolderItem.isHasChildren()) {
      log.debug("Marking SUITE item: {} as having children", testFolderItem.getItemId());
      testFolderItem.setHasChildren(true);
      testItemRepository.save(testFolderItem);
    }
  }

  /**
   * Removes junction link between test folder and test item.
   *
   * @param testItemId test item ID
   */
  @Transactional
  public void removeFolderTestItemLink(Long testItemId) {
    log.debug("Removing test folder-test item links for item: {}", testItemId);
    testFolderTestItemRepository.deleteByTestItem_ItemId(testItemId);
    log.trace("Removed links for item: {}", testItemId);
  }
}
