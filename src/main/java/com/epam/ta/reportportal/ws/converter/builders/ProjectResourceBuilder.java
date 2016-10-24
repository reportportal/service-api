/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.converter.builders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.ws.model.externalsystem.ExternalSystemResource;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource.ProjectUser;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.google.common.collect.Lists;

/**
 * Response {@link ProjectResource} builder for controllers
 * 
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ProjectResourceBuilder extends ResourceBuilder<ProjectResource> {

	public ProjectResourceBuilder addProject(Project prj, Iterable<ExternalSystem> systems) {
		ProjectResource resource = getObject();
		resource.setProjectId(prj.getId());
		resource.setCustomer(prj.getCustomer());
		resource.setAddInfo(prj.getAddInfo());
		resource.setCreationDate(prj.getCreationDate());

		Map<String, ProjectUser> users = new HashMap<>();
		Map<String, UserConfig> actualUsers = prj.getUsers();
		for (Map.Entry<String, UserConfig> user : actualUsers.entrySet()) {
			ProjectUser one = new ProjectUser();
			one.setProjectRole(user.getValue().getProjectRole().name());
			one.setProposedRole(user.getValue().getProposedRole().name());
			users.put(user.getKey(), one);
		}
		resource.setUsers(users);

		// TODO remove NULL validators after DB stabilizing
		if (null != prj.getConfiguration()) {
			ProjectConfiguration configuration = new ProjectConfiguration();
			List<ExternalSystemResource> externalDetails = Lists.newArrayList();

			if (null != prj.getConfiguration().getEntryType())
				configuration.setEntry(prj.getConfiguration().getEntryType().name());
			if (null != prj.getConfiguration().getProjectSpecific())
				configuration.setProjectSpecific(prj.getConfiguration().getProjectSpecific().name());
			if (null != prj.getConfiguration().getKeepLogs())
				configuration.setKeepLogs(prj.getConfiguration().getKeepLogs());
			if (null != prj.getConfiguration().getInterruptJobTime())
				configuration.setInterruptJobTime(prj.getConfiguration().getInterruptJobTime());
			if (null != prj.getConfiguration().getKeepScreenshots())
				configuration.setKeepScreenshots(prj.getConfiguration().getKeepScreenshots());
			if (null != prj.getConfiguration().getIsAutoAnalyzerEnabled())
				configuration.setIsAAEnabled(prj.getConfiguration().getIsAutoAnalyzerEnabled());
			if (null != prj.getConfiguration().getStatisticsCalculationStrategy())
				configuration.setStatisticCalculationStrategy(prj.getConfiguration().getStatisticsCalculationStrategy().name());

			// =============== EMAIL settings ===================
			configuration.setEmailConfig(prj.getConfiguration().getEmailConfig());
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

			// ============= External sub-types =================
			if (null != prj.getConfiguration().getSubTypes()) {
				Map<String, List<IssueSubTypeResource>> result = new HashMap<>();
				prj.getConfiguration().getSubTypes().forEach((k, v) -> {
					List<IssueSubTypeResource> subTypeResources = Lists.newArrayList();
					v.stream().forEach(subType -> subTypeResources.add(new IssueSubTypeResource(subType.getLocator(), subType.getTypeRef(), subType.getLongName(),
                            subType.getShortName(), subType.getHexColor())));
					result.put(k.getValue(), subTypeResources);
				});
				configuration.setSubTypes(result);
			}
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

			// ============== External Systems ==================
			for (ExternalSystem system : systems) {
				ExternalSystemResource details = new ExternalSystemResource();
				details.setSystemId(system.getId());
				details.setUrl(system.getUrl());
				details.setExternalSystemType(system.getExternalSystemType().name());
				details.setExternalSystemAuth(system.getExternalSystemAuth().name());
				details.setUsername(system.getUsername());

				if (null != system.getProject())
					details.setProject(system.getProject());
				if (null != system.getDomain())
					details.setDomain(system.getDomain());
				if (null != system.getAccessKey())
					details.setAccessKey(system.getAccessKey());

				details.setFields(system.getFields());

				externalDetails.add(details);
			}
			configuration.setExternalSystem(externalDetails);
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			resource.setConfiguration(configuration);
		}
		return this;
	}

	@Override
	protected ProjectResource initObject() {
		return new ProjectResource();
	}
}