package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenarioAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface TmsManualScenarioAttributeMapper {

  Set<TmsManualScenarioAttribute> convertToTmsManualScenarioAttributes(List<TmsManualScenarioAttributeRQ> attributes);

  @Mapping(target = "attribute.id", source = "tmsTestTestAttributeRQ.attributeId")
  @Mapping(target = "id.attributeId", source = "tmsTestTestAttributeRQ.attributeId")
  TmsManualScenarioAttribute convertManualScenarioAttribute(
      TmsManualScenarioAttributeRQ tmsTestTestAttributeRQ);
}
