package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(config = CommonMapperConfig.class, uses = {TmsEnvironmentMapper.class,
    TmsProductVersionMapper.class})
public abstract class TmsTestPlanMapper {

  public abstract TmsTestPlanRS convertToRS(TmsTestPlan tmsTestPlan);

  @Mapping(target = "project.id", source = "projectId")
  @Mapping(target = "attributes", ignore = true)
  public abstract TmsTestPlan convertFromRQ(Long projectId, TmsTestPlanRQ testPlanRQ);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL, nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "testFolders", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  public abstract void update(@MappingTarget TmsTestPlan targetTestPlan, TmsTestPlan tmsTestPlan);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "milestones", ignore = true)
  @Mapping(target = "testFolders", ignore = true)
  @Mapping(target = "testCases", ignore = true)
  public abstract void patch(@MappingTarget TmsTestPlan existingTestPlan,
      TmsTestPlan tmsTestPlan);

  public Page<TmsTestPlanRS> convertToRS(Page<TmsTestPlan> testPlansByCriteria) {
    var content = testPlansByCriteria.map(this::convertToRS).getContent();
    return new PageImpl<>(content, testPlansByCriteria.getPageable(),
        testPlansByCriteria.getTotalElements());
  }

}
