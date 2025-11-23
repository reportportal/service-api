package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.item.TestItemService;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.core.tms.mapper.TmsTestCaseExecutionMapper;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.dao.tms.TmsTestCaseExecutionRepository;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsTestCaseExecutionFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
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

  @Override
  @Transactional
  public void createExecution(TmsTestCaseRS testCase, Launch launch) {
    log.debug("Creating execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());

    // Check if execution already exists
    if (tmsTestCaseExecutionRepository.existsByTestCaseIdAndLaunchId(
        testCase.getId(), launch.getId())) {
      log.warn("Execution for test case: {} already exists in launch: {}, skipping",
          testCase.getId(), launch.getId());
      return;
    }

    // Create TestItem through TestItemService
    var testItem = testItemService.createToRunTestItemForTestCase(testCase, launch);

    // Get default version ID through TmsTestCaseVersionService
    var defaultVersionId = tmsTestCaseVersionService
        .findDefaultVersionIdByTestCaseId(testCase.getId())
        .orElse(
            null); //TODO at the moment we don't have test case versions => using default version every time

    tmsTestCaseExecutionRepository.save(
        tmsTestCaseExecutionMapper.createTestCaseExecution(testCase, launch, testItem,
            defaultVersionId)
    );

    log.info("Created execution for test case: {} in launch: {}",
        testCase.getId(), launch.getId());
  }

  @Override
  @Transactional
  public void createExecutions(long projectId, List<Long> testCaseIds, Launch launch) {
    log.debug("Creating executions for {} test cases in launch: {}",
        testCaseIds.size(), launch.getId());

    for (Long testCaseId : testCaseIds) {
      var testCase = tmsTestCaseService.getById(projectId, testCaseId);
      createExecution(testCase, launch); //TODO work with test case version once required
    }

    log.info("Created executions for {} test cases in launch: {}",
        testCaseIds.size(), launch.getId());
  }

  @Override
  @Transactional
  public void addTestCasesToLaunch(long projectId, Launch launch, List<Long> testCaseIds) {
    log.debug("Adding {} test cases to launch: {}", testCaseIds.size(), launch.getId());

    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      log.debug("No test cases to add to launch: {}", launch.getId());
      return;
    }

    createExecutions(projectId, testCaseIds, launch);
    log.info("Successfully added {} test cases to launch: {}", testCaseIds.size(), launch.getId());
  }

  @Override
  @Transactional
  public void removeTestCaseExecutionFromLaunch(Long testCaseExecutionId, Long launchId) {
    log.debug("Removing test case execution {} from launch {}", testCaseExecutionId, launchId);

    // Find execution to get test item
    var execution = tmsTestCaseExecutionRepository.findByTestCaseExecutionIdAndLaunchId(
        testCaseExecutionId,
        launchId);

    if (execution.isPresent()) {
      var testItem = execution.get().getTestItem();

      // Delete execution first
      tmsTestCaseExecutionRepository.deleteByTestCaseIdAndLaunchId(testCaseExecutionId, launchId);

      // Delete test item if exists
      if (testItem != null) {
        testItemService.deleteTestItem(testItem.getItemId());
      }

      log.info("Removed test case execution {} from launch {}", testCaseExecutionId, launchId);
    } else {
      log.warn("Test case execution {} not found in launch {}", testCaseExecutionId, launchId);
    }
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
    log.debug("Updating test case execution: {} in launch: {}", executionId, launchId);
    return findByTestCaseExecutionIdAndLaunchId(executionId, launchId)
        .map(execution -> {
          // Update execution fields from request
          boolean updated = false;

          if (request.getStatus() != null) {
            // Update test item status
            var testItem = execution.getTestItem();
            if (testItem != null && testItem.getItemResults() != null) {
              execution.setTestItem(
                  testItemService.patchTestItemStatus(testItem, request.getStatus()));
              // Add the test case to test plan for PASSED or FAILED status
              addTestCaseToTestPlan(execution, request.getStatus());
              updated = true;
            }
          }

          if (request.getExecutionComment() != null) {
            tmsTestCaseExecutionCommentService.update(execution,
                request.getExecutionComment());
            updated = true;
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
}
