package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import java.util.List;

public interface TmsMilestoneService {

  void createTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void patchTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void updateTestPlanMilestones(TmsTestPlan tmsTestPlan, List<Long> milestoneIds);

  void detachTestPlanFromMilestones(Long testPlanId);
}
