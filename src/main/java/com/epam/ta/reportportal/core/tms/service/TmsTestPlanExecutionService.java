package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanExecutionStatisticRS;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanWithStatistic;

public interface TmsTestPlanExecutionService {

  TmsTestPlanExecutionStatisticRS getStatisticsForTestPlan(Long testPlanId);

  TmsTestPlanWithStatistic enrichWithStatistics(TmsTestPlan testPlan);
}
