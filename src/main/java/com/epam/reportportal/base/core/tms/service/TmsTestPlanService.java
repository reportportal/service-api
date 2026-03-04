package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.Page;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface TmsTestPlanService extends CrudService<TmsTestPlanRQ, TmsTestPlanRS, Long> {

  Page<TmsTestPlanRS> getByCriteria(Long projectId, Filter filter, Pageable pageable);

  BatchTestCaseOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId, @NotEmpty List<Long> testCaseIds);

  BatchTestCaseOperationResultRS removeTestCasesFromPlan(Long projectId, Long testPlanId, @NotEmpty List<Long> testCaseIds);

  boolean addTestCaseToTestPlan(Long testPlanId, Long testCaseId);

  boolean removeSingleTestCaseFromPlan(Long testPlanId, Long testCaseId);

  DuplicateTmsTestPlanRS duplicate(Long projectId, Long testPlanId,
      TmsTestPlanRQ duplicateTestPlanRQ);

  DuplicateTmsTestPlanRS duplicate(Long projectId, Long testPlanId);

  /**
   * Retrieves test cases added to a test plan with pagination. Returns test cases with last
   * execution only (without full execution history).
   *
   * @param projectId    the project ID
   * @param testPlanId   the test plan ID
   * @param testFolderId test folder ID
   * @param pageable     pagination parameters
   * @return page of test cases added to the test plan
   */
  Page<TmsTestCaseInTestPlanRS> getTestCasesAddedToPlan(Long projectId, Long testPlanId,
      Long testFolderId, Pageable pageable);

  /**
   * Retrieves a single test case in test plan with full execution history.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @param testCaseId the test case ID
   * @return test case with last execution and all executions
   */
  TmsTestCaseInTestPlanRS getTestCaseInTestPlan(Long projectId, Long testPlanId, Long testCaseId);

  /**
   * Verifies that test plan exists in the project.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @throws ReportPortalException if test plan not found
   */
  void verifyTestPlanExists(Long projectId, Long testPlanId);

  /**
   * Retrieves test folders where test cases added to a test plan with pagination.
   * Returns folders containing test cases that are part of the specified test plan.
   *
   * @param projectId  the project ID
   * @param testPlanId the test plan ID
   * @param pageable   pagination parameters
   * @return page of test folders from the test plan
   */
  Page<TmsTestFolderRS> getTestFoldersFromPlan(Long projectId, Long testPlanId, Pageable pageable);

  /**
   * Get test plans map by test plan IDs
   * @param testPlanIds list of test plan IDs
   * @return map where key is test plan ID and value is TmsTestPlan entity
   */
  Map<Long, TmsTestPlan> getTestPlanMap(List<Long> testPlanIds);

  TmsTestPlan getEntityById(Long projectId, Long testPlanId);

  List<Long> getTestCaseIdsAddedToPlan(long projectId, Long testPlanId);

  void removeTestPlanFromMilestone(Long projectId, Long milestoneId, Long testPlanId);

  Map<Long, List<TmsTestPlanRS>> getByMilestoneIds(Long projectId, List<Long> milestoneIds);

  List<TmsTestPlanRS> getByMilestoneId(Long projectId, Long milestoneId);

  List<DuplicateTmsTestPlanRS> duplicateTestPlansInMilestone(Long projectId, Long milestoneId);

  void addTestPlanMilestone(Long projectId, Long milestoneId, Long testPlanId);

  void removeTestPlansFromMilestone(Long projectId, Long milestoneId);
}
