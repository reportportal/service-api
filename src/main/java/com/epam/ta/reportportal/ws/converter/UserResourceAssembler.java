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
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.UserController;
import com.epam.ta.reportportal.ws.converter.builders.UserResourceBuilder;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource Assembler for the {@link User} DB entity.
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UserResourceAssembler extends PagedResourcesAssember<User, UserResource> {

	@Autowired
	@Qualifier("userResourceBuilder.reference")
	private LazyReference<UserResourceBuilder> builder;

	public UserResourceAssembler() {
		super(UserController.class, UserResource.class);
	}

	@Override
	public UserResource toResource(User user) {
		return builder.get().addUser(user).addLink(ControllerLinkBuilder.linkTo(UserController.class).slash(user).withSelfRel()).build();
	}

	public PagedResources<UserResource> toPagedResources(Page<User> content, Project project) {
		Map<String, Project.UserConfig> usersConfig = project.getUsers();
		String entryType = project.getConfiguration().getEntryType().name();
		PagedResources<UserResource> userResources = toPagedResources(content);
		for (UserResource userResource : userResources) {
			HashMap<String, UserResource.AssignedProject> assignedProjects = new HashMap<>();
			UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
			assignedProject.setProposedRole(usersConfig.get(userResource.getUserId()).getProposedRole().name());
			assignedProject.setProjectRole(usersConfig.get(userResource.getUserId()).getProjectRole().name());
			assignedProject.setEntryType(entryType);
			assignedProjects.put(project.getId(), assignedProject);
			userResource.setAssignedProjects(assignedProjects);
		}
		return userResources;
	}
}