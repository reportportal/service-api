package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionRS;
import com.epam.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.core.tms.dto.batch.BatchAddTestCasesToLaunchRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchDeleteManualLaunchesRQ;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.util.OffsetRequest;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing TMS Manual Launches.
 */
public interface TmsManualLaunchService {

  /**
   * Creates a new manual launch.
   *
   * @param projectId project ID
   * @param request   manual launch request
   * @return created manual launch response
   */
  TmsManualLaunchRS create(long projectId, TmsManualLaunchRQ request);

  /**
   * Gets manual launch by ID.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   * @return manual launch response
   */
  TmsManualLaunchRS getById(long projectId, Long launchId);

  /**
   * Gets manual launches by criteria.
   *
   * @param projectId project ID
   * @param filter    filter criteria
   * @param pageable  pagination parameters
   * @return page of manual launches
   */
  Page<TmsManualLaunchRS> getManualLaunches(long projectId, Filter filter, Pageable pageable);

  /**
   * Deletes manual launch by ID.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   */
  void delete(long projectId, Long launchId);

  /**
   * Patches manual launch.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   * @param request   patch request
   * @return updated manual launch response
   */
  TmsManualLaunchRS patch(long projectId, Long launchId, TmsManualLaunchRQ request);

  /**
   * Adds single test case to launch.
   *
   * @param projectId  project ID
   * @param launchId   launch ID
   * @param request    test case ID
   */
  void addTestCaseToLaunch(Long projectId, Long launchId, AddTestCaseToLaunchRQ request);

  /**
   * Adds multiple test cases to launch.
   *
   * @param projectId   project ID
   * @param launchId    launch ID
   * @param request     list of test case IDs
   * @return operation result
   */
  BatchTestCaseOperationResultRS addTestCasesToLaunch(Long projectId, Long launchId, BatchAddTestCasesToLaunchRQ request);

  /**
   * Gets all test case executions of launch with pagination and filtering.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   * @param filter    filter criteria
   * @param pageable  pagination details
   * @return page of test case executions
   */
  Page<TmsTestCaseExecutionRS> getLaunchTestCaseExecutions(
      Long projectId,
      Long launchId,
      Filter filter,
      OffsetRequest pageable
  );

  /**
   * Gets specific test case execution of launch.
   *
   * @param projectId   project ID
   * @param launchId    launch ID
   * @param executionId execution ID
   * @return test case execution details
   */
  TmsTestCaseExecutionRS getTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId
  );

  /**
   * Deletes specific test case execution from launch.
   *
   * @param projectId   project ID
   * @param launchId    launch ID
   * @param executionId execution ID
   */
  void deleteTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId
  );

  /**
   * Gets all executions of specific test case in launch.
   *
   * @param projectId  project ID
   * @param launchId   launch ID
   * @param testCaseId test case ID
   * @return list of test case executions
   */
  Page<TmsTestCaseExecutionRS> getTestCaseExecutionsInLaunchForTestCase(
      Long projectId,
      Long launchId,
      Long testCaseId,
      OffsetRequest pageable
  );

  /**
   * Patches test case execution (updates status and comment).
   *
   * @param projectId   project ID
   * @param launchId    launch ID
   * @param executionId execution ID
   * @param request     patch request data
   * @return updated test case execution
   */
  TmsTestCaseExecutionRS patchTestCaseExecution(
      Long projectId,
      Long launchId,
      Long executionId,
      TmsTestCaseExecutionRQ request
  );

  /**
   * Gets unique folders from test cases in launch.
   *
   * @param projectId project ID
   * @param launchId  launch ID
   * @param pageable  pagination parameters
   * @return page of test folders
   */
  Page<TmsTestFolderRS> getLaunchFolders(Long projectId, Long launchId, Pageable pageable);

  BatchManualLaunchOperationResultRS batchDeleteManualLaunches(Long projectId, BatchDeleteManualLaunchesRQ batchDeleteManualLaunchesRQ);

  Optional<Long> getTestPlanIdByLaunchId(Long launchId);
}
