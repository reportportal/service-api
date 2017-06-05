package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ProjectSettingsConverter {

    private ProjectSettingsConverter() {
        //static only
    }

    public static final Function<Project, ProjectSettingsResource> TO_RESOURCE = settings -> {
        ProjectSettingsResource resource = new ProjectSettingsResource();
        resource.setProjectId(settings.getId());
        Map<String, List<IssueSubTypeResource>> result = settings.getConfiguration().getSubTypes()
                .entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getValue(),
                        entry -> entry.getValue().stream().map(ProjectConverter.TO_SUBTYPE_RESOURCE).collect(Collectors.toList())));
        resource.setSubTypes(result);
        resource.setStatisticsStrategy(settings.getConfiguration().getStatisticsCalculationStrategy().name());
        return resource;
    };

}
