package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsStep;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsStepMapper {

  default Set<TmsStep> convertToTmsSteps(TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    var stepsRQs = testCaseManualScenarioRQ.getSteps();
    if (CollectionUtils.isEmpty(stepsRQs)) {
      return null;
    }
    return stepsRQs
        .stream()
        .map(this::convertToTmsStep)
        .collect(Collectors.toSet());
  }

  TmsStep convertToTmsStep(TmsTextManualScenarioRQ testCaseManualScenarioRQ);

  TmsStep convertToTmsStep(TmsManualScenarioStepRQ tmsManualScenarioStepRQ);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  void patch(@MappingTarget TmsStep target, TmsStep source);
}
