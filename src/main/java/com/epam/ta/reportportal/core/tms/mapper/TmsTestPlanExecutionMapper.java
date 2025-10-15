package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanExecutionStatisticRS;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestPlanExecutionMapper {

  TmsTestPlanExecutionStatisticRS toDto(
      com.epam.ta.reportportal.entity.tms.TmsTestPlanExecutionStatisticRS executionStatistic);

  default TmsTestPlanExecutionStatisticRS createEmptyStatistics() {
    return TmsTestPlanExecutionStatisticRS.builder()
        .total(0L)
        .covered(0L)
        .build();
  }
}
