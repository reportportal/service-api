package com.epam.reportportal.infrastructure.persistence.entity.tms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlanWithStatistic {

  private TmsTestPlan testPlan;
  private TmsTestPlanExecutionStatistic executionStatistic;

  public static TmsTestPlanWithStatistic of(TmsTestPlan testPlan,
      TmsTestPlanExecutionStatistic statistic) {
    return new TmsTestPlanWithStatistic(testPlan, statistic);
  }

  public Long getId() {
    return testPlan.getId();
  }

  public String getName() {
    return testPlan.getName();
  }
}
