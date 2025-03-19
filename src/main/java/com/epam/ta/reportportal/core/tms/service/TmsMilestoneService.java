package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import java.util.List;

public interface TmsMilestoneService {

    void upsertTestPlanToMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

    void detachTestPlanFromMilestones(Long testPlanId);
}
