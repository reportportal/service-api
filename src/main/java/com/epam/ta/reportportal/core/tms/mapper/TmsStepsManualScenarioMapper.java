package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRS;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class, uses = {TmsStepMapper.class, TmsManualScenarioAttributeMapper.class})
public interface TmsStepsManualScenarioMapper {

  default TmsStepsManualScenario createTmsStepsManualScenario() {
    return TmsStepsManualScenario.builder().build();
  }

  @Mapping(target = "id", source = "id")
  @Mapping(target = "executionEstimationTime", source = "executionEstimationTime")
  @Mapping(target = "linkToRequirements", source = "linkToRequirements")
  @Mapping(target = "preconditions", source = "preconditions")
  @Mapping(target = "manualScenarioType", source = "type")
  @Mapping(target = "tags", source = "attributes",
      conditionExpression = "java(tmsManualScenario.getAttributes() != null "
          + "&& !tmsManualScenario.getAttributes().isEmpty())")
  @Mapping(target = "steps", source = "stepsScenario.steps")
  TmsStepsManualScenarioRS convert(TmsManualScenario tmsManualScenario);

  @Mapping(target = "value", source = "value")
  TmsManualScenarioPreconditionsRS convertToTmsManualScenarioPreconditionsRS(String value);
}
