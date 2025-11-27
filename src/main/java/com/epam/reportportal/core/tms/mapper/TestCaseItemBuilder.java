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

package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builder for creating TEST test items (test case executions in manual launches).
 * Encapsulates TEST item creation logic with proper initialization.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
public class TestCaseItemBuilder {

  /**
   * Creates a TEST test item (test case execution) with given parameters.
   * TEST items are direct children of SUITE items and represent executable test cases.
   *
   * @param name test case name
   * @param description test case description
   * @param parentSuiteItem parent SUITE item (test folder container)
   * @param launch launch entity
   * @return created TEST test item (not yet persisted)
   */
  public TestItem buildTestCaseItem(
      String name,
      String description,
      TestItem parentSuiteItem,
      Launch launch) {

    log.debug("Building TEST item (test case) with name: {}", name);

    var testItem = new TestItem();
    testItem.setUuid(UUID.randomUUID().toString());
    testItem.setName(name);
    testItem.setDescription(description);
    testItem.setType(TestItemTypeEnum.TEST);
    testItem.setStartTime(Instant.now());
    testItem.setLaunchId(launch.getId());
    testItem.setHasStats(true);  // TEST contributes to statistics
    testItem.setHasChildren(false);  // Will be set to true when nested steps added
    testItem.setRetryOf(null);
    testItem.setParentId(parentSuiteItem.getItemId());
    testItem.setPath(parentSuiteItem.getPath() + ".");  // Will be completed with itemId after persist

    // Create test item results with TO_RUN status
    var testResults = new TestItemResults();
    testResults.setStatus(StatusEnum.TO_RUN);
    testResults.setEndTime(null);
    testResults.setDuration(null);

    testItem.setItemResults(testResults);
    testResults.setTestItem(testItem);

    log.trace("Successfully built TEST item: {}", name);
    return testItem;
  }
}
