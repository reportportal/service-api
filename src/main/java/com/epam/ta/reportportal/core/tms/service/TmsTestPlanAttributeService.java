package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import java.util.List;

public interface TmsTestPlanAttributeService {

    void createTestPlanAttributes(TmsTestPlan tmsTestPlan, List<TmsTestPlanAttributeRQ> attributes);

    void updateTestPlanAttributes(TmsTestPlan existingTestPlan,
        List<TmsTestPlanAttributeRQ> attributes);

    void patchTestPlanAttributes(TmsTestPlan existingTestPlan,
        List<TmsTestPlanAttributeRQ> attributes);
}
