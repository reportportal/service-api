package com.epam.reportportal.core.tms.mapper;

import com.epam.reportportal.core.tms.mapper.config.CommonMapperConfig;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsEnvironment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsEnvironmentMapper {

  @Mapping(target = "id", source = "tmsEnvironmentId")
  public abstract TmsEnvironment convertToTmsEnvironment(Long tmsEnvironmentId);
}
