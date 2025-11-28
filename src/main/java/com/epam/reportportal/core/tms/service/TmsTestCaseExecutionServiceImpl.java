package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.item.TestItemService;
import com.epam.reportportal.core.tms.dto.NestedStepResult;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioRS;
import com.epam.reportportal.core.tms.dto.TmsStepRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.mapper.TmsManualScenarioMapper;
import com.epam.reportportal.core.tms.mapper.TmsTestCaseExecutionMapper;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsTestCaseExecutionFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.ws.converter.PagedResourcesAssembler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing TMS Test Case Executions. Handles both execution queries and
 * creation for manual launches.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsTestCaseExecutionServiceImpl implements TmsTestCaseExecutionService {

  private static final String TEST_CASE_EXECUTION_IN_LAUNCH =
      "Test Case execution: %d for Launch: %d";

  private final TmsTestCaseExecutionRepository tmsTestCaseExecutionRepository;
  private final TmsTestCaseExecutionFilterableRepository tmsTestCaseExecutionFilterableRepository;
  private final TmsTestCaseVersionService tmsTestCaseVersionService;
  private final TestItemService testItemService;
  private final TmsTestCaseExecutionCommentService tmsTestCaseExecutionCommentService;
  private final TmsTestCaseExecutionMapper tmsTestCaseExecutionMapper;
  private final TmsTestPlanService tmsTestPlanService;
  private final TestItemRepository testItemRepository;
  private final TestFolderItemService testFolderItemService;
  private final TestCaseItemService testCaseItemService;
  private final NestedStepsService nestedStepsService;
  private final TmsStepExecutionService tmsStepExecutionService;
  private final TmsManualScenarioMapper tmsManualScenarioMapper;

  private TmsTestCaseService tmsTestCaseService;
  private TmsManualLaunchService tmsManualLaunchService;

  @Autowired
  public void setTmsTestCaseService(
      TmsTestCaseService tmsTestCaseService) {
    this.tmsTestCaseService = tmsTestCaseService;
  }

  @Autowired
  public void setTmsManualLaunchService(
      TmsManualLaunchService tmsManualLaunchService) {
    this.tmsManualLaunchService = tmsManualLaunchService;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestCaseExecution> getLastTestCasesExecutionsByTestCaseIds(
      List<Long> testCaseIds) {
    return Optional
        .ofNullable(tmsTestCaseExecutionRepository.findLastExecutionsByTestCaseIds(testCaseIds))
        .orElse(Collections.emptyList())
        .stream()
        .collect(Collectors.toMap(
            TmsTestCaseExecution::getTestCaseId, Function.identity()
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseExecution getLastTestCaseExecution(Long testCaseId) {
    return tmsTestCaseExecutionRepository
        .findLastExecutionByTestCaseId(testCaseId)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestCaseExecution> findLastExecutionsByTestCaseIdsAndTestPlanId(
      List<Long> testCaseIds, Long testPlanId) {

    if (testCaseIds == null || testCaseIds.isEmpty()) {
      return Map.of();
    }

    return tmsTestCaseExecutionRepository
        .findLastExecutionsByTestCaseIdsAndTestPlanId(testCaseIds, testPlanId)
        .stream()
        .collect(Collectors.toMap(
            TmsTestCaseExecution::getTestCaseId,
            Function.identity(),
            (existing, replacement) -> existing // keep first in case of duplicates
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseExecution> findByTestCaseIdAndTestPlanId(Long testCaseId,
      Long testPlanId) {

    if (testCaseId == null || testPlanId == null) {
      return List.of();
    }

    return tmsTestCaseExecutionRepository.findByTestCaseIdAndTestPlanId(testCaseId, testPlanId);
  }

  /**
   * Creates a complete test case execution with hierarchical structure. Orchestrates: 1. Find or
   * create SUITE item (test folder container) 2. Create TEST item (test case) under SUITE 3. Create
   * nested steps from a manual scenario (if exists) 4. Create TmsTestCaseExecution record 5. Create
   * TmsStepExecution records for nested steps
   *
   * @param projectId project ID
   * @param testCase  test case entity
   * @param launch    launch entity
   */
  @Transactional
  @Override
  public void createExecution(long projectId, TmsTestCaseRS testCase, Launch launch) {
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
    var testFolderId = testCase.getTestFolder().getId();

    var testFolderItem = testFolderItemService.findTestFolderItem(projectId, testFolderId, launch);
    testFolderItemService.markAsHavingChildren(testFolderItem);
    log.debug("SUITE item resolved: {}", testFolderItem.getItemId());

    // Step 2: Create TEST item (test case execution) under SUITE
    var testItem = testCaseItemService.createTestCaseItem(
        testCase,
        testFolderItem,
        launch
    );
    log.debug("TEST item created: {}", testItem.getItemId());

    // Step 3: Create nested steps from a manual scenario (if exists)
    var nestedSteps = List.<TestItem>of();
    var tmsStepIds = List.<Long>of();

    if (testCase.getManualScenario() != null && tmsManualScenarioMapper.isValidScenario(
        testCase.getManualScenario())) {
      var scenarioResult = createNestedStepsFromScenario(
          testCase.getManualScenario(), testItem, launch
      );
      nestedSteps = scenarioResult.getNestedSteps();
      tmsStepIds = scenarioResult.getTmsStepIds();

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
      tmsStepExecutionService.createTmsStepExecutions(
          execution.getId(), nestedSteps, launch, tmsStepIds
      );
      log.trace("Step execution records created: {}", nestedSteps.size());
    }

    log.info("Successfully created execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());
  }

  /**
   * Creates nested steps from a manual scenario. Handles both steps-based and text-based
   * scenarios.
   *
   * @param scenario       manual scenario
   * @param parentTestItem parent TEST item
   * @param launch         launch entity
   * @return result object containing nested steps and their TMS step IDs
   */
  @Transactional
  public NestedStepResult createNestedStepsFromScenario(
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
      var tmsStepIds = stepsScenario
          .getSteps()
          .stream()
          .map(TmsStepRS::getId)
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

  /**
   * Internal method to create executions for multiple test cases. Handles batch creation with error
   * handling and transaction management.
   *
   * @param projectId   project ID
   * @param testCaseIds list of test case IDs to create executions for
   * @param launch      launch entity
   */
  @Override
  @Transactional
  public void createExecutions(long projectId, List<Long> testCaseIds, Launch launch) {
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
            "Failed to create execution for test case: " + testCaseId + ", exception: "
                + e.getMessage());
      }
    }
  }

  @Override
  @Transactional
  public void addTestCasesToLaunch(long projectId, Launch launch, List<Long> testCaseIds) {
    log.debug("Adding {} test cases to launch: {}", testCaseIds.size(), launch.getId());

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("No test cases to add to launch: {}", launch.getId());
      return;
    }

    createExecutions(projectId, testCaseIds, launch);
    log.info("Successfully added {} test cases to launch: {}", testCaseIds.size(),
        launch.getId());
  }

  @Transactional
  @Override
  public void deleteTestCaseExecutionFromLaunch(long projectId, Long launchId,
      Long executionId) {
    log.debug("Removing test case execution: {} from launch: {}", executionId, launchId);

    var execution = tmsTestCaseExecutionRepository.findById(executionId)
        .orElseThrow(
            () -> new ReportPortalException(
                "Test case execution not found: " + executionId)
        );

    var testItem = execution.getTestItem();
    var suiteItemId = testItem.getParentId();
    var testItemId = testItem.getItemId();

    // Delete associated step executions
    tmsStepExecutionService.deleteStepExecutionsByTestCaseExecution(executionId);
    log.debug("Deleted step execution records for test case execution: {}", executionId);

    log.debug("Deleted TEST item: {} and its nested steps", testItemId);

    // Delete the execution record
    tmsTestCaseExecutionRepository.deleteById(executionId);
    log.debug("Deleted test case execution: {}", executionId);

    // Check if SUITE item has any remaining TEST children
    if (suiteItemId != null) {
      var testChildrenCount = testItemRepository.countByParentIdAndType(
          suiteItemId, TestItemTypeEnum.TEST);

      if (testChildrenCount == 0) {
        log.debug("SUITE item: {} has no more TEST children, deleting it", suiteItemId);
        // Clean up folder-item link
        testFolderItemService.deleteTestFolderTestItemByTestItemId(suiteItemId);

        testItemRepository.deleteById(suiteItemId);
      } else {
        log.debug("SUITE item: {} still has {} TEST children", suiteItemId, testChildrenCount);
      }
    }

    // Delete the TEST item and all its nested steps (cascade)
    testItemRepository.deleteById(testItemId);

    log.info("Successfully removed test case execution: {} from launch: {}", executionId,
        launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isTestCaseInLaunch(Long testCaseId, Long launchId) {
    return tmsTestCaseExecutionRepository.existsByTestCaseIdAndLaunchId(testCaseId, launchId);
  }

  @Override
  @Transactional
  public void deleteByLaunchId(Long launchId) {
    log.debug("Deleting all executions for launch: {}", launchId);
    // Delete executions
    tmsTestCaseExecutionRepository.deleteByLaunchId(launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public Long countTestCasesInLaunch(Long launchId) {
    return tmsTestCaseExecutionRepository.countByLaunchId(launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByTestItemId(Long testItemId) {
    return tmsTestCaseExecutionRepository.existsByTestItemId(testItemId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseExecution> findByLaunchId(Long launchId) {
    log.debug("Finding executions by launch ID: {}", launchId);
    return tmsTestCaseExecutionRepository.findByLaunchId(launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TmsTestCaseExecution> findByLaunchIdWithDetails(Long launchId) {
    log.debug("Finding executions with details by launch ID: {}", launchId);
    return tmsTestCaseExecutionRepository.findByLaunchIdWithDetails(launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TmsTestCaseExecution> findByTestCaseExecutionIdAndLaunchId(
      Long testCaseExecutionId,
      Long launchId) {
    log.debug("Finding execution by test case ID: {} and launch ID: {}", testCaseExecutionId,
        launchId);
    return tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(testCaseExecutionId,
        launchId);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestCaseExecution> findExecutionsByLaunchIdAsMap(Long launchId) {
    log.debug("Finding executions by launch ID as map: {}", launchId);

    return tmsTestCaseExecutionRepository.findByLaunchId(launchId)
        .stream()
        .collect(Collectors.toMap(
            TmsTestCaseExecution::getTestCaseId,
            Function.identity(),
            (existing, replacement) -> existing // keep first in case of duplicates
        ));
  }

  @Override
  public Page<TmsTestCaseExecutionRS> findByLaunchIdWithFilter(Long launchId, Filter filter,
      Pageable pageable) {
    log.debug("Finding test case executions for launch: {} with filter", launchId);

    // Get executions with filtering and pagination using filterable repository
    var executionsPage = tmsTestCaseExecutionFilterableRepository
        .findByLaunchIdWithFilter(launchId, filter, pageable);

    // Convert to RS using mapper
    return PagedResourcesAssembler
        .<TmsTestCaseExecutionRS>pageConverter()
        .apply(tmsTestCaseExecutionMapper.convertToPageTmsTestCaseExecutionRS(executionsPage,
            pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseExecutionRS findByIdAndLaunchIdWithDetails(Long executionId, Long launchId) {
    log.debug("Finding test case execution: {} for launch: {}", executionId, launchId);

    // Find execution and verify it belongs to launch
    var execution = findByTestCaseExecutionIdAndLaunchId(executionId, launchId)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.NOT_FOUND,
            TEST_CASE_EXECUTION_IN_LAUNCH.formatted(executionId, launchId)
        ));

    return tmsTestCaseExecutionMapper.convert(execution);
  }

  @Override
  public Page<TmsTestCaseExecutionRS> findByTestCaseIdAndLaunchId(
      Long testCaseId,
      Long launchId,
      Pageable pageable) {
    log.debug("Finding executions for test case: {} in launch: {}", testCaseId, launchId);

    var executions =
        tmsTestCaseExecutionRepository.findByTestCaseIdAndLaunchId(testCaseId, launchId, pageable);

    return PagedResourcesAssembler
        .<TmsTestCaseExecutionRS>pageConverter()
        .apply(
            tmsTestCaseExecutionMapper.convertToPageTmsTestCaseExecutionRS(executions, pageable));
  }

  @Override
  @Transactional(readOnly = true)
  public TmsTestCaseExecutionRS patch(Long executionId, Long launchId,
      TmsTestCaseExecutionRQ request) {
    log.debug("Updating test case execution: {} in launch: {}", executionId,
        launchId); //TODO check that anf think how to fix
    return findByTestCaseExecutionIdAndLaunchId(executionId, launchId)
        .map(execution -> {
          // Update execution fields from request
          var updated = false;

          if (request.getStatus() != null) {
            // Update test item status
            var testItem = execution.getTestItem();
            if (testItem != null && testItem.getItemResults() != null) {
              execution.setTestItem(
                  testItemService.patchTestItemStatus(testItem, request.getStatus().toUpperCase()));
              // Add the test case to test plan for PASSED or FAILED status
              addTestCaseToTestPlan(execution, request.getStatus());
              updated = true;
            }
          }

          if (updated) {
            execution = tmsTestCaseExecutionRepository.save(execution);
            log.info("Test case execution: {} updated in launch: {}", executionId, launchId);
          }

          return tmsTestCaseExecutionMapper.convert(execution);
        })
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.NOT_FOUND,
            TEST_CASE_EXECUTION_IN_LAUNCH.formatted(executionId, launchId)
        ));
  }

  /**
   * Adds a test case to test plan if test item status is PASSED or FAILED.
   *
   * @param execution the test case execution
   * @param status    the new status
   */
  private void addTestCaseToTestPlan(TmsTestCaseExecution execution, String status) {
    if (status == null) {
      return;
    }
    var statusEnum = StatusEnum.valueOf(status.toUpperCase());

    // Check if the status is PASSED or FAILED
    if (statusEnum == StatusEnum.PASSED || statusEnum == StatusEnum.FAILED) {
      tmsManualLaunchService
          .getTestPlanIdByLaunchId(execution.getLaunchId())
          .ifPresentOrElse(
              testPlanId -> {
                var testCaseId = execution.getTestCaseId();

                var added = tmsTestPlanService.addTestCaseToTestPlan(testPlanId, testCaseId);

                if (added) {
                  log.info("Added test case: {} to test plan: {} due to {} status",
                      testCaseId, testPlanId, status);
                } else {
                  log.debug("Test case: {} already exists in test plan: {} or failed to add",
                      testCaseId, testPlanId);
                }
              },
              () -> log.warn("No test plan ID found for launch: {}, skipping test case addition",
                  execution.getLaunchId())
          );
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByTestCaseExecutionIdAndLaunchId(Long executionId, Long launchId) {
    return tmsTestCaseExecutionRepository.existsByTestCaseExecutionIdAndLaunchId(executionId,
        launchId);
  }

  @Override
  @Transactional
  public TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(Long projectId, Long launchId,
      Long executionId, TmsTestCaseExecutionCommentRQ request) {
    var execution = findByTestCaseExecutionIdAndLaunchId(executionId, launchId)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.NOT_FOUND,
            TEST_CASE_EXECUTION_IN_LAUNCH.formatted(executionId, launchId)
        ));

    return tmsTestCaseExecutionCommentService.putTestCaseExecutionComment(execution, request);
  }

  @Override
  @Transactional
  public void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId) {
    tmsTestCaseExecutionCommentService.deleteTestCaseExecutionComment(
        projectId, launchId, executionId
    );
  }
}
