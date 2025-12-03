package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsManualLaunchMapper {

  @Mapping(target = "executionStatistic", source = "testCaseExecutionStatistic")
  TmsManualLaunchRS convert(Launch launch,
      TmsManualLaunchExecutionStatisticRS testCaseExecutionStatistic);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "attributes", ignore = true)
  void patch(@MappingTarget Launch existingLaunch, TmsManualLaunchRQ request);

  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "uuid",
      expression = "java(java.util.Optional.ofNullable(request.getUuid()).orElse(java.util.UUID.randomUUID().toString()))")
  @Mapping(target = "startTime",
      expression = "java(request.getStartTime() == null ? java.time.Instant.now() : java.time.Instant.parse(request.getStartTime()))")
  @Mapping(target = "mode",
      expression = "java(request.getMode() == null ? "
          + "com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchModeEnum.DEFAULT : "
          + "com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchModeEnum.findByName(request.getMode().name()).orElseThrow())")
  Launch convertFromRQ(long projectId, TmsManualLaunchRQ request);

  default BatchTestCaseOperationResultRS convertBatchAddTestCaseOperationResultRS(
      List<Long> testCaseIds, List<Long> successTestCaseIds,
      List<BatchTestCaseOperationError> errors) {
    return BatchTestCaseOperationResultRS.builder()
        .totalCount(testCaseIds.size())
        .successCount(successTestCaseIds.size())
        .failureCount(errors.size())
        .successTestCaseIds(successTestCaseIds)
        .errors(errors)
        .build();
  }

  default BatchManualLaunchOperationResultRS convertToBatchDeleteResponse(List<Long> launchIds,
      List<Long> successLaunchIds, List<BatchManualLaunchOperationError> errors) {
    return BatchManualLaunchOperationResultRS.builder()
        .totalCount(launchIds.size())
        .successCount(successLaunchIds.size())
        .failureCount(errors.size())
        .successLaunchIds(successLaunchIds)
        .errors(errors)
        .build();
  }
}
