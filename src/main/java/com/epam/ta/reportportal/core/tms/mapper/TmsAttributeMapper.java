package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.entity.tms.TmsAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CommonMapperConfig.class)
public interface TmsAttributeMapper {

  @Mapping(target = "id", ignore = true)
  TmsAttribute convertToTmsAttribute(TmsAttributeRQ request);

  TmsAttributeRS convertToTmsAttributeRS(TmsAttribute entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "id", ignore = true)
  void patch(@MappingTarget TmsAttribute entity, TmsAttributeRQ request);
}
