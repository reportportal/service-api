package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;

import java.util.function.Function;

public final class ProjectInfoConverter {
    private ProjectInfoConverter() {
        //static only
    }
    
    public static final Function<Project, ProjectInfoResource> TO_RESOURCE = project -> {
        ProjectInfoResource resource = new ProjectInfoResource();
        resource.setUsersQuantity(null != project.getUsers() ? project.getUsers().size() : 0);
        resource.setProjectId(project.getId());
        resource.setCreationDate(project.getCreationDate());
        String entryType = null == project.getConfiguration() ? EntryType.INTERNAL.name() : null == project.getConfiguration()
                .getEntryType() ? EntryType.INTERNAL.name() : project.getConfiguration().getEntryType().name();
        resource.setEntryType(entryType);
        return resource;
    };
}
