package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanExecutionMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanStatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.projection.TmsTestPlanStatisticByTestPlan;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestPlanExecutionServiceImplTest {

  @Mock
  private TmsTestPlanStatisticsRepository tmsTestPlanStatisticsRepository;

  @Mock
  private TmsTestPlanExecutionMapper tmsTestPlanExecutionMapper;

  private TmsTestPlanExecutionServiceImpl sut;

  private Long testPlanId;
  private TmsTestPlan testPlan;

  @BeforeEach
  void setUp() {
    sut = new TmsTestPlanExecutionServiceImpl(
        tmsTestPlanStatisticsRepository,
        tmsTestPlanExecutionMapper
    );

    testPlanId = 1L;

    testPlan = new TmsTestPlan();
    testPlan.setId(testPlanId);
    testPlan.setName("Test Plan");
    testPlan.setDescription("Test Plan Description");
  }

  private static TmsTestPlanStatisticByTestPlan row(Long testPlanId, long total, long covered) {
    return new TmsTestPlanStatisticByTestPlan() {
      @Override
      public Long getTestPlanId() {
        return testPlanId;
      }

      @Override
      public Long getTotal() {
        return total;
      }

      @Override
      public Long getCovered() {
        return covered;
      }
    };
  }

  @Test
  void getStatisticsForTestPlan_WhenStatisticsExist_ShouldReturnStatistics() {
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(testPlanId)))
        .thenReturn(List.of(row(testPlanId, 10, 5)));

    var result = sut.getStatisticsForTestPlan(testPlanId);

    assertNotNull(result);
    assertEquals(10L, result.getTotal());
    assertEquals(5L, result.getCovered());
    verify(tmsTestPlanExecutionMapper, never()).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlan_WhenNoRow_ShouldReturnEmptyStatistics() {
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(testPlanId)))
        .thenReturn(List.of());
    when(tmsTestPlanExecutionMapper.createEmptyStatistics())
        .thenReturn(new TmsTestPlanExecutionStatistic(0, 0));

    var result = sut.getStatisticsForTestPlan(testPlanId);

    assertEquals(0L, result.getTotal());
    assertEquals(0L, result.getCovered());
    verify(tmsTestPlanExecutionMapper).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlan_WhenRepositoryFails_ShouldPropagateException() {
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(testPlanId)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> sut.getStatisticsForTestPlan(testPlanId));
    verify(tmsTestPlanExecutionMapper, never()).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlans_ShouldLoadAllPlansWithOneQuery() {
    var ids = List.of(1L, 2L, 3L);
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(ids))
        .thenReturn(List.of(row(1L, 10, 5), row(2L, 3, 3)));
    when(tmsTestPlanExecutionMapper.createEmptyStatistics())
        .thenReturn(new TmsTestPlanExecutionStatistic(0, 0));

    var result = sut.getStatisticsForTestPlans(ids);

    assertEquals(3, result.size());
    assertEquals(10L, result.get(1L).getTotal());
    assertEquals(5L, result.get(1L).getCovered());
    assertEquals(3L, result.get(2L).getTotal());
    assertEquals(3L, result.get(2L).getCovered());
    assertEquals(0L, result.get(3L).getTotal());
    verify(tmsTestPlanStatisticsRepository, times(1)).getExecutionStatisticsByTestPlanIds(anyCollection());
  }

  @Test
  void getStatisticsForTestPlans_WhenEmpty_ShouldNotQuery() {
    assertTrue(sut.getStatisticsForTestPlans(List.of()).isEmpty());
    verify(tmsTestPlanStatisticsRepository, never()).getExecutionStatisticsByTestPlanIds(any());
  }

  @Test
  void enrichWithStatistics_WhenTestPlanIsNull_ShouldReturnNull() {
    var result = sut.enrichWithStatistics((TmsTestPlan) null);

    assertNull(result);
    verify(tmsTestPlanStatisticsRepository, never()).getExecutionStatisticsByTestPlanIds(any());
  }

  @Test
  void enrichWithStatistics_WhenTestPlanExists_ShouldReturnEnrichedTestPlan() {
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(testPlanId)))
        .thenReturn(List.of(row(testPlanId, 10, 5)));

    var result = sut.enrichWithStatistics(testPlan);

    assertNotNull(result);
    assertEquals(testPlan, result.getTestPlan());
    assertEquals(10L, result.getExecutionStatistic().getTotal());
    assertEquals(5L, result.getExecutionStatistic().getCovered());
  }

  @Test
  void enrichWithStatistics_WhenRepositoryFails_ShouldPropagateException() {
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(testPlanId)))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> sut.enrichWithStatistics(testPlan));
  }

  @Test
  void enrichWithStatistics_ForList_ShouldUseOneQueryKeepOrderAndSkipNulls() {
    var anotherTestPlan = new TmsTestPlan();
    anotherTestPlan.setId(999L);
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanIds(List.of(999L, testPlanId)))
        .thenReturn(List.of(row(testPlanId, 10, 5), row(999L, 20, 15)));

    var result = sut.enrichWithStatistics(Arrays.asList(anotherTestPlan, null, testPlan));

    assertEquals(2, result.size());
    assertEquals(999L, result.get(0).getId());
    assertEquals(20L, result.get(0).getExecutionStatistic().getTotal());
    assertEquals(15L, result.get(0).getExecutionStatistic().getCovered());
    assertEquals(testPlanId, result.get(1).getId());
    assertEquals(10L, result.get(1).getExecutionStatistic().getTotal());
    assertEquals(5L, result.get(1).getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository, times(1)).getExecutionStatisticsByTestPlanIds(anyCollection());
  }
}
