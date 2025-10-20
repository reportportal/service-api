package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanExecutionStatisticRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanWithStatistic;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
  public abstract TmsTestPlanRS convertToRS(TmsTestPlan tmsTestPlan);

  @Mapping(target = "id", source = "tmsTestPlan.testPlan.id")
  @Mapping(target = "name", source = "tmsTestPlan.testPlan.name")
  @Mapping(target = "description", source = "tmsTestPlan.testPlan.description")
  @Mapping(target = "attributes", source = "tmsTestPlan.testPlan.attributes")
  @Mapping(target = "executionStatistic", source = "tmsTestPlan.executionStatistic")
  public abstract TmsTestPlanRS convertTmsTestPlanWithStatisticToRS(
      TmsTestPlanWithStatistic tmsTestPlan);

  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "attributes", ignore = true)
  public abstract TmsTestPlan convertFromRQ(Long projectId, TmsTestPlanRQ testPlanRQ);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  public abstract void update(@MappingTarget TmsTestPlan targetTestPlan, TmsTestPlan tmsTestPlan);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  public abstract void patch(@MappingTarget TmsTestPlan existingTestPlan,
      TmsTestPlan tmsTestPlan);

  public Page<TmsTestPlanRS> convertToRS(Page<TmsTestPlan> testPlansByCriteria) {
    var content = testPlansByCriteria.map(this::convertToRS).getContent();
    return new PageImpl<>(content, testPlansByCriteria.getPageable(),
        testPlansByCriteria.getTotalElements());
  }

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
            .collect(Collectors.toList()),
        pageable,
        totalCount
    );
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "searchVector", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "launches", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  public abstract TmsTestPlan duplicateTestPlan(TmsTestPlan originalTestPlan);

  @Mapping(target = "duplicationStatistic", ignore = true)
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
        .collect(Collectors.toList());

    var result = new BatchTestCaseOperationResultRS();
    result.setSuccessCount(0);
    result.setFailureCount(failedIds.size());
    result.setErrors(errors);
    result.setTotalCount(failedIds.size());
    return result;
  }
}
