package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;

public interface TmsTestPlanExecutionService {

  TmsTestPlanExecutionStatistic getStatisticsForTestPlan(Long testPlanId);

  TmsTestPlanWithStatistic enrichWithStatistics(TmsTestPlan testPlan);
}
