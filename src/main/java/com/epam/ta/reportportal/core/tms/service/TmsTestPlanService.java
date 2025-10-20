package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationResultRS;
import com.epam.ta.reportportal.model.Page;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TmsTestPlanService extends CrudService<TmsTestPlanRQ, TmsTestPlanRS, Long> {

  Page<TmsTestPlanRS> getByCriteria(Long projectId, Filter filter, Pageable pageable);

  BatchOperationResultRS addTestCasesToPlan(Long projectId, Long testPlanId, @NotEmpty List<Long> testCaseIds);

  BatchOperationResultRS removeTestCasesFromPlan(Long projectId, Long testPlanId, @NotEmpty List<Long> testCaseIds);

  boolean addTestCaseToTestPlan(Long testPlanId, Long testCaseId);

  boolean removeSingleTestCaseFromPlan(Long testPlanId, Long testCaseId);

  /**
   * Duplicates a test plan and all its associated test cases.
   * Creates a new test plan with "-copy" postfix in the name and duplicates all test cases 
   * that are currently added to the original test plan, also with "-copy" postfix in their names.
   * All test cases are duplicated to their same folders.
   *
   * @param projectId The ID of the project.
   * @param testPlanId The ID of the test plan to duplicate.
   * @return The duplicated test plan details.
   */
  TmsTestPlanRS duplicate(long projectId, Long testPlanId);
}