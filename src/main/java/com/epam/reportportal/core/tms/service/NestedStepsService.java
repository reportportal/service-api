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

import com.epam.reportportal.core.tms.dto.TmsStepRS;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.core.tms.mapper.NestedStepItemBuilder;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing nested steps (hasStats=false items) created from manual scenarios.
 * Handles creation of both steps-based and text-based scenario nested items.
 *
 * @author ReportPortal
 */
@Slf4j
@Service
public class NestedStepsService {

  private final TestItemRepository testItemRepository;
  private final NestedStepItemBuilder nestedStepItemBuilder;

  @Autowired
  public NestedStepsService(
      TestItemRepository testItemRepository,
      NestedStepItemBuilder nestedStepItemBuilder) {
    this.testItemRepository = testItemRepository;
    this.nestedStepItemBuilder = nestedStepItemBuilder;
  }

  /**
   * Creates nested steps from a steps-based manual scenario.
   * Each step becomes a separate nested item under the parent TEST item.
   *
   * @param stepsScenario scenario containing list of steps
   * @param parentTestItem parent TEST item
   * @param launch launch entity
   * @return list of created nested step items (persisted)
   */
  @Transactional
  public List<TestItem> createNestedStepsFromStepScenario(
      TmsStepsManualScenarioRS stepsScenario,
      TestItem parentTestItem,
      Launch launch) {

    log.debug("Creating nested steps from step-based scenario for parent item: {}",
        parentTestItem.getItemId());

    var createdSteps = new ArrayList<TestItem>();

    var steps = stepsScenario.getSteps();
    if (steps == null || steps.isEmpty()) {
      log.warn("Step scenario has no steps, skipping nested step creation");
      return createdSteps;
    }

    log.debug("Processing {} steps from scenario", steps.size());

    var stepIndex = 1;
    for (var step : steps) {
      try {
        var nestedStep = createNestedStepFromStep(step, parentTestItem, launch, stepIndex);
        createdSteps.add(nestedStep);
        stepIndex++;
      } catch (Exception e) {
        log.error("Error creating nested step {} for parent item: {}",
            stepIndex, parentTestItem.getItemId(), e);
        throw e;  // Re-throw to stop transaction
      }
    }

    log.info("Successfully created {} nested steps for test item: {}",
        createdSteps.size(), parentTestItem.getItemId());

    return createdSteps;
  }

  /**
   * Creates a nested step from a single step entity.
   * Builds appropriate name and description from step data.
   *
   * @param step step entity
   * @param parentTestItem parent TEST item
   * @param launch launch entity
   * @param stepIndex step index (1-based) for naming
   * @return created nested step item (persisted)
   */
  @Transactional
  private TestItem createNestedStepFromStep(
      TmsStepRS step,
      TestItem parentTestItem,
      Launch launch,
      int stepIndex) {

    log.debug("Creating nested step {} from step data", stepIndex);

    // Build step name: "Step N: {instructions}"
    var stepName = nestedStepItemBuilder.buildStepName(step.getInstructions(), stepIndex);

    // Description is the expected result
    var description = step.getExpectedResult();

    // Build nested step item
    var nestedStep = nestedStepItemBuilder.buildNestedStepItem(
        parentTestItem, stepName, description, launch
    );

    // Persist nested step
    nestedStep = testItemRepository.save(nestedStep);

    // Set path after persistence (when itemId is generated)
    nestedStep.setPath(parentTestItem.getPath() + "." + nestedStep.getItemId());
    nestedStep = testItemRepository.save(nestedStep);

    log.info("Created nested step item: {} with name: {}", nestedStep.getItemId(), stepName);

    return nestedStep;
  }

  /**
   * Creates a nested step from a text-based manual scenario.
   * Creates a single nested item representing the entire scenario.
   *
   * @param textScenario scenario with text instructions and expected result
   * @param parentTestItem parent TEST item
   * @param launch launch entity
   * @return created nested step item (persisted)
   */
  @Transactional
  public TestItem createNestedStepFromTextScenario(
      TmsTextManualScenarioRS textScenario,
      TestItem parentTestItem,
      Launch launch) {

    log.debug("Creating nested step from text-based scenario for parent item: {}",
        parentTestItem.getItemId());

    // Build description combining instructions and expected result
    var description = nestedStepItemBuilder.buildTextScenarioDescription(
        textScenario.getInstructions(), textScenario.getExpectedResult()
    );

    // Use instructions as the name
    var name = textScenario.getInstructions();

    // Build nested step item
    var nestedStep = nestedStepItemBuilder.buildNestedStepItem(
        parentTestItem, name, description, launch
    );

    // Persist nested step
    nestedStep = testItemRepository.save(nestedStep);

    // Set path after persistence (when itemId is generated)
    nestedStep.setPath(parentTestItem.getPath() + "." + nestedStep.getItemId());
    nestedStep = testItemRepository.save(nestedStep);

    log.info("Created nested step item: {} from text scenario", nestedStep.getItemId());

    return nestedStep;
  }
}
