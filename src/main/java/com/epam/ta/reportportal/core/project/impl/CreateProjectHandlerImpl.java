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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.reportportal.extension.event.ProjectEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.project.CreateProjectHandler;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.ProjectType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateProjectHandlerImpl implements CreateProjectHandler {

	private static final String CREATE_KEY = "create";
	private static final String RESERVED_PROJECT_NAME = "project";

	private final PersonalProjectService personalProjectService;

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final AttributeRepository attributeRepository;

	private final IssueTypeRepository issueTypeRepository;

	private final ApplicationEventPublisher applicationEventPublisher;

	private final ProjectUserRepository projectUserRepository;

	@Autowired
	public CreateProjectHandlerImpl(PersonalProjectService personalProjectService, ProjectRepository projectRepository, UserRepository userRepository,
			AttributeRepository attributeRepository, IssueTypeRepository issueTypeRepository, ApplicationEventPublisher applicationEventPublisher,
			ProjectUserRepository projectUserRepository) {
		this.personalProjectService = personalProjectService;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.attributeRepository = attributeRepository;
		this.issueTypeRepository = issueTypeRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.projectUserRepository = projectUserRepository;
	}

	@Override
	public EntryCreatedRS createProject(CreateProjectRQ createProjectRQ, ReportPortalUser user) {
		String projectName = createProjectRQ.getProjectName().toLowerCase().trim();

		expect(projectName, not(equalTo(RESERVED_PROJECT_NAME))).verify(ErrorType.INCORRECT_REQUEST,
				Suppliers.formattedSupplier("Project with name '{}' is reserved by system", projectName)
		);

		expect(projectName, com.epam.ta.reportportal.util.Predicates.SPECIAL_CHARS_ONLY.negate()).verify(ErrorType.INCORRECT_REQUEST,
				Suppliers.formattedSupplier("Project name '{}' consists only of special characters", projectName)
		);

		Optional<Project> existProject = projectRepository.findByName(projectName);
		expect(existProject, not(isPresent())).verify(ErrorType.PROJECT_ALREADY_EXISTS, projectName);

		ProjectType projectType = ProjectType.findByName(createProjectRQ.getEntryType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, createProjectRQ.getEntryType()));
		expect(projectType, equalTo(ProjectType.INTERNAL)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Only internal projects can be created via API"
		);

		User dbUser = userRepository.findRawById(user.getUserId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername()));

		Project project = new Project();
		project.setName(projectName);
		project.setCreationDate(new Date());

		project.setProjectIssueTypes(ProjectUtils.defaultIssueTypes(project, issueTypeRepository.getDefaultIssueTypes()));
		Set<ProjectAttribute> projectAttributes = ProjectUtils.defaultProjectAttributes(project,
				attributeRepository.getDefaultProjectAttributes()
		);

		project.setProjectType(projectType);

		project.setProjectAttributes(projectAttributes);

		ProjectUser projectUser = new ProjectUser().withProject(project).withUser(dbUser).withProjectRole(ProjectRole.PROJECT_MANAGER);

		projectRepository.save(project);
		projectUserRepository.save(projectUser);

		applicationEventPublisher.publishEvent(new ProjectEvent(project.getId(), CREATE_KEY));

		return new EntryCreatedRS(project.getId());
	}

	@Override
	public Project createPersonal(User user) {
		//TODO refactor personal project generation to not add user inside method (cannot be done now, because DAO dependency may affect other services)
		final Project personalProject = personalProjectService.generatePersonalProject(user);
		personalProject.getUsers().clear();
		projectRepository.save(personalProject);
		return personalProject;
	}
}
