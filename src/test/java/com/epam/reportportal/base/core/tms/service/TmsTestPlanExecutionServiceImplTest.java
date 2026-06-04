package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanExecutionMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanStatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
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
  private TmsTestPlanExecutionStatistic statistics;
  private TmsTestPlanExecutionStatistic emptyStatistics;

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

    statistics = new TmsTestPlanExecutionStatistic(10, 5);

    emptyStatistics = new TmsTestPlanExecutionStatistic(0, 0);
  }

  @Test
  void getStatisticsForTestPlan_WhenStatisticsExist_ShouldReturnStatistics() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(statistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(10L, result.getTotal());
    assertEquals(5L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
    verify(tmsTestPlanExecutionMapper, never()).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlan_WhenStatisticsIsNull_ShouldReturnEmptyStatistics() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(null);
    when(tmsTestPlanExecutionMapper.createEmptyStatistics()).thenReturn(emptyStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(0L, result.getTotal());
    assertEquals(0L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
    verify(tmsTestPlanExecutionMapper).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlan_WhenExceptionOccurs_ShouldReturnEmptyStatistics() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenThrow(new RuntimeException("Database error"));
    when(tmsTestPlanExecutionMapper.createEmptyStatistics()).thenReturn(emptyStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(0L, result.getTotal());
    assertEquals(0L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
    verify(tmsTestPlanExecutionMapper).createEmptyStatistics();
  }

  @Test
  void getStatisticsForTestPlan_WithZeroValues_ShouldReturnZeroStatistics() {
    // Given
    var zeroStatistics = TmsTestPlanExecutionStatistic.builder()
        .total(0L)
        .covered(0L)
        .build();

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(zeroStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(0L, result.getTotal());
    assertEquals(0L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void getStatisticsForTestPlan_WithFullyCoveredPlan_ShouldReturnCorrectStatistics() {
    // Given
    var fullyCoveredStatistics = TmsTestPlanExecutionStatistic.builder()
        .total(10L)
        .covered(10L)
        .build();

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(fullyCoveredStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(10L, result.getTotal());
    assertEquals(10L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void getStatisticsForTestPlan_WithNotCoveredPlan_ShouldReturnCorrectStatistics() {
    // Given
    var notCoveredStatistics = new TmsTestPlanExecutionStatistic(10, 0);

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(notCoveredStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(10L, result.getTotal());
    assertEquals(0L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void getStatisticsForTestPlan_WithLargeNumbers_ShouldReturnCorrectStatistics() {
    // Given
    var largeStatistics = new TmsTestPlanExecutionStatistic(1000, 750);

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(largeStatistics);

    // When
    var result = sut.getStatisticsForTestPlan(testPlanId);

    // Then
    assertNotNull(result);
    assertEquals(1000L, result.getTotal());
    assertEquals(750L, result.getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void enrichWithStatistics_WhenTestPlanIsNull_ShouldReturnNull() {
    // When
    var result = sut.enrichWithStatistics(null);

    // Then
    assertNull(result);
    verify(tmsTestPlanStatisticsRepository, never()).getExecutionStatisticsByTestPlanId(any());
    verify(tmsTestPlanExecutionMapper, never()).toDto(any());
    verify(tmsTestPlanExecutionMapper, never()).createEmptyStatistics();
  }

  @Test
  void enrichWithStatistics_WhenTestPlanExists_ShouldReturnEnrichedTestPlan() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(statistics);

    // When
    var result = sut.enrichWithStatistics(testPlan);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTestPlan());
    assertNotNull(result.getExecutionStatistic());
    assertEquals(testPlanId, result.getId());
    assertEquals("Test Plan", result.getName());
    assertEquals(testPlan, result.getTestPlan());
    assertEquals(statistics, result.getExecutionStatistic());
    assertEquals(10L, result.getExecutionStatistic().getTotal());
    assertEquals(5L, result.getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void enrichWithStatistics_WhenStatisticsNotFound_ShouldReturnTestPlanWithEmptyStatistics() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(null);
    when(tmsTestPlanExecutionMapper.createEmptyStatistics()).thenReturn(emptyStatistics);

    // When
    var result = sut.enrichWithStatistics(testPlan);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTestPlan());
    assertNotNull(result.getExecutionStatistic());
    assertEquals(testPlanId, result.getId());
    assertEquals("Test Plan", result.getName());
    assertEquals(testPlan, result.getTestPlan());
    assertEquals(emptyStatistics, result.getExecutionStatistic());
    assertEquals(0L, result.getExecutionStatistic().getTotal());
    assertEquals(0L, result.getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
    verify(tmsTestPlanExecutionMapper).createEmptyStatistics();
    verify(tmsTestPlanExecutionMapper, never()).toDto(any());
  }

  @Test
  void enrichWithStatistics_WhenExceptionOccurs_ShouldReturnTestPlanWithEmptyStatistics() {
    // Given
    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenThrow(new RuntimeException("Database error"));
    when(tmsTestPlanExecutionMapper.createEmptyStatistics()).thenReturn(emptyStatistics);

    // When
    var result = sut.enrichWithStatistics(testPlan);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTestPlan());
    assertNotNull(result.getExecutionStatistic());
    assertEquals(testPlanId, result.getId());
    assertEquals("Test Plan", result.getName());
    assertEquals(testPlan, result.getTestPlan());
    assertEquals(emptyStatistics, result.getExecutionStatistic());
    assertEquals(0L, result.getExecutionStatistic().getTotal());
    assertEquals(0L, result.getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
    verify(tmsTestPlanExecutionMapper).createEmptyStatistics();
    verify(tmsTestPlanExecutionMapper, never()).toDto(any());
  }

  @Test
  void enrichWithStatistics_WithZeroStatistics_ShouldReturnEnrichedTestPlan() {
    // Given
    var zeroStatistics = TmsTestPlanExecutionStatistic.builder()
        .total(0L)
        .covered(0L)
        .build();

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(testPlanId))
        .thenReturn(zeroStatistics);

    // When
    var result = sut.enrichWithStatistics(testPlan);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTestPlan());
    assertNotNull(result.getExecutionStatistic());
    assertEquals(0L, result.getExecutionStatistic().getTotal());
    assertEquals(0L, result.getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(testPlanId);
  }

  @Test
  void enrichWithStatistics_WithDifferentTestPlanIds_ShouldLoadCorrectStatistics() {
    // Given
    var anotherTestPlanId = 999L;
    var anotherTestPlan = new TmsTestPlan();
    anotherTestPlan.setId(anotherTestPlanId);
    anotherTestPlan.setName("Another Test Plan");

    var anotherStatistics = new TmsTestPlanExecutionStatistic(20, 15);

    when(tmsTestPlanStatisticsRepository.getExecutionStatisticsByTestPlanId(anotherTestPlanId))
        .thenReturn(anotherStatistics);

    // When
    var result = sut.enrichWithStatistics(anotherTestPlan);

    // Then
    assertNotNull(result);
    assertEquals(anotherTestPlanId, result.getId());
    assertEquals("Another Test Plan", result.getName());
    assertEquals(20L, result.getExecutionStatistic().getTotal());
    assertEquals(15L, result.getExecutionStatistic().getCovered());
    verify(tmsTestPlanStatisticsRepository).getExecutionStatisticsByTestPlanId(anotherTestPlanId);
  }
}
