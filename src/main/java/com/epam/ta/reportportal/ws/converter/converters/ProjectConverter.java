/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.core.analyzer.auto.indexer.IndexerStatusCache;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.converters.ProjectSettingsConverter.TO_SUBTYPE_RESOURCE;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service
public final class ProjectConverter {

	private final static String INDEXING_RUN = "analyzer.indexingRunning";

	@Autowired
	private IndexerStatusCache indexerStatusCache;

	public Function<Project, ProjectResource> TO_PROJECT_RESOURCE = project -> {
		if (project == null) {
			return null;
		}

		ProjectResource projectResource = new ProjectResource();
		projectResource.setProjectId(project.getId());
		projectResource.setProjectName(project.getName());
		projectResource.setEntryType(project.getProjectType().name());
		projectResource.setCreationDate(project.getCreationDate());
		projectResource.setAllocatedStorage(project.getAllocatedStorage());
		projectResource.setUsers(project.getUsers().stream().map(user -> {
			ProjectResource.ProjectUser projectUser = new ProjectResource.ProjectUser();
			projectUser.setLogin(user.getUser().getLogin());
			projectUser.setProjectRole(user.getProjectRole().toString());
			return projectUser;
		}).collect(Collectors.toList()));

		Map<String, List<IssueSubTypeResource>> subTypes = project.getProjectIssueTypes()
				.stream()
				.map(ProjectIssueType::getIssueType)
				.collect(Collectors.groupingBy(
						it -> it.getIssueGroup().getTestItemIssueGroup().getValue(),
						Collectors.mapping(TO_SUBTYPE_RESOURCE, Collectors.toList())
				));

		ProjectConfiguration projectConfiguration = new ProjectConfiguration();

		Map<String, String> attributes = ProjectUtils.getConfigParameters(project.getProjectAttributes());

		attributes.put(
				INDEXING_RUN,
				String.valueOf(ofNullable(indexerStatusCache.getIndexingStatus().getIfPresent(project.getId())).orElse(false))
		);

		projectConfiguration.setProjectAttributes(attributes);

		projectConfiguration.setPatterns(project.getPatternTemplates()
				.stream()
				.map(PatternTemplateConverter.TO_RESOURCE)
				.collect(Collectors.toList()));

		projectResource.setIntegrations(project.getIntegrations()
				.stream()
				.map(IntegrationConverter.TO_INTEGRATION_RESOURCE)
				.collect(Collectors.toList()));

		ProjectNotificationConfigDTO notificationConfig = new ProjectNotificationConfigDTO();
		notificationConfig.setEnabled(BooleanUtils.toBoolean(attributes.get(ProjectAttributeEnum.NOTIFICATIONS_ENABLED.getAttribute())));

		ofNullable(project.getSenderCases()).ifPresent(senderCases -> notificationConfig.setSenderCases(NotificationConfigConverter.TO_RESOURCE
				.apply(senderCases)));
		projectConfiguration.setProjectConfig(notificationConfig);

		projectConfiguration.setSubTypes(subTypes);

		projectResource.setConfiguration(projectConfiguration);
		projectResource.setOrganization(project.getOrganization());
		return projectResource;
	};

}
