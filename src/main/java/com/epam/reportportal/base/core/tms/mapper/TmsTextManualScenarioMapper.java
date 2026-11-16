package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTextManualScenario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class, uses = {
    TmsManualScenarioAttributeMapper.class,
    TmsManualScenarioRequirementMapper.class,
    TmsManualScenarioAttachmentMapper.class
})
public interface TmsTextManualScenarioMapper {

  @Mapping(target = "manualScenarioId", ignore = true)
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  TmsTextManualScenario createTmsManualScenario(TmsTextManualScenarioRQ manualScenarioRQ);

  @Mapping(target = "manualScenarioId", ignore = true)
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy =
      NullValuePropertyMappingStrategy.SET_TO_NULL,
      nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION
  )
  void updateTmsManualScenario(
      @MappingTarget TmsTextManualScenario target,
      TmsTextManualScenarioRQ testCaseManualScenarioRQ
  );

  @Mapping(target = "manualScenarioId", ignore = true)
  @Mapping(target = "manualScenario", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  void patchTmsManualScenario(
      @MappingTarget TmsTextManualScenario target,
      TmsTextManualScenarioRQ testCaseManualScenarioRQ
  );

  @Mapping(target = "id", source = "id")
  @Mapping(target = "executionEstimationTime", source = "executionEstimationTime")
  @Mapping(target = "requirements", source = "requirements", defaultExpression = "java(java.util.List.of())")
  @Mapping(target = "preconditions", source = "preconditions")
  @Mapping(target = "manualScenarioType", source = "type")
  @Mapping(target = "attributes", source = "attributes")
  @Mapping(target = "instructions", source = "textScenario.instructions")
  @Mapping(target = "expectedResult", source = "textScenario.expectedResult")
  @Mapping(target = "attachments", source = "textScenario.attachments")
  TmsTextManualScenarioRS convert(TmsManualScenario tmsManualScenario);

  @Mapping(target = "manualScenarioId", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "instructions", source = "originalTextScenario.instructions")
  @Mapping(target = "expectedResult", source = "originalTextScenario.expectedResult")
  @Mapping(target = "manualScenario", source = "newScenario")
  TmsTextManualScenario duplicateTextScenario(TmsManualScenario newScenario,
      TmsTextManualScenario originalTextScenario);
}
