package com.epam.ta.reportportal.core.tms.mapper;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.mapper.config.CommonMapperConfig;
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
