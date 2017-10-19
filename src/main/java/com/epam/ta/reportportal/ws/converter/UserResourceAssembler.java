/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.ws.converter.builders.UserResourceBuilder;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.HashMap;

import static java.util.Optional.ofNullable;

/**
 * Resource Assembler for the {@link User} DB entity.
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UserResourceAssembler extends PagedResourcesAssembler<User, UserResource> {

	@Autowired
	private Provider<UserResourceBuilder> builder;

	@Override
	public UserResource toResource(User user) {
		return builder.get().addUser(user).build();
	}

	public com.epam.ta.reportportal.ws.model.Page<UserResource> toPagedResources(Page<User> content, Project project) {
		String entryType = project.getConfiguration().getEntryType().name();
		com.epam.ta.reportportal.ws.model.Page<UserResource> userResources = toPagedResources(content);
		for (UserResource userResource : userResources) {
			HashMap<String, UserResource.AssignedProject> assignedProjects = new HashMap<>();
			UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
			Project.UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, userResource.getUserId());

			ofNullable(userConfig.getProjectRole()).ifPresent(it -> assignedProject.setProjectRole(it.name()));
			ofNullable(userConfig.getProposedRole()).ifPresent(it -> assignedProject.setProposedRole(it.name()));

			assignedProject.setEntryType(entryType);
			assignedProjects.put(project.getId(), assignedProject);
			userResource.setAssignedProjects(assignedProjects);
		}
		return userResources;
	}
}
