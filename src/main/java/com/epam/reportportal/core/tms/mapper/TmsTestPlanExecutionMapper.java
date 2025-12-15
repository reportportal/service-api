package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsTestPlanExecutionStatisticRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanExecutionStatistic;
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
