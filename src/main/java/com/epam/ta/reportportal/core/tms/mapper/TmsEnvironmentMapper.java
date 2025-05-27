package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsEnvironment;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsEnvironmentMapper {

  @Mapping(target = "id", source = "tmsEnvironmentId")
  public abstract TmsEnvironment convertToTmsEnvironment(Long tmsEnvironmentId);
}
