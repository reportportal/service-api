package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestPlanAttributeMapper {

    public abstract Set<TmsTestPlanAttribute> convertToTmsTestPlanAttributes(
        List<TmsTestPlanAttributeRQ> tmsTestPlanAttributeRQList);

    @Mapping(target = "attribute.id", source = "tmsTestPlanAttributeRQ.attributeId")
    @Mapping(target = "id.attributeId", source = "tmsTestPlanAttributeRQ.attributeId")
    public abstract TmsTestPlanAttribute convertToTmsTestPlanAttribute(
        TmsTestPlanAttributeRQ tmsTestPlanAttributeRQ);
}
