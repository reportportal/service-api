package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import java.util.List;

public interface TmsTestPlanAttributeService {

  void createTestPlanAttributes(TmsTestPlan tmsTestPlan, List<TmsTestPlanAttributeRQ> attributes);

  void updateTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes);

  void patchTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsTestPlanAttributeRQ> attributes);

  void deleteAllByTestPlanId(Long testPlanId);

  /**
   * Duplicates attributes from original test plan to new test plan. 
   * Uses existing TmsAttribute entities but creates new TmsTestPlanAttribute associations.
   *
   * @param originalTestPlan The original test plan with attributes to duplicate.
   * @param newTestPlan      The new test plan to attach duplicated attributes to.
   */
  void duplicateTestPlanAttributes(TmsTestPlan originalTestPlan, TmsTestPlan newTestPlan);
}
