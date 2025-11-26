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

import com.epam.reportportal.common.exception.ReportPortalException;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.reportportal.core.tms.mapper.TmsTestCaseExecutionMapper;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TmsTestCaseExecutionCommentRepository;
import com.epam.reportportal.infrastructure.persistence.dao.TmsTestCaseExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing TMS test case executions in manual launches.
 * Orchestrates creation of hierarchical test item structures (SUITE -> TEST -> nested steps).
 *
 * @author ReportPortal
 */
@Slf4j
@Service
public class TmsTestCaseExecutionServiceImpl implements TmsTestCaseExecutionService {

  private final TmsTestCaseExecutionRepository tmsTestCaseExecutionRepository;
  private final TmsTestCaseService tmsTestCaseService;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TmsTestCaseExecutionMapper tmsTestCaseExecutionMapper;
  private final TmsTestCaseExecutionCommentRepository tmsTestCaseExecutionCommentRepository;
  private final TestItemRepository testItemRepository;
  private final SuiteItemService suiteItemService;
  private final TestCaseItemService testCaseItemService;
  private final NestedStepsService nestedStepsService;
  private final StepExecutionService stepExecutionService;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;

  @Autowired
  public TmsTestCaseExecutionServiceImpl(
      TmsTestCaseExecutionRepository tmsTestCaseExecutionRepository,
      TmsTestCaseService tmsTestCaseService,
      TmsTestCaseVersionService tmsTestCaseVersionService,
      TmsTestCaseExecutionMapper tmsTestCaseExecutionMapper,
      TmsTestCaseExecutionCommentRepository tmsTestCaseExecutionCommentRepository,
      TestItemRepository testItemRepository,
      SuiteItemService suiteItemService,
      TestCaseItemService testCaseItemService,
      NestedStepsService nestedStepsService,
      StepExecutionService stepExecutionService,
      TmsManualScenarioMapper tmsManualScenarioMapper) {
    this.tmsTestCaseExecutionRepository = tmsTestCaseExecutionRepository;
    this.tmsTestCaseService = tmsTestCaseService;
    this.tmsTestCaseVersionService = tmsTestCaseVersionService;
    this.tmsTestCaseExecutionMapper = tmsTestCaseExecutionMapper;
    this.tmsTestCaseExecutionCommentRepository = tmsTestCaseExecutionCommentRepository;
    this.testItemRepository = testItemRepository;
    this.suiteItemService = suiteItemService;
    this.testCaseItemService = testCaseItemService;
    this.nestedStepsService = nestedStepsService;
    this.stepExecutionService = stepExecutionService;
    this.tmsManualScenarioMapper = tmsManualScenarioMapper;
  }

  @Override
  @Transactional
  public void addTestCasesToLaunch(long projectId, Launch launch, List<Long> testCaseIds) {
    log.debug("Adding {} test cases to launch: {}", testCaseIds.size(), launch.getId());

    // FIXED: Changed from isNotEmpty() to isEmpty() - was inverted logic bug
    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("No test cases to add to launch: {}", launch.getId());
      return;
    }

