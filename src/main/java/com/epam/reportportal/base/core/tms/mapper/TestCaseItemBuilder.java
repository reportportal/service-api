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

package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builder for creating TEST test items (test case executions in manual launches). Encapsulates TEST
 * item creation logic with proper initialization.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestCaseItemBuilder {

  /**
   * Creates a TEST test item (test case execution) with given parameters. TEST items are direct
   * children of SUITE items and represent executable test cases.
   *
   * @param tmsTestCaseRS   test case data
   * @param parentSuiteItem parent SUITE item (test folder container)
   * @param launch          launch entity
   * @return created TEST item (not yet persisted)
   */
  public TestItem buildTestCaseItem(
      TmsTestCaseRS tmsTestCaseRS,
      TestItem parentSuiteItem,
      Launch launch) {

    log.debug("Building TEST item (test case) with name: {}", tmsTestCaseRS.getName());

    var testItem = new TestItem();
    testItem.setUuid(UUID.randomUUID().toString());
    testItem.setTestCaseId(String.valueOf(tmsTestCaseRS.getId()));
    testItem.setName(tmsTestCaseRS.getName());
    testItem.setDescription(tmsTestCaseRS.getDescription());
    testItem.setType(TestItemTypeEnum.TEST);
    testItem.setStartTime(Instant.now());
    testItem.setLaunchId(launch.getId());
    testItem.setHasStats(true);  // TEST contributes to statistics
    testItem.setHasChildren(false);  // Will be set to true when nested steps added
    testItem.setRetryOf(null);
    testItem.setParentId(parentSuiteItem.getItemId());
    return testItem;
  }
}
