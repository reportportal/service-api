package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.model.Page;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface TmsTestCaseExecutionService {

  Map<Long, TmsTestCaseExecution> getLastTestCasesExecutionsByTestCaseIds(List<Long> testCaseIds);

  TmsTestCaseExecution getLastTestCaseExecution(Long testCaseId);

  /**
   * Finds last executions for multiple test cases within a specific test plan. Returns a map where
   * key is test case ID and value is the last execution.
   *
   * @param testCaseIds list of test case IDs
   * @param testPlanId  the test plan ID
   * @return map of test case ID to last execution
   */
  Map<Long, TmsTestCaseExecution> findLastExecutionsByTestCaseIdsAndTestPlanId(
      List<Long> testCaseIds, Long testPlanId);

  /**
   * Finds all executions for a specific test case within a test plan. Results are ordered by
   * test_item.start_time DESC (latest first).
   *
   * @param testCaseId the test case ID
   * @param testPlanId the test plan ID
   * @return list of executions ordered by start time descending
   */
  List<TmsTestCaseExecution> findByTestCaseIdAndTestPlanId(Long testCaseId, Long testPlanId);

  /**
   * Creates execution for a test case in launch.
   *
   * @param projectId project id
   * @param testCase  test case entity
   * @param launch    launch entity
   */

  void createExecution(long projectId, TmsTestCaseRS testCase, Launch launch);


  /**
   * Adds test cases to launch (creates executions).
   *
   * @param projectId
   * @param launch      launch entity
   * @param testCaseIds list of test case IDs
   */
  BatchTestCaseOperationResultRS addTestCasesToLaunch(long projectId, Launch launch, List<Long> testCaseIds);

  /**
   * Adds a test case to launch (creates executions).
   *
   * @param projectId
   * @param launch      launch entity
   * @param testCaseId  test case ID
   */
  void addTestCaseToLaunch(long projectId, Launch launch, Long testCaseId);

  /**
   * Removes test case execution from launch.
   *
   * @param executionId test case execution ID
   * @param launchId    launch ID
   */
  void deleteTestCaseExecutionFromLaunch(long projectId, Long launchId,
      Long executionId);

  /**
   * Checks if test case is in launch.
   *
   * @param testCaseId test case ID
   * @param launchId   launch ID
   * @return true if exists
   */
  boolean isTestCaseInLaunch(Long testCaseId, Long launchId);

  /**
   * Deletes all executions by launch ID.
   *
   * @param launchId launch ID
   */
  void deleteByLaunchId(Long launchId);

  /**
   * Gets count of test cases in launch.
   *
   * @param launchId launch ID
   * @return count of test cases
   */
  Long countTestCasesInLaunch(Long launchId);

  /**
   * Checks if execution exists for test item.
   *
   * @param testItemId test item ID
   * @return true if exists
   */
  boolean existsByTestItemId(Long testItemId);

  /**
   * Finds all executions by launch ID.
   *
   * @param launchId launch ID
   * @return list of executions
   */
  List<TmsTestCaseExecution> findByLaunchId(Long launchId);

  /**
   * Finds all executions by launch ID with full details loaded.
   *
   * @param launchId launch ID
   * @return list of executions with associations
   */
  List<TmsTestCaseExecution> findByLaunchIdWithDetails(Long launchId);

  /**
   * Finds execution by test case ID and launch ID.
   *
   * @param testCaseId test case ID
   * @param launchId   launch ID
   * @return optional execution
   */
  Optional<TmsTestCaseExecution> findByTestCaseExecutionIdAndLaunchId(Long testCaseId,
      Long launchId);

  /**
   * Finds executions by launch ID as map (test case ID -> execution).
   *
   * @param launchId launch ID
   * @return map of test case ID to execution
   */
  Map<Long, TmsTestCaseExecution> findExecutionsByLaunchIdAsMap(Long launchId);

  /**
   * Finds test case executions by launch ID with pagination and filtering.
   *
   * @param launchId launch ID
   * @param filter   filter criteria
   * @param pageable pagination parameters
   * @return page of test case execution DTOs
   */
  Page<TmsTestCaseExecutionRS> findByLaunchIdWithFilter(
      Long launchId,
      Filter filter,
      Pageable pageable
  );

  /**
   * Finds test case execution by ID and launch ID with full details.
   *
   * @param executionId execution ID
   * @param launchId    launch ID
   * @return test case execution DTO
   */
  TmsTestCaseExecutionRS findByIdAndLaunchIdWithDetails(
      Long executionId,
      Long launchId
  );

  /**
   * Finds test case executions by test case ID and launch ID.
   *
   * @param testCaseId test case ID
   * @param launchId   launch ID
   * @param pageable   pageable
   * @return list of test case execution DTOs
   */
  Page<TmsTestCaseExecutionRS> findByTestCaseIdAndLaunchId(
      Long testCaseId,
      Long launchId,
      Pageable pageable);

  /**
   * Updates test case execution.
   *
   * @param executionId execution ID
   * @param launchId    launch ID
   * @param request     update request
   * @return updated test case execution DTO
   */
  TmsTestCaseExecutionRS patch(
      Long executionId,
      Long launchId,
      TmsTestCaseExecutionRQ request
  );

  boolean existsByTestCaseExecutionIdAndLaunchId(Long executionId, Long launchId);

  TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(Long projectId, Long launchId,
      Long executionId, TmsTestCaseExecutionCommentRQ request);

  void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId);

  TmsManualLaunchExecutionStatisticRS getTestCaseExecutionStatistic(Long launchId);

  Map<Long, TmsManualLaunchExecutionStatisticRS> getTestCaseExecutionStatistic(List<Long> launchIds);
}
