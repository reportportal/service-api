package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestCaseAttributeMapper {

  @Autowired
  private TmsAttributeMapper tmsAttributeMapper;

  public Set<TmsTestCaseAttribute> convertToTmsTestCaseAttributes(
      Map<String, TmsAttribute> tmsAttributes,
      List<TmsAttributeRQ> attributes) {
    if (CollectionUtils.isEmpty(attributes)) {
      return Set.of();
    }

    return attributes
        .stream()
        .map(attribute -> {
          var tmsAttribute = tmsAttributeMapper.getTmsAttribute(tmsAttributes, attribute);
          if (tmsAttribute == null) {
            throw new IllegalStateException("TmsAttribute not found for: " + attribute);
          }
          return convertTmsTestCaseAttribute(tmsAttribute, attribute.getValue());
        })
        .collect(Collectors.toSet());
  }

  @Mapping(target = "attribute", source = "tmsAttribute")
  @Mapping(target = "id.attributeId", source = "tmsAttribute.id")
  @Mapping(target = "value", source = "value")
  public abstract TmsTestCaseAttribute convertTmsTestCaseAttribute(TmsAttribute tmsAttribute, String value);

  @Mapping(target = "id", source = "tmsTestCaseAttribute.id.attributeId")
  @Mapping(target = "key", source = "tmsTestCaseAttribute.attribute.key")
  @Mapping(target = "value", source = "tmsTestCaseAttribute.value")
  public abstract TmsAttributeRS convertTmsAttributeRS(TmsTestCaseAttribute tmsTestCaseAttribute);
}
