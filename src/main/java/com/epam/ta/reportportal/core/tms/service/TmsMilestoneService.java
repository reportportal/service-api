package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import java.util.List;

public interface TmsMilestoneService {

  void createTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void patchTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void updateTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void detachTestPlanFromMilestones(Long testPlanId);
}
