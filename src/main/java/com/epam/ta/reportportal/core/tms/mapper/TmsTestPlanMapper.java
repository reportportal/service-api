package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationError;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationResultRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanWithStatistic;
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

@Mapper(config = CommonMapperConfig.class, uses = TmsTestPlanAttributeMapper.class)
public abstract class TmsTestPlanMapper {

  @Mapping(target = "attributes", source = "attributes")
  public abstract TmsTestPlanRS convertToRS(TmsTestPlan tmsTestPlan);

  @Mapping(target = "id", source = "tmsTestPlan.testPlan.id")
  @Mapping(target = "name", source = "tmsTestPlan.testPlan.name")
  @Mapping(target = "description", source = "tmsTestPlan.testPlan.description")
  @Mapping(target = "attributes", source = "tmsTestPlan.testPlan.attributes")
  @Mapping(target = "executionStatistic", source = "tmsTestPlan.executionStatistic")
  public abstract TmsTestPlanRS convertTmsTestPlanWithStatisticToRS(TmsTestPlanWithStatistic tmsTestPlan);

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

  /**
   * Duplicates a test plan with "-copy" suffix added to the name.
   *
   * @param originalTestPlan the original test plan to duplicate
   * @return the duplicated test plan entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", expression = "java(originalTestPlan.getName() + \"-copy\")")
  @Mapping(target = "description", source = "originalTestPlan.description")
  @Mapping(target = "project", source = "originalTestPlan.project")
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  public abstract TmsTestPlan duplicateTestPlan(TmsTestPlan originalTestPlan);

  public Page<TmsTestPlanRS> convertToRS(Page<TmsTestPlan> testPlansByCriteria) {
    var content = testPlansByCriteria.map(this::convertToRS).getContent();
    return new PageImpl<>(content, testPlansByCriteria.getPageable(),
        testPlansByCriteria.getTotalElements());
  }

  public BatchOperationResultRS convertToRS(int totalCount, int successCount,
      List<BatchOperationError> errors) {
    return BatchOperationResultRS.builder()
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
}