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

import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeRS;
import com.epam.reportportal.core.tms.mapper.ItemAttributeMapper;
import com.epam.reportportal.core.tms.mapper.TestCaseItemBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing TEST test items (test case executions in manual launches).
 * Handles creation of TEST items with attributes under SUITE items.
 *
 * @author ReportPortal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseItemService {

  private final TestItemRepository testItemRepository;
  private final TestCaseItemBuilder testCaseItemBuilder;
  private final ItemAttributeMapper itemAttributeMapper;

  /**
   * Creates a TEST item (test case execution) under a SUITE item.
   * Includes attributes from the test case.
   *
   * @param testCaseName test case name
   * @param testCaseDescription test case description
   * @param attributes test case attributes
   * @param suiteItem parent SUITE item
   * @param launch launch entity
   * @return created TEST item (persisted)
   */
  @Transactional
  public TestItem createTestCaseItem(
      String testCaseName,
      String testCaseDescription,
      Set<TmsTestCaseAttributeRS> attributes,
      TestItem suiteItem,
      Launch launch) {

    log.debug("Creating TEST item for test case: {} under SUITE item: {}",
        testCaseName, suiteItem.getItemId());

    // Build TEST item
    var testItem = testCaseItemBuilder.buildTestCaseItem(
        testCaseName, testCaseDescription, suiteItem, launch
    );

    // Persist TEST item
    testItem = testItemRepository.save(testItem);
    log.trace("Persisted TEST item with ID: {}", testItem.getItemId());

    // Complete the path with the generated itemId
    testItem.setPath(suiteItem.getPath() + "." + testItem.getItemId());
    testItem = testItemRepository.save(testItem);

    // Add attributes
    addAttributesToTestItem(testItem, attributes, launch);

    log.info("Successfully created TEST item: {} for test case: {}",
        testItem.getItemId(), testCaseName);

    return testItem;
  }

  /**
   * Adds attributes to a TEST item.
   *
   * @param testItem test item to add attributes to
   * @param attributes test case attributes
   * @param launch launch entity
   */

  private void addAttributesToTestItem(
      TestItem testItem,
      Set<TmsTestCaseAttributeRS> attributes,
      Launch launch) {

    log.debug("Adding attributes to TEST item: {}", testItem.getItemId());

    // Convert TMS attributes to ItemAttribute entities
    var itemAttributes = itemAttributeMapper.mapTestCaseAttributesToItemAttributes(
        attributes, testItem, launch
    );

    if (itemAttributes.isEmpty()) {
      log.debug("No attributes to add to TEST item: {}", testItem.getItemId());
      return;
    }

    // Set attributes on test item
    testItem.setAttributes(itemAttributes);
    testItemRepository.save(testItem);

    log.info("Added {} attributes to TEST item: {}", itemAttributes.size(), testItem.getItemId());
  }

  /**
   * Marks TEST item as having nested children (nested steps).
   *
   * @param testItem TEST item
   */
  @Transactional
  public void markAsHavingNestedChildren(TestItem testItem) {
    if (!testItem.isHasChildren()) {
      log.debug("Marking TEST item: {} as having children", testItem.getItemId());
      testItem.setHasChildren(true);
      testItemRepository.save(testItem);
    }
  }
}
