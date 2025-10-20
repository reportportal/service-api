package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanExecutionStatisticRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanExecutionStatistic;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestPlanExecutionMapper {

  TmsTestPlanExecutionStatisticRS toDto(
      TmsTestPlanExecutionStatistic executionStatistic);

  default TmsTestPlanExecutionStatistic createEmptyStatistics() {
    return TmsTestPlanExecutionStatistic.builder()
        .total(0L)
        .covered(0L)
        .build();
  }
}
