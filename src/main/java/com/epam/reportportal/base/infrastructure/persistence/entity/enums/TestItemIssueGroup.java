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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vocabulary for supported test item issues types. They are applied as markers for test steps. User marks test step
 * according to the type of the issue, caused it's failure.<br> locator - predefined ID for immutable issue type<br>
 *
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
public enum TestItemIssueGroup /*implements StatisticsAwareness*/ {

  NOT_ISSUE_FLAG("NOT_ISSUE", "notIssue", ""),

  PRODUCT_BUG("PRODUCT_BUG", "productBug", "pb001"),
  AUTOMATION_BUG("AUTOMATION_BUG", "automationBug", "ab001"),
  SYSTEM_ISSUE("SYSTEM_ISSUE", "systemIssue", "si001"),
  TO_INVESTIGATE("TO_INVESTIGATE", "toInvestigate", "ti001"),
  NO_DEFECT("NO_DEFECT", "noDefect", "nd001");

  private final String value;

  private final String issueCounterField;

  private final String locator;

  TestItemIssueGroup(String value, String executionCounterField, String locator) {
    this.value = value;
    this.issueCounterField = executionCounterField;
    this.locator = locator;
  }

  /**
   * Retrieves TestItemIssueType value by it's string value
   *
   * @param value - string representation of desired TestItemIssueType value
   * @return TestItemIssueType value
   */
  public static Optional<TestItemIssueGroup> fromValue(String value) {
    return Arrays.stream(TestItemIssueGroup.values())
        .filter(type -> type.getValue().equalsIgnoreCase(value)).findAny();
  }

  public static TestItemIssueGroup validate(String value) {
    return Arrays.stream(TestItemIssueGroup.values())
        .filter(type -> type.getValue().replace(" ", "_").equalsIgnoreCase(value))
        .findAny()
        .orElse(null);
  }

  public static List<String> validValues() {
    return Arrays.stream(TestItemIssueGroup.values()).map(TestItemIssueGroup::getValue)
        .collect(Collectors.toList());
  }

  public String getValue() {
    return value;
  }

  public String getLocator() {
    return locator;
  }

  public String getIssueCounterField() {
    return issueCounterField;
  }
}
