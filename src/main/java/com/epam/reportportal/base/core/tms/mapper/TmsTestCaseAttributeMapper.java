package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseAttributeId;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseAttributeMapper {

  @Mapping(target = "id", source = "tmsTestCaseAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsTestCaseAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsTestCaseAttribute.attribute.value")
  TmsTestCaseAttributeRS convertToTmsTestCaseAttributeRS(TmsTestCaseAttribute tmsTestCaseAttribute);

  default TmsTestCaseAttribute duplicateTestCaseAttribute(TmsTestCaseAttribute originalAttribute,
      TmsTestCase newTestCase) {
    var duplicatedAttribute = new TmsTestCaseAttribute();

    var newAttributeId = new TmsTestCaseAttributeId();
    newAttributeId.setTestCaseId(newTestCase.getId());
    newAttributeId.setAttributeId(originalAttribute.getAttribute().getId());

    duplicatedAttribute.setId(newAttributeId);
    duplicatedAttribute.setTestCase(newTestCase);
    duplicatedAttribute.setAttribute(
        originalAttribute.getAttribute()); // use same TmsAttribute

    return duplicatedAttribute;
  }

  default TmsTestCaseAttribute createTestCaseAttribute(TmsTestCase testCase, TmsAttribute attribute) {
    var entity = new TmsTestCaseAttribute();
    var id = new TmsTestCaseAttributeId();
    id.setTestCaseId(testCase.getId());
    id.setAttributeId(attribute.getId());
    entity.setId(id);
    entity.setTestCase(testCase);
    entity.setAttribute(attribute);
    return entity;
  }

  @Mapping(target = "attribute.id", source = "attributeId")
  @Mapping(target = "testCase.id", source = "testCaseId")
  @Mapping(target = "id.attributeId", source = "attributeId")
  @Mapping(target = "id.testCaseId", source = "testCaseId")
  TmsTestCaseAttribute createTestCaseAttribute(Long testCaseId, Long attributeId);
}
