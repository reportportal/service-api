package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchExecutionStatisticRS;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchManualLaunchOperationResultRS;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsManualLaunchMapper {

  @Mapping(target = "id", source = "launch.id")
  @Mapping(target = "name", source = "launch.name")
  @Mapping(target = "description", source = "launch.description")
  @Mapping(target = "owner.id", source = "launch.userId")
  @Mapping(target = "type", expression = "java(launch.getLaunchType() != null ? launch.getLaunchType().name() : null)")
  @Mapping(target = "startTime", source = "launch.startTime")
  @Mapping(target = "endTime", source = "launch.endTime")
  @Mapping(target = "createdAt", expression = "java(launch.getLastModified() != null ? launch.getLastModified().toString() : null)")
  @Mapping(target = "number", source = "launch.number")
  @Mapping(target = "mode", source = "launch.mode")
  @Mapping(target = "status", source = "launch.status")
  @Mapping(target = "testPlan.id", source = "launch.testPlanId")
  @Mapping(target = "attributes", source = "launch.attributes")
  @Mapping(target = "executionStatistic", source = "testCaseExecutionStatistic")
  TmsManualLaunchRS convert(Launch launch,
      TmsManualLaunchExecutionStatisticRS testCaseExecutionStatistic);

  @Mapping(target = "id", source = "launch.id")
  @Mapping(target = "name", source = "launch.name")
  @Mapping(target = "description", source = "launch.description")
  @Mapping(target = "owner.id", source = "launch.userId")
  @Mapping(target = "type", expression = "java(launch.getLaunchType() != null ? launch.getLaunchType().name() : null)")
  @Mapping(target = "startTime", source = "launch.startTime")
  @Mapping(target = "endTime", source = "launch.endTime")
  @Mapping(target = "createdAt", expression = "java(launch.getLastModified() != null ? launch.getLastModified().toString() : null)")
  @Mapping(target = "number", source = "launch.number")
  @Mapping(target = "mode", source = "launch.mode")
  @Mapping(target = "status", source = "launch.status")
  @Mapping(target = "testPlan.id", source = "launch.testPlanId")
  @Mapping(target = "attributes", source = "launch.attributes")
  @Mapping(target = "executionStatistic.total", source = "batchTestCaseOperationResultRS.successCount")
  @Mapping(target = "executionStatistic.toRun", source = "batchTestCaseOperationResultRS.successCount")
  CreateTmsManualLaunchRS convertToCreateTmsManualLaunchRS(Launch launch,
      BatchTestCaseOperationResultRS batchTestCaseOperationResultRS);

  /**
   * Convert launch with user and test plan maps to response DTO
   */
  @Mapping(target = "id", source = "launch.id")
  @Mapping(target = "name", source = "launch.name")
  @Mapping(target = "description", source = "launch.description")
  @Mapping(target = "owner.id", source = "user.id")
  @Mapping(target = "owner.email", source = "user.email")
  @Mapping(target = "type", expression = "java(launch.getLaunchType() != null ? launch.getLaunchType().name() : null)")
  @Mapping(target = "startTime", source = "launch.startTime")
  @Mapping(target = "endTime", source = "launch.endTime")
  @Mapping(target = "createdAt", expression = "java(launch.getLastModified() != null ? launch.getLastModified().toString() : null)")
  @Mapping(target = "number", source = "launch.number")
  @Mapping(target = "mode", source = "launch.mode")
  @Mapping(target = "status", source = "launch.status")
  @Mapping(target = "testPlan.id", source = "testPlan.id")
  @Mapping(target = "testPlan.name", source = "testPlan.name")
  @Mapping(target = "attributes", source = "launch.attributes")
  @Mapping(target = "executionStatistic", source = "testCaseExecutionStatistic")
  TmsManualLaunchRS convert(Launch launch,
      TmsManualLaunchExecutionStatisticRS testCaseExecutionStatistic,
      User user,
      TmsTestPlan testPlan);

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
  @Mapping(target = "testPlanId", source = "request.testPlanId")
  @Mapping(target = "userId", source = "user.userId")
  Launch convertFromCreateTmsManualLaunchRQ(Long projectId, ReportPortalUser user,
      CreateTmsManualLaunchRQ request);

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
