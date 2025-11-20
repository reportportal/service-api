package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseAttributeId;
import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeRS;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsTestCaseAttributeMapper {

  Set<TmsTestCaseAttribute> convertToTmsTestCaseAttributes(
      List<TmsTestCaseAttributeRQ> attributes);

  @Mapping(target = "attribute.id", source = "tmsTestTestAttributeRQ.id")
  @Mapping(target = "id.attributeId", source = "tmsTestTestAttributeRQ.id")
  TmsTestCaseAttribute convertTmsTestCaseAttribute(
      TmsTestCaseAttributeRQ tmsTestTestAttributeRQ);

  @Mapping(target = "attribute.id", source = "attributeId")
  @Mapping(target = "testCase.id", source = "testCaseId")
  @Mapping(target = "id.attributeId", source = "attributeId")
  @Mapping(target = "id.testCaseId", source = "testCaseId")
  TmsTestCaseAttribute createTestCaseAttribute(Long testCaseId, Long attributeId);

  @Mapping(target = "id", source = "tmsTestCaseAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsTestCaseAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsTestCaseAttribute.value")
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
    duplicatedAttribute.setValue(originalAttribute.getValue());

    return duplicatedAttribute;
  }
}
