package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlanAttributeId;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanAttributeRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestPlanAttributeMapper {

  @Mapping(target = "id", source = "tmsTestPlanAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsTestPlanAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsTestPlanAttribute.attribute.value")
  public abstract TmsTestPlanAttributeRS convertTmsPlanAttributeRS(
      TmsTestPlanAttribute tmsTestPlanAttribute);

  public abstract List<TmsTestPlanAttributeRS> convertTmsPlanAttributeRSList(Set<TmsTestPlanAttribute> attributes);

  @Mapping(target = "id.testPlanId", ignore = true)
  @Mapping(target = "id.attributeId", source = "originalAttribute.id.attributeId")
  @Mapping(target = "attribute", source = "originalAttribute.attribute")
  @Mapping(target = "testPlan", source = "newTestPlan")
  public abstract TmsTestPlanAttribute duplicateTestPlanAttribute(
      TmsTestPlanAttribute originalAttribute, 
      TmsTestPlan newTestPlan);

  public TmsTestPlanAttribute createTestPlanAttribute(TmsTestPlan testPlan, TmsAttribute attribute) {
    var entity = new TmsTestPlanAttribute();
    var id = new TmsTestPlanAttributeId();
    id.setTestPlanId(testPlan.getId());
    id.setAttributeId(attribute.getId());
    entity.setId(id);
    entity.setTestPlan(testPlan);
    entity.setAttribute(attribute);
    return entity;
  }
}
