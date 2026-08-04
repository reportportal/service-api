package com.epam.reportportal.base.core.tms.sync.mapper;

import com.epam.reportportal.base.core.tms.sync.dto.TmsSyncJobRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsSyncJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TmsSyncJobMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "integrationId", source = "integration.id")
    TmsSyncJobRS toTmsSyncJobRS(TmsSyncJob job);
}
