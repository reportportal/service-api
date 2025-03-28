package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsTestCaseAttributeMapper {

    public abstract Set<TmsTestCaseAttribute> convertToTmsTestCaseAttributes(
        List<TmsTestCaseAttributeRQ> attributes);

    @Mapping(target = "attribute.id", source = "tmsTestTestAttributeRQ.attributeId")
    @Mapping(target = "id.attributeId", source = "tmsTestTestAttributeRQ.attributeId")
    public abstract TmsTestCaseAttribute convertTmsTestCaseAttribute(TmsTestCaseAttributeRQ tmsTestTestAttributeRQ);
}
