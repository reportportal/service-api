package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import java.util.List;
import java.util.Map;

public interface TmsTestCaseExecutionService {

  Map<Long, TmsTestCaseExecution> getLastTestCasesExecutionsByTestCaseIds(List<Long> testCaseIds);

  TmsTestCaseExecution getLastTestCaseExecution(Long testCaseId);

  /**
   * Finds last executions for multiple test cases within a specific test plan.
   * Returns a map where key is test case ID and value is the last execution.
   *
   * @param testCaseIds list of test case IDs
   * @param testPlanId  the test plan ID
   * @return map of test case ID to last execution
   */
  Map<Long, TmsTestCaseExecution> findLastExecutionsByTestCaseIdsAndTestPlanId(
      List<Long> testCaseIds, Long testPlanId);

  /**
   * Finds all executions for a specific test case within a test plan.
   * Results are ordered by test_item.start_time DESC (latest first).
   *
   * @param testCaseId the test case ID
   * @param testPlanId the test plan ID
   * @return list of executions ordered by start time descending
   */
  List<TmsTestCaseExecution> findByTestCaseIdAndTestPlanId(Long testCaseId, Long testPlanId);
}
