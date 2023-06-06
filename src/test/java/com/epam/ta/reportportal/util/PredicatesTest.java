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

package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Andrei Varabyeu
 */
class PredicatesTest {

  @Test
  void checkSpecialCharacters() {
    assertTrue(Predicates.SPECIAL_CHARS_ONLY.test("_"),
        "Incorrect predicate behavior: only spec chars");
    assertFalse(Predicates.SPECIAL_CHARS_ONLY.test("a_"),
        "Incorrect predicate behavior: spec chars after ASCII");
    assertFalse(Predicates.SPECIAL_CHARS_ONLY.test("_a"),
        "Incorrect predicate behavior: spec chars before ASCII");
  }

  @ParameterizedTest
  @ValueSource(strings = {"STEP", "BEFORE_METHOD", "AFTER_METHOD"})
  void checkCanBeIndexed(String type) {
    TestItem testItem = new TestItem();
    testItem.setType(TestItemTypeEnum.fromValue(type).get());
    final TestItemResults itemResults = new TestItemResults();
    final IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIgnoreAnalyzer(false);
    final IssueType issueType = new IssueType();
    issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
    issueEntity.setIssueType(issueType);
    itemResults.setIssue(issueEntity);
    testItem.setItemResults(itemResults);
    assertTrue(ITEM_CAN_BE_INDEXED.test(testItem), "Item should be available for indexing");
  }

  @Test
  void checkTIIndexed() {
    TestItem testItem = new TestItem();
    final TestItemResults itemResults = new TestItemResults();
    testItem.setType(TestItemTypeEnum.STEP);
    final IssueEntity issue = new IssueEntity();
    final IssueType issueType = new IssueType();
    issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.TO_INVESTIGATE));
    issueType.setLocator(TestItemIssueGroup.TO_INVESTIGATE.getLocator());
    issue.setIssueType(issueType);
    itemResults.setIssue(issue);
    testItem.setItemResults(itemResults);
    assertTrue(ITEM_CAN_BE_INDEXED.test(testItem), "Item with TI issue is available for indexing");
  }

  @Test
  void checkIgnoreIndexed() {
    TestItem testItem = new TestItem();
    testItem.setType(TestItemTypeEnum.STEP);
    final TestItemResults itemResults = new TestItemResults();
    final IssueEntity issueEntity = new IssueEntity();
    issueEntity.setIgnoreAnalyzer(true);
    final IssueType issueType = new IssueType();
    issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
    issueEntity.setIssueType(issueType);
    itemResults.setIssue(issueEntity);
    testItem.setItemResults(itemResults);
    assertFalse(ITEM_CAN_BE_INDEXED.test(testItem),
        "Item with ignore flag shouldn't be available for indexing");
  }

  @Test
  void checkLaunchCanBeIndexed() {
    Launch launch = new Launch();
    launch.setMode(LaunchModeEnum.DEFAULT);
    assertTrue(LAUNCH_CAN_BE_INDEXED.test(launch), "Launch should be available for indexing");
  }

  @Test
  void checkDebugLaunchCanBeIndexed() {
    Launch launch = new Launch();
    launch.setMode(LaunchModeEnum.DEFAULT);
    assertTrue(LAUNCH_CAN_BE_INDEXED.test(launch),
        "Launch in debug mode should not be available for indexing");
  }
}