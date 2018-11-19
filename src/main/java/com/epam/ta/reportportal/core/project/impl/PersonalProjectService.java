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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.integration.email.EmailIntegrationService;
import com.epam.ta.reportportal.dao.AttributeRepository;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.enums.ProjectType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Collections;

import static com.epam.ta.reportportal.entity.project.ProjectUtils.defaultIssueTypes;
import static com.epam.ta.reportportal.entity.project.ProjectUtils.defaultProjectAttributes;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Generates Personal project for provided user
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Service
public final class PersonalProjectService {

	public static final String PERSONAL_PROJECT_POSTFIX = "_personal";
	private final ProjectRepository projectRepository;
	private final AttributeRepository attributeRepository;
	private final IssueTypeRepository issueTypeRepository;
	private final EmailIntegrationService emailIntegrationService;

	@Autowired
	public PersonalProjectService(ProjectRepository projectRepository, AttributeRepository attributeRepository,
			IssueTypeRepository issueTypeRepository, EmailIntegrationService emailIntegrationService) {
		this.projectRepository = projectRepository;
		this.attributeRepository = attributeRepository;
		this.issueTypeRepository = issueTypeRepository;
		this.emailIntegrationService = emailIntegrationService;
	}

	/**
	 * Prefix from username with replaced dots as underscores
	 *
	 * @param username Name of user
	 * @return Corresponding personal project name
	 */
	@VisibleForTesting
	private String generatePersonalProjectName(String username) {
		String initialName = getProjectPrefix(username);

		String name = initialName;
		//iterate until we find free project name
		for (int i = 1; projectRepository.existsByName(name); i++) {
			name = initialName + "_" + i;
		}

		return name;
	}

	/**
	 * Generates personal project for provided user
	 *
	 * @param user User project should be created for
	 * @return Built Project object
	 */
	public Project generatePersonalProject(User user) {
		Project project = new Project();
		project.setName(generatePersonalProjectName(user.getLogin()));
		project.setCreationDate(Date.from(ZonedDateTime.now().toInstant()));
		project.setProjectType(ProjectType.PERSONAL);

		ProjectUser projectUser = new ProjectUser().withUser(user).withProjectRole(ProjectRole.PROJECT_MANAGER).withProject(project);
		project.setUsers(ImmutableSet.<ProjectUser>builder().add(projectUser).build());

		project.setMetadata(new Metadata(Collections.singletonMap("additional_info",
				"Personal project of " + (isNullOrEmpty(user.getFullName()) ? user.getLogin() : user.getFullName())
		)));

		project.setProjectAttributes(defaultProjectAttributes(project, attributeRepository.getDefaultProjectAttributes()));
		project.setProjectIssueTypes(defaultIssueTypes(project, issueTypeRepository.getDefaultIssueTypes()));

		/* Default email configuration */
		emailIntegrationService.setDefaultEmailConfiguration(project);

		return project;
	}

	/**
	 * Generates prefix for personal project
	 *
	 * @param username Name of user
	 * @return Prefix
	 */
	public String getProjectPrefix(String username) {
		String projectName = username.replaceAll("\\.", "_");
		return (projectName + PERSONAL_PROJECT_POSTFIX).toLowerCase();
	}
}
