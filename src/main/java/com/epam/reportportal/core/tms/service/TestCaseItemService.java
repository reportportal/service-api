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

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.mapper.TestCaseItemBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  /**
   * Creates a TEST item (test case execution) under a SUITE item.
   * Includes attributes from the test case.
   *
   * @param testCase test case data
   * @param suiteItem parent SUITE item
   * @param launch launch entity
   * @return created TEST item (persisted)
   */
  @Transactional
  public TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch) {

    log.debug("Creating TEST item for test case: {} under SUITE item: {}",
        testCase.getName(), suiteItem.getItemId());

    // Build TEST item
    var testItem = testCaseItemBuilder.buildTestCaseItem(
        testCase, suiteItem, launch
    );

    // Persist TEST item
    testItem = testItemRepository.save(testItem);

    log.trace("Persisted TEST item with ID: {}", testItem.getItemId());

    var testResults = new TestItemResults();
    testResults.setStatus(StatusEnum.TO_RUN);

    testResults.setTestItem(testItem);
    testItem.setItemResults(testResults);
    testItem.setPath(suiteItem.getPath() + "." + testItem.getItemId());

    log.info("Successfully created TEST item: {} for test case: {}",
        testItem.getItemId(), testCase.getName());

    return testItemRepository.save(testItem);
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
