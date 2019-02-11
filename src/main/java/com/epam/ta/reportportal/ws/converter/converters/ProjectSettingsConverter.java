/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public final class ProjectSettingsConverter {

	private ProjectSettingsConverter() {
		//static only
	}

	public static final Function<ProjectInfo, ProjectInfoResource> TO_PROJECT_INFO_RESOURCE = project -> {
		Preconditions.checkNotNull(project);
		ProjectInfoResource resource = new ProjectInfoResource();
		resource.setUsersQuantity(project.getUsersQuantity());
		resource.setLaunchesQuantity(project.getLaunchesQuantity());
		resource.setProjectId(project.getId());
		resource.setProjectName(project.getName());
		resource.setCreationDate(EntityUtils.TO_DATE.apply(project.getCreationDate()));
		resource.setLastRun(ofNullable(project.getLastRun()).map(EntityUtils.TO_DATE).orElse(null));
		resource.setEntryType(project.getProjectType());
		resource.setOrganization(project.getOrganization());
		return resource;
	};

	public static final Function<IssueType, IssueSubTypeResource> TO_SUBTYPE_RESOURCE = issueType -> {
		IssueSubTypeResource issueSubTypeResource = new IssueSubTypeResource();
		issueSubTypeResource.setId(issueType.getId());
		issueSubTypeResource.setLocator(issueType.getLocator());
		issueSubTypeResource.setColor(issueType.getHexColor());
		issueSubTypeResource.setLongName(issueType.getLongName());
		issueSubTypeResource.setShortName(issueType.getShortName());
		issueSubTypeResource.setTypeRef(issueType.getIssueGroup().getTestItemIssueGroup().getValue());
		return issueSubTypeResource;
	};

	public static final Function<List<IssueType>, Map<String, List<IssueSubTypeResource>>> TO_PROJECT_SUB_TYPES_RESOURCE = issueTypes -> issueTypes
			.stream()
			.collect(Collectors.groupingBy(
					it -> it.getIssueGroup().getTestItemIssueGroup().getValue(),
					Collectors.mapping(TO_SUBTYPE_RESOURCE, Collectors.toList())
			));

	public static final Function<Project, ProjectSettingsResource> TO_PROJECT_SETTINGS_RESOURCE = project -> {
		ProjectSettingsResource resource = new ProjectSettingsResource();
		resource.setProjectId(project.getId());
		resource.setSubTypes(TO_PROJECT_SUB_TYPES_RESOURCE.apply(project.getProjectIssueTypes()
				.stream()
				.map(ProjectIssueType::getIssueType)
				.collect(Collectors.toList())));
		return resource;
	};

}
