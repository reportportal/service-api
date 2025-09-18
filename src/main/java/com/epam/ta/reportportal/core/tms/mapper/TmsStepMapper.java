package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsStep;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsStepRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Collection;
import java.util.List;
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

  List<TmsStepRS> convert(Collection<TmsStep> steps);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "instructions", source = "originalStep.instructions")
  @Mapping(target = "expectedResult", source = "originalStep.expectedResult")
  @Mapping(target = "stepsManualScenario", source = "newStepsScenario")
  TmsStep duplicateStep(TmsStep originalStep, TmsStepsManualScenario newStepsScenario);
}
