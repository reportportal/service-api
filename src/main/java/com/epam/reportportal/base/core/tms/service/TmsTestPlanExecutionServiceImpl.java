package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.mapper.TmsTestPlanExecutionMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestPlanStatisticsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.projection.TmsTestPlanStatisticByTestPlan;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsTestPlanExecutionServiceImpl implements TmsTestPlanExecutionService {

  private final TmsTestPlanStatisticsRepository tmsTestPlanStatisticsRepository;
  private final TmsTestPlanExecutionMapper tmsTestPlanExecutionMapper;

  @Override
  @Transactional(readOnly = true)
  public TmsTestPlanExecutionStatistic getStatisticsForTestPlan(Long testPlanId) {
    return getStatisticsForTestPlans(List.of(testPlanId)).get(testPlanId);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, TmsTestPlanExecutionStatistic> getStatisticsForTestPlans(
      Collection<Long> testPlanIds) {
    if (testPlanIds.isEmpty()) {
      return Map.of();
    }
    log.debug("Loading execution statistics for test plans: {}", testPlanIds);

    var statistics = tmsTestPlanStatisticsRepository
        .getExecutionStatisticsByTestPlanIds(testPlanIds)
        .stream()
        .collect(Collectors.toMap(
            TmsTestPlanStatisticByTestPlan::getTestPlanId,
            row -> new TmsTestPlanExecutionStatistic(row.getTotal(), row.getCovered())
        ));

    return testPlanIds
        .stream()
        .distinct()
        .collect(Collectors.toMap(
            testPlanId -> testPlanId,
            testPlanId -> {
              var statistic = statistics.get(testPlanId);
              return statistic != null ? statistic
                  : tmsTestPlanExecutionMapper.createEmptyStatistics();
            }
        ));
  }

  @Transactional(readOnly = true)
  @Override
  public TmsTestPlanWithStatistic enrichWithStatistics(TmsTestPlan testPlan) {
    if (testPlan == null) {
      log.warn("Attempted to enrich null test plan with statistics");
      return null;
    }

    return TmsTestPlanWithStatistic.of(testPlan, getStatisticsForTestPlan(testPlan.getId()));
  }

  @Transactional(readOnly = true)
  @Override
  public List<TmsTestPlanWithStatistic> enrichWithStatistics(List<TmsTestPlan> testPlans) {
    var nonNullTestPlans = testPlans.stream().filter(Objects::nonNull).toList();
    var statistics = getStatisticsForTestPlans(
        nonNullTestPlans.stream().map(TmsTestPlan::getId).toList()
    );

    return nonNullTestPlans
        .stream()
        .map(testPlan -> TmsTestPlanWithStatistic.of(testPlan, statistics.get(testPlan.getId())))
        .toList();
  }
}
