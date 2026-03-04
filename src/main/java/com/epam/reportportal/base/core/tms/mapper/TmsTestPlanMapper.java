package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanExecutionStatisticRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanWithStatistic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Mapper(config = CommonMapperConfig.class, uses = {
    TmsTestPlanAttributeMapper.class, TmsTestPlanExecutionMapper.class
})
public abstract class TmsTestPlanMapper {

  @Mapping(target = "attributes", source = "attributes")
  @Mapping(target = "milestoneId", source = "milestone.id")
  public abstract TmsTestPlanRS convertToRS(TmsTestPlan tmsTestPlan);

  @Mapping(target = "id", source = "tmsTestPlan.testPlan.id")
  @Mapping(target = "name", source = "tmsTestPlan.testPlan.name")
  @Mapping(target = "description", source = "tmsTestPlan.testPlan.description")
  @Mapping(target = "attributes", source = "tmsTestPlan.testPlan.attributes")
  @Mapping(target = "milestoneId", source = "tmsTestPlan.testPlan.milestone.id")
  @Mapping(target = "executionStatistic", source = "tmsTestPlan.executionStatistic")
  public abstract TmsTestPlanRS convertTmsTestPlanWithStatisticToRS(
      TmsTestPlanWithStatistic tmsTestPlan);

  public List<TmsTestPlanRS> convertTmsTestPlansWithStatisticToRS(
      List<TmsTestPlanWithStatistic> tmsTestPlans) {
    return Optional
        .ofNullable(tmsTestPlans)
        .orElse(Collections.emptyList())
        .stream()
        .map(this::convertTmsTestPlanWithStatisticToRS)
        .toList();
  }

  public Map<Long, List<TmsTestPlanRS>> convertTmsTestPlansWithStatisticToMap(
      List<TmsTestPlanWithStatistic> tmsTestPlans) {
    return Optional
        .ofNullable(tmsTestPlans)
        .orElse(Collections.emptyList())
        .stream()
        .map(this::convertTmsTestPlanWithStatisticToRS)
        .filter(testPlan -> Objects.nonNull(testPlan.getMilestoneId()))
        .collect(
            Collectors.groupingBy(
                TmsTestPlanRS::getMilestoneId,
                Collectors.toList()
            )
        );
  }

  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestone", source = "testPlanRQ.milestoneId", qualifiedByName = "milestoneIdToMilestone")
  public abstract TmsTestPlan convertFromRQ(Long projectId, TmsTestPlanRQ testPlanRQ);

  @Named("milestoneIdToMilestone")
  protected TmsMilestone milestoneIdToMilestone(Long milestoneId) {
    if (milestoneId == null) {
      return null;
    }
    var milestone = new TmsMilestone();
    milestone.setId(milestoneId);
    return milestone;
  }

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract void update(@MappingTarget TmsTestPlan targetTestPlan, TmsTestPlan tmsTestPlan);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract void patch(@MappingTarget TmsTestPlan existingTestPlan,
      TmsTestPlan tmsTestPlan);

  public BatchTestCaseOperationResultRS convertToRS(int totalCount, int successCount,
      List<BatchTestCaseOperationError> errors) {
    return BatchTestCaseOperationResultRS.builder()
        .totalCount(totalCount)
        .successCount(successCount)
        .failureCount(totalCount - successCount)
        .errors(errors)
        .build();
  }

  public Page<TmsTestPlanRS> convertTmsTestPlanWithStatisticToRS(
      List<TmsTestPlanWithStatistic> orderedTestPlans,
      Pageable pageable,
      long totalCount) {
    return new PageImpl<>(
        orderedTestPlans
            .stream()
            .map(this::convertTmsTestPlanWithStatisticToRS)
            .toList(),
        pageable,
        totalCount
    );
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "launches", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "project", source = "originalTestPlan.project")
  @Mapping(target = "environment", source = "originalTestPlan.environment")
  @Mapping(target = "productVersion", source = "originalTestPlan.productVersion")
  @Mapping(target = "name", source = "duplicateTestPlanRQ.name")
  @Mapping(target = "description", source = "duplicateTestPlanRQ.description")
  @Mapping(target = "milestone", source = "duplicateTestPlanRQ.milestoneId", qualifiedByName = "milestoneIdToMilestone")
  public abstract TmsTestPlan duplicateTestPlan(TmsTestPlan originalTestPlan,
      TmsTestPlanRQ duplicateTestPlanRQ);

  @Mapping(target = "duplicationStatistic", ignore = true)
  @Mapping(target = "milestoneId", source = "milestone.id")
  public abstract DuplicateTmsTestPlanRS toDuplicateTmsTestPlanRS(TmsTestPlan testPlan);

  public DuplicateTmsTestPlanRS buildDuplicateTestPlanResponse(TmsTestPlan testPlan,
      BatchTestCaseOperationResultRS duplicationStatistic) {
    var response = toDuplicateTmsTestPlanRS(testPlan);
    response.setDuplicationStatistic(duplicationStatistic);
    response.setExecutionStatistic(TmsTestPlanExecutionStatisticRS
        .builder()
        .covered(0L)
        .total(Long.valueOf(duplicationStatistic.getTotalCount()))
        .build());
    return response;
  }

  public BatchTestCaseOperationResultRS combineDuplicateTestPlanBatchResults(
      BatchTestCaseOperationResultRS duplicateTestCasesResult,
      BatchTestCaseOperationResultRS addTestCasesToPlanResult) {
    var allErrors = new ArrayList<BatchTestCaseOperationError>();
    if (duplicateTestCasesResult.getErrors() != null) {
      allErrors.addAll(duplicateTestCasesResult.getErrors());
    }
    if (addTestCasesToPlanResult.getErrors() != null) {
      allErrors.addAll(addTestCasesToPlanResult.getErrors());
    }

    var result = new BatchTestCaseOperationResultRS();
    result.setSuccessCount(
        addTestCasesToPlanResult.getSuccessCount()); // Only truly successful are those added to plan
    result.setFailureCount(duplicateTestCasesResult.getFailureCount() + addTestCasesToPlanResult.getFailureCount());
    result.setErrors(allErrors);
    result.setTotalCount(duplicateTestCasesResult.getTotalCount());
    return result;
  }

  public BatchTestCaseOperationResultRS createFailedBatchResult(List<Long> failedIds,
      String errorMessage) {
    var errors = failedIds
        .stream()
        .map(id -> new BatchTestCaseOperationError(id, errorMessage))
        .toList();

    var result = new BatchTestCaseOperationResultRS();
    result.setSuccessCount(0);
    result.setFailureCount(failedIds.size());
    result.setErrors(errors);
    result.setTotalCount(failedIds.size());
    return result;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "launches", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "project", source = "project")
  @Mapping(target = "environment", source = "environment")
  @Mapping(target = "productVersion", source = "productVersion")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "milestone", source = "milestone")
  public abstract TmsTestPlan duplicateTestPlan(TmsTestPlan originalTestPlan);
}
