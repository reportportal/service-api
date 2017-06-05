package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSpecific;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.project.*;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Function;

public final class ProjectConverter {
    private ProjectConverter() {
        //static only
    }
    
    public static final Function<CreateProjectRQ, Project> TO_MODEL = request -> {
        Project project = new Project();
        if (Optional.ofNullable(request).isPresent()) {
            project.setName(request.getProjectName().trim());
            project.setCreationDate(new Date());
            project.getConfiguration().setEntryType(EntryType.findByName(request.getEntryType())
                    .orElse(null));
            if (null != request.getCustomer())
                project.setCustomer(request.getCustomer().trim());
            if (null != request.getAddInfo())
                project.setAddInfo(request.getAddInfo().trim());

            // Empty fields creation by default
            project.getConfiguration().setExternalSystem(new ArrayList<>());
            project.getConfiguration().setProjectSpecific(ProjectSpecific.DEFAULT);
            project.getConfiguration().setInterruptJobTime(InterruptionJobDelay.ONE_DAY.getValue());
            project.getConfiguration().setKeepLogs(KeepLogsDelay.THREE_MONTHS.getValue());
            project.getConfiguration().setKeepScreenshots(KeepScreenshotsDelay.TWO_WEEKS.getValue());
            project.getConfiguration().setIsAutoAnalyzerEnabled(false);
            project.getConfiguration().setStatisticsCalculationStrategy(StatisticsCalculationStrategy.STEP_BASED);

            // Email settings by default
            ProjectUtils.setDefaultEmailCofiguration(project);

            // Users
            project.setUsers(Maps.newHashMap());
        }
        return project;
    };
    
    public static final Function<Project, ProjectResource> TO_RESOURCE = model -> {
        ProjectResource resource = new ProjectResource();
        resource.setProjectId(model.getId());
        resource.setCustomer(model.getCustomer());
        resource.setAddInfo(model.getAddInfo());
        resource.setCreationDate(model.getCreationDate());

        Map<String, ProjectResource.ProjectUser> users = new HashMap<>();
        Map<String, Project.UserConfig> actualUsers = model.getUsers();
        for (Map.Entry<String, Project.UserConfig> user : actualUsers.entrySet()) {
            ProjectResource.ProjectUser one = new ProjectResource.ProjectUser();
            one.setProjectRole(user.getValue().getProjectRole().name());
            one.setProposedRole(user.getValue().getProposedRole().name());
            users.put(user.getKey(), one);
        }
        resource.setUsers(users);

        // TODO remove NULL validators after DB stabilizing
        if (null != model.getConfiguration()) {
            ProjectConfiguration configuration = new ProjectConfiguration();

            if (null != model.getConfiguration().getEntryType())
                configuration.setEntry(model.getConfiguration().getEntryType().name());
            if (null != model.getConfiguration().getProjectSpecific())
                configuration.setProjectSpecific(model.getConfiguration().getProjectSpecific().name());
            if (null != model.getConfiguration().getKeepLogs())
                configuration.setKeepLogs(model.getConfiguration().getKeepLogs());
            if (null != model.getConfiguration().getInterruptJobTime())
                configuration.setInterruptJobTime(model.getConfiguration().getInterruptJobTime());
            if (null != model.getConfiguration().getKeepScreenshots())
                configuration.setKeepScreenshots(model.getConfiguration().getKeepScreenshots());
            if (null != model.getConfiguration().getIsAutoAnalyzerEnabled())
                configuration.setIsAAEnabled(model.getConfiguration().getIsAutoAnalyzerEnabled());
            if (null != model.getConfiguration().getStatisticsCalculationStrategy())
                configuration.setStatisticCalculationStrategy(model.getConfiguration().getStatisticsCalculationStrategy().name());

            // =============== EMAIL settings ===================
            configuration.setEmailConfig(EmailConfigConverters
                    .TO_RESOURCE.apply(model.getConfiguration().getEmailConfig()));
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            // ============= External sub-types =================
            if (null != model.getConfiguration().getSubTypes()) {
                Map<String, List<IssueSubTypeResource>> result = new HashMap<>();
                model.getConfiguration().getSubTypes().forEach((k, v) -> {
                    List<IssueSubTypeResource> subTypeResources = Lists.newArrayList();
                    v.forEach(subType -> subTypeResources.add(new IssueSubTypeResource(subType.getLocator(), subType.getTypeRef(), subType.getLongName(),
                            subType.getShortName(), subType.getHexColor())));
                    result.put(k.getValue(), subTypeResources);
                });
                configuration.setSubTypes(result);
            }
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            resource.setConfiguration(configuration);
        }
        return resource;
    };
}
