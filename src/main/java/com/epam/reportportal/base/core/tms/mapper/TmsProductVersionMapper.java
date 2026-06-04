package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsProductVersion;
import com.epam.reportportal.base.core.tms.dto.TmsProductVersionRS;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsProductVersionMapper implements DtoMapper<TmsProductVersion,
    TmsProductVersionRS> {

  @Mapping(target = "id", source = "tmsProductVersionId")
  public abstract TmsProductVersion convertToTmsProductVersion(Long tmsProductVersionId);

}
