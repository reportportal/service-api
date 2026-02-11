package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsManualScenarioMapper implements DtoMapper<TmsManualScenario, TmsManualScenarioRS> {

  @Autowired
  private TmsTextManualScenarioMapper tmsTextManualScenarioMapper;

  @Autowired
  private TmsStepsManualScenarioMapper tmsStepsManualScenarioMapper;

  @Override
  public TmsManualScenarioRS convert(TmsManualScenario tmsManualScenario) {
    if (tmsManualScenario == null) {
      return null;
    }

    return switch (tmsManualScenario.getType()) {
      case TEXT -> tmsTextManualScenarioMapper.convert(tmsManualScenario);
      case STEPS -> tmsStepsManualScenarioMapper.convert(tmsManualScenario);
    };
  }

  @Mapping(target = "executionEstimationTime", source = "executionEstimationTime")
  @Mapping(target = "linkToRequirements", source = "linkToRequirements")
  @Mapping(target = "type", source = "manualScenarioType")
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  public abstract TmsManualScenario createTmsManualScenario(TmsManualScenarioRQ manualScenarioRQ);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCaseVersion", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  public abstract void update(@MappingTarget TmsManualScenario target, TmsManualScenario source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testCaseVersion", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  public abstract void patch(@MappingTarget TmsManualScenario target, TmsManualScenario source);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "textScenario", ignore = true)
  @Mapping(target = "stepsScenario", ignore = true)
  @Mapping(target = "preconditions", ignore = true)
  @Mapping(target = "executionEstimationTime", source = "originalScenario.executionEstimationTime")
  @Mapping(target = "linkToRequirements", source = "originalScenario.linkToRequirements")
  @Mapping(target = "type", source = "originalScenario.type")
  @Mapping(target = "testCaseVersion", source = "newVersion")
  public abstract TmsManualScenario duplicateManualScenario(TmsManualScenario originalScenario,
      TmsTestCaseVersion newVersion);
}
