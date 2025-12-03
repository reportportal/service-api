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
 * Builder for creating nested step test items from manual scenario data.
 * Encapsulates nested step creation logic with proper initialization.
 *
 * @author ReportPortal
 */
@Slf4j
@Component
public class NestedStepItemBuilder {

  /**
   * Creates a nested step test item with given parameters.
   * Nested steps have hasStats=false and are not counted in statistics.
   *
   * @param parentTestItem parent TEST item
   * @param name nested step name/title
   * @param description nested step description
   * @param launch launch entity
   * @return created nested step test item (not yet persisted)
   */
  public TestItem buildNestedStepItem(
      TestItem parentTestItem,
      String name,
      String description,
      Launch launch) {

    log.debug("Building nested step item with name: {}", name);

    var nestedStep = new TestItem();
    nestedStep.setUuid(UUID.randomUUID().toString());
    nestedStep.setName(name);
    nestedStep.setDescription(description);
    nestedStep.setType(TestItemTypeEnum.STEP);
    nestedStep.setStartTime(Instant.now());
    nestedStep.setLaunchId(launch.getId());
    nestedStep.setHasStats(false);  // Nested step - does NOT contribute to statistics
    nestedStep.setHasChildren(false);
    nestedStep.setRetryOf(null);
    nestedStep.setParentId(parentTestItem.getItemId());
    nestedStep.setTestCaseId(parentTestItem.getTestCaseId());

    // Create test item results with INFO status
    var nestedResults = new TestItemResults();
    nestedResults.setStatus(StatusEnum.INFO);
    nestedResults.setEndTime(null);
    nestedResults.setDuration(null);

    nestedStep.setItemResults(nestedResults);
    nestedResults.setTestItem(nestedStep);

    nestedStep.setTestCaseHash(parentTestItem.getTestCaseHash()); //TODO check is it correct

    log.trace("Successfully built nested step item: {}", name);
    return nestedStep;
  }

  /**
   * Builds nested step name from instructions and step index.
   * Format: "Step N: {instructions}"
   *
   * @param instructions step instructions
   * @param stepIndex step index (1-based)
   * @return formatted step name
   */
  public String buildStepName(String instructions, int stepIndex) {
    return String.format("Step %d: %s", stepIndex, instructions);
  }

  /**
   * Builds nested step description from text and text scenario data.
   * Combines instructions and expected result with formatting.
   *
   * Format for text scenario: "{instructions}\n\nExpected result: {expectedResult}"
   *
   * @param instructions scenario instructions
   * @param expectedResult scenario expected result
   * @return combined description
   */
  public String buildTextScenarioDescription(String instructions, String expectedResult) {
    var description = new StringBuilder();

    if (instructions != null && !instructions.isEmpty()) {
      description.append(instructions);
    }

    if (expectedResult != null && !expectedResult.isEmpty()) {
      if (description.length() > 0) {
        description.append("\n\nExpected result: ");
      } else {
        description.append("Expected result: ");
      }
      description.append(expectedResult);
    }

    return description.toString();
  }
}
