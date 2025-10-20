package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import java.util.List;

public interface TmsTestPlanAttributeService {

  void createTestPlanAttributes(TmsTestPlan tmsTestPlan, List<TmsTestPlanAttributeRQ> attributes);

  void updateTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes);

  void patchTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes);

  void deleteAllByTestPlanId(Long testPlanId);

  /**
   * Duplicates attributes from the original test plan to the new test plan.
   * Creates new TmsTestPlanAttribute associations while preserving the original TmsAttribute entities.
   *
   * @param originalTestPlan the original test plan with attributes to duplicate
   * @param newTestPlan      the new test plan to attach duplicated attributes to
   */
  void duplicateTestPlanAttributes(TmsTestPlan originalTestPlan, TmsTestPlan newTestPlan);
}