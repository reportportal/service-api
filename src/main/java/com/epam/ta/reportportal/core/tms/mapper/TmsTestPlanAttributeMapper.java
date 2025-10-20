package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.entity.tms.TmsTestPlan;
import com.epam.ta.reportportal.entity.tms.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestPlanAttributeMapper {

  public abstract Set<TmsTestPlanAttribute> convertToTmsTestPlanAttributes(
      List<TmsTestPlanAttributeRQ> tmsTestPlanAttributeRQList);

  @Mapping(target = "attribute.id", source = "tmsTestPlanAttributeRQ.id")
  @Mapping(target = "id.attributeId", source = "tmsTestPlanAttributeRQ.id")
  public abstract TmsTestPlanAttribute convertToTmsTestPlanAttribute(
      TmsTestPlanAttributeRQ tmsTestPlanAttributeRQ);

  @Mapping(target = "id", source = "tmsTestPlanAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsTestPlanAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsTestPlanAttribute.value")
  public abstract TmsTestPlanAttributeRS convertTmsPlanAttributeRS(
      TmsTestPlanAttribute tmsTestPlanAttribute);

  @Mapping(target = "id.testPlanId", ignore = true)
  @Mapping(target = "id.attributeId", source = "originalAttribute.id.attributeId")
  @Mapping(target = "attribute", source = "originalAttribute.attribute")
  @Mapping(target = "value", source = "originalAttribute.value")
  @Mapping(target = "testPlan", source = "newTestPlan")
  public abstract TmsTestPlanAttribute duplicateTestPlanAttribute(
      TmsTestPlanAttribute originalAttribute, 
      TmsTestPlan newTestPlan);
}