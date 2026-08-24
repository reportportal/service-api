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

import static com.epam.reportportal.base.reporting.ValidationConstraints.MAX_TEST_ITEM_NAME_LENGTH;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.Parameter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    testItem.setName(StringUtils.substring(tmsTestCaseRS.getName(), 0, MAX_TEST_ITEM_NAME_LENGTH));
    testItem.setDescription(tmsTestCaseRS.getDescription());
    testItem.setType(
        TestItemTypeEnum.STEP); //as per the RPP philosophy test case is the step, but test cases steps are inner steps for this step
    testItem.setStartTime(Instant.now());
    testItem.setLaunchId(launch.getId());
    testItem.setHasStats(true);  // TEST contributes to statistics
    testItem.setHasChildren(false);
    testItem.setRetryOf(null);
    testItem.setParentId(parentSuiteItem.getItemId());
    return testItem;
  }

  public TestItem buildRetryTestCaseItem(TestItem originalItem, StatusEnum newStatus) {
    var retryItem = new TestItem();
    retryItem.setUuid(UUID.randomUUID().toString());
    retryItem.setName(StringUtils.substring(originalItem.getName(), 0, MAX_TEST_ITEM_NAME_LENGTH));
    retryItem.setCodeRef(originalItem.getCodeRef());
    retryItem.setType(originalItem.getType());
    retryItem.setStartTime(Instant.now());
    retryItem.setDescription(originalItem.getDescription());
    retryItem.setLaunchId(originalItem.getLaunchId());
    retryItem.setUniqueId(originalItem.getUniqueId());
    retryItem.setTestCaseId(originalItem.getTestCaseId());
    retryItem.setTestCaseHash(originalItem.getTestCaseHash());

    // Copy attributes
    if (originalItem.getAttributes() != null) {
      var copiedAttributes = new HashSet<ItemAttribute>();
      for (var attr : originalItem.getAttributes()) {
        var newAttr = new ItemAttribute(attr.getKey(), attr.getValue(), attr.isSystem());
        newAttr.setTestItem(retryItem);
        copiedAttributes.add(newAttr);
      }
      retryItem.setAttributes(copiedAttributes);
    }

    // Copy parameters
    if (originalItem.getParameters() != null) {
      var copiedParameters = new HashSet<Parameter>();
      for (var param : originalItem.getParameters()) {
        var p = new Parameter();
        p.setKey(param.getKey());
        p.setValue(param.getValue());
        copiedParameters.add(p);
      }
      retryItem.setParameters(copiedParameters);
    }

    retryItem.setRetryOf(originalItem.getItemId());
    retryItem.setParentId(originalItem.getParentId());

    var results = new TestItemResults();
    results.setStatus(newStatus);
    results.setTestItem(retryItem);
    retryItem.setItemResults(results);

    retryItem.setHasChildren(originalItem.isHasChildren());
    retryItem.setHasRetries(false);
    retryItem.setHasStats(originalItem.isHasStats());

    return retryItem;
  }
}
