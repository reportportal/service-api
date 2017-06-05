package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public final class ProjectInfoConverter {

    private ProjectInfoConverter() {
        //static only
    }

    public static final Function<Project, ProjectInfoResource> TO_RESOURCE = project -> {
        ProjectInfoResource resource = new ProjectInfoResource();
        resource.setUsersQuantity(Optional.ofNullable(project.getUsers()).orElse(Collections.emptyMap()).size());
        resource.setProjectId(project.getId());
        resource.setCreationDate(project.getCreationDate());
        String entryType = Optional.ofNullable(project.getConfiguration()).isPresent()
                ? EntryType.INTERNAL.name() : Optional.ofNullable(project.getConfiguration().getEntryType()).isPresent()
                ? EntryType.INTERNAL.name() : project.getConfiguration().getEntryType().name();
        resource.setEntryType(entryType);
        return resource;
    };
}
