package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioAttributeId;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttributeRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsManualScenarioAttributeMapper {

  @Mapping(target = "id", source = "tmsManualScenarioAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsManualScenarioAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsManualScenarioAttribute.attribute.value")
  TmsManualScenarioAttributeRS convertManualScenarioAttributeRS(
      TmsManualScenarioAttribute tmsManualScenarioAttribute);

  default TmsManualScenarioAttribute duplicateAttribute(
      TmsManualScenarioAttribute originalAttribute,
      TmsManualScenario newScenario) {

    var duplicatedAttribute = new TmsManualScenarioAttribute();

    var newAttributeId = new TmsManualScenarioAttributeId();
    newAttributeId.setManualScenarioId(newScenario.getId());
    newAttributeId.setAttributeId(originalAttribute.getAttribute().getId());

    duplicatedAttribute.setId(newAttributeId);
    duplicatedAttribute.setManualScenario(newScenario);
    duplicatedAttribute.setAttribute(
        originalAttribute.getAttribute()); // using same TmsAttribute

    return duplicatedAttribute;
  }

  default TmsManualScenarioAttribute createManualScenarioAttribute(TmsManualScenario scenario, TmsAttribute attribute) {
    var entity = new TmsManualScenarioAttribute();
    var id = new TmsManualScenarioAttributeId();
    id.setManualScenarioId(scenario.getId());
    id.setAttributeId(attribute.getId());
    entity.setId(id);
    entity.setManualScenario(scenario);
    entity.setAttribute(attribute);
    return entity;
  }
}