    createExecutions(projectId, testCaseIds, launch);
    log.info("Successfully added {} test cases to launch: {}", testCaseIds.size(),
        launch.getId());
  }

  /**
   * Internal method to create executions for multiple test cases.
   * Handles batch creation with error handling and transaction management.
   *
   * @param projectId project ID
   * @param testCaseIds list of test case IDs to create executions for
   * @param launch launch entity
   */
  @Transactional
  private void createExecutions(long projectId, List<Long> testCaseIds, Launch launch) {
    log.debug("Creating {} executions in batch for launch: {}", testCaseIds.size(),
        launch.getId());

    for (var testCaseId : testCaseIds) {
      try {
        // Load test case details
        var testCase = tmsTestCaseService.getById(projectId, testCaseId);

        // Create execution with orchestration
        createExecution(projectId, testCase, launch);

      } catch (Exception e) {
        log.error("Error creating execution for test case: {} in launch: {}",
            testCaseId, launch.getId(), e);
        throw new ReportPortalException(
            "Failed to create execution for test case: " + testCaseId);
      }
    }
  }

  @Override
  @Transactional
  public void createExecution(TmsTestCaseRS testCase, Launch launch) {
    log.debug("Creating execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());

    // Delegate to the new method with project ID
    createExecution(launch.getProjectId(), testCase, launch);
  }

  /**
   * Creates a complete test case execution with hierarchical structure.
   * Orchestrates:
   * 1. Find or create SUITE item (test folder container)
   * 2. Create TEST item (test case) under SUITE
   * 3. Create nested steps from manual scenario (if exists)
   * 4. Create TmsTestCaseExecution record
   * 5. Create TmsStepExecution records for nested steps
   *
   * @param projectId project ID
   * @param testCase test case entity
   * @param launch launch entity
   */
  @Transactional
  private void createExecution(long projectId, TmsTestCaseRS testCase, Launch launch) {
    log.debug("Creating execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());

    // Check if execution already exists - prevent duplicates
    if (tmsTestCaseExecutionRepository.existsByTestCaseIdAndLaunchId(
        testCase.getId(), launch.getId())) {
      log.warn("Execution for test case: {} already exists in launch: {}, skipping",
          testCase.getId(), launch.getId());
      return;
    }

    // Step 1: Find or create SUITE item for the test folder
    var testFolderId = testCase.getTestFolder() != null ? testCase.getTestFolder().getId() : null;
    TestItem suiteItem = null;

    if (testFolderId != null) {
      suiteItem = suiteItemService.findOrCreateSuiteItem(projectId, testFolderId, launch);
      suiteItemService.markAsHavingChildren(suiteItem);
      log.trace("SUITE item resolved: {}", suiteItem.getItemId());
    }

    // Step 2: Create TEST item (test case execution) under SUITE
    var testItem = testCaseItemService.createTestCaseItem(
        testCase.getName(),
        testCase.getDescription(),
        testCase.getAttributes(),
        suiteItem,
        launch
    );
    log.trace("TEST item created: {}", testItem.getItemId());

    // Step 3: Create nested steps from manual scenario (if exists)
    var nestedSteps = List.<TestItem>of();
    var tmsStepIds = List.<Long>of();

    if (testCase.getManualScenario() != null && tmsManualScenarioMapper.isValidScenario(
        testCase.getManualScenario())) {
      var scenarioResult = createNestedStepsFromScenario(
          testCase.getManualScenario(), testItem, launch
      );
      nestedSteps = scenarioResult.nestedSteps;
      tmsStepIds = scenarioResult.tmsStepIds;

      if (!nestedSteps.isEmpty()) {
        testCaseItemService.markAsHavingNestedChildren(testItem);
        log.trace("Nested steps created: {} for TEST item: {}", nestedSteps.size(),
            testItem.getItemId());
      }
    }

    // Step 4: Create TmsTestCaseExecution record
    var defaultVersionId = tmsTestCaseVersionService
        .findDefaultVersionIdByTestCaseId(testCase.getId())
        .orElse(null);

    var execution = tmsTestCaseExecutionMapper.createTestCaseExecution(
        testCase, launch, testItem, defaultVersionId
    );
    execution = tmsTestCaseExecutionRepository.save(execution);
    log.trace("TmsTestCaseExecution created: {}", execution.getId());

    // Step 5: Create TmsStepExecution records for nested steps
    if (!nestedSteps.isEmpty()) {
      stepExecutionService.createStepExecutionRecords(
          execution.getId(), nestedSteps, launch, tmsStepIds
      );
      log.trace("Step execution records created: {}", nestedSteps.size());
    }

    log.info("Successfully created execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());
  }

  /**
   * Creates nested steps from a manual scenario.
   * Handles both steps-based and text-based scenarios.
   *
   * @param scenario manual scenario
   * @param parentTestItem parent TEST item
   * @param launch launch entity
   * @return result object containing nested steps and their TMS step IDs
   */
  @Transactional
  private NestedStepResult createNestedStepsFromScenario(
      TmsManualScenarioRS scenario,
      TestItem parentTestItem,
      Launch launch) {

    log.debug("Creating nested steps from scenario for test item: {}",
        parentTestItem.getItemId());

    if (tmsManualScenarioMapper.isStepsBasedScenario(scenario)) {
      var stepsScenario = tmsManualScenarioMapper.asStepsScenario(scenario);
      var nestedSteps = nestedStepsService.createNestedStepsFromStepScenario(
          stepsScenario, parentTestItem, launch
      );

      // Extract TMS step IDs for correlation
      var tmsStepIds = stepsScenario.getSteps().stream()
          .map(step -> step.getId())
          .toList();

      return new NestedStepResult(nestedSteps, tmsStepIds);

    } else if (tmsManualScenarioMapper.isTextBasedScenario(scenario)) {
      var textScenario = tmsManualScenarioMapper.asTextScenario(scenario);
      var nestedStep = nestedStepsService.createNestedStepFromTextScenario(
          textScenario, parentTestItem, launch
      );

      return new NestedStepResult(List.of(nestedStep), List.of());
    }

    log.warn("Unknown scenario type, no nested steps created");
    return new NestedStepResult(List.of(), List.of());
  }

  @Override
  @Transactional
  public void removeTestCaseExecutionFromLaunch(long projectId, Long launchId,
      Long executionId) {
    log.debug("Removing test case execution: {} from launch: {}", executionId, launchId);

    var execution = tmsTestCaseExecutionRepository.findById(executionId)
        .orElseThrow(
            () -> new ReportPortalException(
                "Test case execution not found: " + executionId)
        );

    var testItem = execution.getTestItem();
    Long suiteItemId = null;

    // Get SUITE item ID before deletion (for cleanup check)
    if (testItem.getParentId() != null) {
      var parentItem = testItemRepository.findById(testItem.getParentId()).orElse(null);
      if (parentItem != null && TestItemTypeEnum.SUITE == parentItem.getType()) {
        suiteItemId = parentItem.getItemId();
      }
    }

    // Delete associated step executions
    stepExecutionService.deleteStepExecutionsByTestCaseExecution(executionId);
    log.debug("Deleted step execution records for test case execution: {}", executionId);

    // Delete the TEST item and all its nested steps (cascade)
    testItemRepository.delete(testItem);
    log.debug("Deleted TEST item: {} and its nested steps", testItem.getItemId());

    // Delete the execution record
    tmsTestCaseExecutionRepository.delete(execution);
    log.debug("Deleted test case execution: {}", executionId);

    // Check if SUITE item has any remaining TEST children
    if (suiteItemId != null) {
      var suiteItem = testItemRepository.findById(suiteItemId).orElse(null);
      if (suiteItem != null) {
        var testChildrenCount = testItemRepository.countByParentIdAndType(
            suiteItemId, TestItemTypeEnum.TEST);

        if (testChildrenCount == 0) {
          log.debug("SUITE item: {} has no more TEST children, deleting it", suiteItemId);
          testItemRepository.delete(suiteItem);

          // Clean up folder-item link
          suiteItemService.removeFolderTestItemLink(suiteItemId);
        } else {
          log.debug("SUITE item: {} still has {} TEST children", suiteItemId, testChildrenCount);
        }
      }
    }

    log.info("Successfully removed test case execution: {} from launch: {}", executionId,
        launchId);
  }

  @Override
  public boolean isTestCaseInLaunch(Long testCaseId, Long launchId) {
    return tmsTestCaseExecutionRepository.existsByTestCaseIdAndLaunchId(testCaseId, launchId);
  }

  /**
   * Internal result class for nested step creation.
   * Holds both the created nested step items and their corresponding TMS step IDs.
   */
  private static class NestedStepResult {
    final List<TestItem> nestedSteps;
    final List<Long> tmsStepIds;

    NestedStepResult(List<TestItem> nestedSteps, List<Long> tmsStepIds) {
      this.nestedSteps = nestedSteps;
      this.tmsStepIds = tmsStepIds;
    }
  }
}
