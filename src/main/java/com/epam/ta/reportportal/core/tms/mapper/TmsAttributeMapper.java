package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsAttributeMapper {

  @Mapping(target = "key", source = "key")
  TmsAttribute createTmsAttribute(String key);

  default TmsAttribute getTmsAttribute(
      Map<String, TmsAttribute> tmsAttributesMap,
      TmsAttributeRQ attribute) {
    if (attribute.getId() != null) {
      return tmsAttributesMap.get("id:" + attribute.getId());
    } else if (StringUtils.isNotBlank(attribute.getKey())) {
      return tmsAttributesMap.get("key:" + attribute.getKey());
    }
    return null;
  }
}
