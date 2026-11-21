package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TmsTestPlanExecutionService {

  TmsTestPlanExecutionStatistic getStatisticsForTestPlan(Long testPlanId);

  /**
   * Loads execution statistics for several test plans with one query.
   *
   * @param testPlanIds test plan IDs
   * @return statistics by test plan ID; a plan without data gets empty statistics
   */
  Map<Long, TmsTestPlanExecutionStatistic> getStatisticsForTestPlans(Collection<Long> testPlanIds);

  TmsTestPlanWithStatistic enrichWithStatistics(TmsTestPlan testPlan);

  /**
   * Enriches test plans with execution statistics loaded with one query. Null elements are skipped,
   * the order is preserved.
   *
   * @param testPlans test plans
   * @return test plans with statistics
   */
  List<TmsTestPlanWithStatistic> enrichWithStatistics(List<TmsTestPlan> testPlans);
}
