package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsStepsManualScenario;
import com.epam.reportportal.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.core.tms.dto.TmsStepRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

  @Mapping(target = "attachments", ignore = true)
  TmsStep convertToTmsStep(TmsStepRQ tmsStepRQ);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "stepsManualScenario", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  void patch(@MappingTarget TmsStep target, TmsStep source);

  default List<TmsStepRS> convert(Collection<TmsStep> steps) {
    if (CollectionUtils.isEmpty(steps)) {
      return null;
    }
    return steps
        .stream()
        .sorted(Comparator.comparingInt(TmsStep::getNumber))
        .map(this::tmsStepToTmsStepRS)
        .collect(Collectors.toList());

  }

  TmsStepRS tmsStepToTmsStepRS(TmsStep tmsStep);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "instructions", source = "originalStep.instructions")
  @Mapping(target = "expectedResult", source = "originalStep.expectedResult")
  @Mapping(target = "stepsManualScenario", source = "newStepsScenario")
  TmsStep duplicateStep(TmsStep originalStep, TmsStepsManualScenario newStepsScenario);
}
