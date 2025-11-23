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
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builder for creating SUITE test items (test folder containers in launches).
 * Encapsulates SUITE item creation logic with proper initialization.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
public class SuiteTestItemBuilder {

  /**
   * Creates a SUITE test item (test folder container) with given metadata.
   * SUITE items are root-level containers for test cases in manual launches.
   *
   * @param testFolder TMS test folder metadata
   * @param launch launch entity
   * @param testFolderId ID of the test folder
   * @return created SUITE test item (not yet persisted)
   */
  public TestItem buildSuiteItem(
      TmsTestFolder testFolder,
      Launch launch,
      Long testFolderId) {

    log.debug("Building SUITE item for test folder: {} in launch: {}", testFolderId,
        launch.getId());

    var suiteItem = new TestItem();
    suiteItem.setUuid(UUID.randomUUID().toString());
    suiteItem.setName(testFolder != null ? testFolder.getName()
        : "Test Folder " + testFolderId);
    suiteItem.setDescription(testFolder != null ? testFolder.getDescription() : null);
    suiteItem.setType(TestItemTypeEnum.SUITE);
    suiteItem.setStartTime(Instant.now());
    suiteItem.setLaunchId(launch.getId());
    suiteItem.setHasStats(true);  // SUITE contributes to statistics
    suiteItem.setHasChildren(false);  // Will be set to true when first TEST child added
    suiteItem.setRetryOf(null);
    suiteItem.setPath(String.valueOf(launch.getId())); //TODO check if that is required

    // Create test item results with INFO status
    var suiteResults = new TestItemResults();
    suiteResults.setStatus(StatusEnum.INFO);
    suiteResults.setEndTime(null);
    suiteResults.setDuration(null);

    suiteItem.setItemResults(suiteResults);
    suiteResults.setTestItem(suiteItem);

    log.trace("Successfully built SUITE item for test folder: {}", testFolderId);
    return suiteItem;
  }
}
