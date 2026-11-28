package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import org.junit.jupiter.api.Test;

class TmsTestPlanMapperTest {

  private final TmsTestPlanMapper mapper = new TmsTestPlanMapperImpl(
      mock(TmsTestPlanAttributeMapper.class), mock(TmsTestPlanExecutionMapper.class));

  @Test
  void convertToRS_withMilestone_exposesMilestoneIdAndName() {
    var result = mapper.convertToRS(testPlan(milestone()));

    assertEquals(6L, result.getMilestoneId());
    assertEquals(6L, result.getMilestone().getId());
    assertEquals("Release 24.2 Beta", result.getMilestone().getName());
    assertNull(result.getMilestone().getType());
    assertNull(result.getMilestone().getStartDate());
    assertNull(result.getMilestone().getProductVersion());
  }

  @Test
  void convertTmsTestPlanWithStatisticToRS_withMilestone_exposesMilestoneIdAndName() {
    var result = mapper.convertTmsTestPlanWithStatisticToRS(
        TmsTestPlanWithStatistic.of(testPlan(milestone()), null));

    assertEquals(6L, result.getMilestoneId());
    assertEquals(6L, result.getMilestone().getId());
    assertEquals("Release 24.2 Beta", result.getMilestone().getName());
  }

  @Test
  void convertTmsTestPlanWithStatisticToRS_withoutMilestone_leavesMilestoneEmpty() {
    var result = mapper.convertTmsTestPlanWithStatisticToRS(
        TmsTestPlanWithStatistic.of(testPlan(null), null));

    assertNull(result.getMilestoneId());
    assertNull(result.getMilestone());
  }

  private static TmsMilestone milestone() {
    var milestone = new TmsMilestone();
    milestone.setId(6L);
    milestone.setName("Release 24.2 Beta");
    return milestone;
  }

  private static TmsTestPlan testPlan(TmsMilestone milestone) {
    var testPlan = new TmsTestPlan();
    testPlan.setId(17L);
    testPlan.setName("Smoke regression");
    testPlan.setMilestone(milestone);
    return testPlan;
  }
}
