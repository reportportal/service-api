package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsProductVersionMapper implements DtoMapper<TmsProductVersion,
                                                                   TmsProductVersionRS> {

    @Mapping(target = "id", source = "tmsProductVersionId")
    public abstract TmsProductVersion convertToTmsProductVersion(Long tmsProductVersionId);

}
