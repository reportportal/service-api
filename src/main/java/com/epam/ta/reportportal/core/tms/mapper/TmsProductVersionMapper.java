package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsProductVersion;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRS;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsProductVersionMapper implements DtoMapper<TmsProductVersion, ProductVersionRS> {

    @Mapping(target = "id", source = "tmsProductVersionId")
    public abstract TmsProductVersion convertToTmsProductVersion(Long tmsProductVersionId);
}
