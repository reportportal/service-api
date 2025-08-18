package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import java.util.List;

public interface TmsTestPlanAttributeService {

  void createTestPlanAttributes(TmsTestPlan tmsTestPlan, List<TmsAttributeRQ> attributes);

  void updateTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsAttributeRQ> attributes);

  void patchTestPlanAttributes(TmsTestPlan existingTestPlan,
      List<TmsAttributeRQ> attributes);

  void deleteAllByTestPlanId(Long testPlanId);
}
