package com.epam.reportportal.base.core.tms.mapper;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.core.tms.mapper.config.CommonMapperConfig;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class TmsMilestoneMapper {

  public abstract Set<TmsMilestone> convertToTmsMilestones(List<Long> milestoneIds);

  @Mapping(target = "id", source = "tmsMilestoneId")
  protected abstract TmsMilestone convertToTmsMilestone(Long tmsMilestoneId);
}
