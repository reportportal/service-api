package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepsManualScenario;
import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class, uses = {TmsStepMapper.class,
    TmsManualScenarioAttributeMapper.class, TmsManualScenarioRequirementMapper.class})
public interface TmsStepsManualScenarioMapper {

  default TmsStepsManualScenario createTmsStepsManualScenario() {
    return TmsStepsManualScenario.builder().build();
  }

  default TmsStepsManualScenario createTmsStepsManualScenario(TmsManualScenario newScenario) {
    var tmsStepsManualScenario = createTmsStepsManualScenario();
    tmsStepsManualScenario.setManualScenario(newScenario);
    return tmsStepsManualScenario;
  }

  @Mapping(target = "id", source = "id")
  @Mapping(target = "executionEstimationTime", source = "executionEstimationTime")
  @Mapping(target = "requirements", source = "requirements", defaultExpression = "java(java.util.List.of())")
  @Mapping(target = "preconditions", source = "preconditions")
  @Mapping(target = "manualScenarioType", source = "type")
  @Mapping(target = "attributes", source = "attributes")
  @Mapping(target = "steps", source = "stepsScenario.steps")
  TmsStepsManualScenarioRS convert(TmsManualScenario tmsManualScenario);
}
