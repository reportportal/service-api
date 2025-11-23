package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.dto.TmsManualLaunchAttributeRQ;
import com.epam.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsManualLaunchAttribute;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestPlanAttribute;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for TMS Manual Launch Attribute.
 */
@Mapper(config = CommonMapperConfig.class)
public interface TmsManualLaunchAttributeMapper {

  List<TmsManualLaunchAttribute> convertToManualLaunchAttributes(Collection<TmsManualLaunchAttributeRQ> attributes);

  @Mapping(target = "attribute.id", source = "attribute.id")
  @Mapping(target = "id.attributeId", source = "attribute.id")
  @Mapping(target = "value", source = "attribute.value")
  TmsManualLaunchAttribute convertToTmsManualLaunchAttribute(
      TmsManualLaunchAttributeRQ attribute);
}
