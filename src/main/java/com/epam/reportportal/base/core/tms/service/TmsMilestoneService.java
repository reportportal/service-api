package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import java.util.List;

public interface TmsMilestoneService {

  void createTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void patchTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void updateTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void detachTestPlanFromMilestones(Long testPlanId);
}
