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

package com.epam.ta.reportportal.core.project.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.project.ICreateProjectHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.ProjectSettingsRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.ProjectBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.*;

/**
 * @author Hanna_Sukhadolava
 * 
 */
@Service
public class CreateProjectHandler implements ICreateProjectHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectSettingsRepository settingsRepository;

	@Autowired
	@Qualifier("projectBuilder.reference")
	private LazyReference<ProjectBuilder> projectBuilder;

	@Override
	public EntryCreatedRS createProject(CreateProjectRQ createProjectRQ, String username) {

		String projectName = createProjectRQ.getProjectName().toLowerCase().trim();
		Project existProject = projectRepository.findByName(projectName);
		BusinessRule.expect(existProject, Predicates.isNull()).verify(ErrorType.PROJECT_ALREADY_EXISTS, projectName);
		Optional<EntryType> projectType = EntryType.findByName(createProjectRQ.getEntryType());
		BusinessRule.expect(projectType, isPresent()).verify(ErrorType.BAD_REQUEST_ERROR,
				createProjectRQ.getEntryType());
		BusinessRule.expect(projectType.get(), not(equalTo(EntryType.PERSONAL))).verify(ErrorType.BAD_REQUEST_ERROR, "Cannot create personal project manually");

		Project project = projectBuilder.get().addCreateProjectRQ(createProjectRQ).build();
		Project.UserConfig userConfig = new Project.UserConfig();
		userConfig.setProjectRole(ProjectRole.PROJECT_MANAGER);
		userConfig.setProposedRole(ProjectRole.PROJECT_MANAGER);
		project.getUsers().put(username, userConfig);

		Project createdProject;
		try {
			createdProject = projectRepository.save(project);
			settingsRepository.save(new ProjectSettings(createdProject.getId()));
		} catch (Exception e) {
			throw new ReportPortalException("Error during creating Project", e);
		}

		return new EntryCreatedRS(createdProject.getId());
	}
}