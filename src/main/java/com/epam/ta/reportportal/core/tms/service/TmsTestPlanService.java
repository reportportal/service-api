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

  /**
   * Duplicates a test plan with all its associated test cases.
   *
   * @param projectId the project ID
   * @param testPlanId the ID of the test plan to duplicate
   * @return the duplicated test plan with statistics
   */
  TmsTestPlanRS duplicate(long projectId, Long testPlanId);

  boolean addTestCaseToTestPlan(Long testPlanId, Long testCaseId);

  boolean removeSingleTestCaseFromPlan(Long testPlanId, Long testCaseId);
}