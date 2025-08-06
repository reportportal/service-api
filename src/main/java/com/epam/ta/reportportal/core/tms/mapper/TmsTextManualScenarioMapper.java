package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTextManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRS;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
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
  @Mapping(target = "linkToRequirements", source = "linkToRequirements")
  @Mapping(target = "preconditions", source = "preconditions")
  @Mapping(target = "manualScenarioType", source = "type")
  @Mapping(target = "attributes", source = "attributes")
  @Mapping(target = "instructions", source = "textScenario.instructions")
  @Mapping(target = "expectedResult", source = "textScenario.expectedResult")
  TmsTextManualScenarioRS convert(TmsManualScenario tmsManualScenario);

  @Mapping(target = "value", source = "value")
  TmsManualScenarioPreconditionsRS convertToTmsManualScenarioPreconditionsRS(String value);
}
