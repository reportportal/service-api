package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchAttributeRQ;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for TMS Manual Launch Attribute.
 */
@Mapper(config = CommonMapperConfig.class)
public interface TmsManualLaunchAttributeMapper {

  default Set<ItemAttribute> convertToManualLaunchAttributes(Collection<TmsManualLaunchAttributeRQ> attributeRQs,
      Map<Long, TmsAttribute> tmsAttributes, Launch launch) {
    return Optional
        .ofNullable(attributeRQs)
        .orElse(Collections.emptyList())
        .stream()
        .map(attribute -> convertToTmsManualLaunchAttribute(
            attribute, tmsAttributes.get(attribute.getId()), launch))
        .collect(Collectors.toSet());
  }

  @Mapping(target = "key", source = "tmsAttribute.key")
  @Mapping(target = "value", source = "attributeRQ.value")
  @Mapping(target = "system", constant = "false")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "testItem", ignore = true)
  @Mapping(target = "launch", source = "launch")
  ItemAttribute convertToTmsManualLaunchAttribute(
      TmsManualLaunchAttributeRQ attributeRQ, TmsAttribute tmsAttribute, Launch launch);
}
