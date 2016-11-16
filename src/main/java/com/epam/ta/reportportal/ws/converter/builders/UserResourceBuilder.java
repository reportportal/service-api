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

import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserType;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epam.ta.reportportal.database.personal.PersonalProjectUtils.personalProjectName;

/**
 * Response {@link UserResource} builder for controllers
 * 
 * @author Andrei_Ramanchuk
 * 
 */
@Service
@Scope("prototype")
public class UserResourceBuilder extends ResourceBuilder<UserResource> {

	@Autowired
	private ProjectRepository projectRepository;

	public UserResourceBuilder addUser(User user) {
		if (null != user){
			return addUser(user, projectRepository.findUserProjects(user.getLogin()));
		}
		return this;
	}

	private UserResourceBuilder addUser(User user, List<Project> projects) {
		if (user != null) {
			UserResource resource = getObject();
			resource.setUserId(user.getLogin());
			resource.setEmail(user.getEmail());
			resource.setPhotoId(user.getPhotoId());
			resource.setFullName(user.getFullName());
			resource.setAccountType(user.getType().toString());
			resource.setUserRole(user.getRole().toString());
			resource.setLastlogin(user.getMetaInfo().getLastLogin());
			resource.setIsLoaded(UserType.UPSA != user.getType());

			if (projects.size() > 1)
				Collections.sort(projects, PROJECT_NAME_ALPHABET);
			LinkedHashMap<String, UserResource.AssignedProject> userProjects = new LinkedHashMap<>(projects.size());
			for (Project project : projects) {
				UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
				Project.UserConfig userConfig = project.getUsers().get(user.getId());
				assignedProject.setProjectRole(userConfig.getProjectRole().name());
				assignedProject.setProposedRole(userConfig.getProposedRole().name());
				assignedProject.setEntryType(project.getConfiguration().getEntryType().name());
				userProjects.put(project.getId(), assignedProject);
			}

			resource.setAssignedProjects(userProjects);

			if (userProjects.containsKey(user.getDefaultProject())) {
				resource.setDefaultProject(user.getDefaultProject());
			} else {
				resource.setDefaultProject(personalProjectName(user.getId()));
			}
		}
		return this;
	}

	@Override
	protected UserResource initObject() {
		return new UserResource();
	}

	private static Comparator<Project> PROJECT_NAME_ALPHABET = (prj1, prj2) -> {
		int res = String.CASE_INSENSITIVE_ORDER.compare(prj1.getName(), prj2.getName());
		if (res == 0) {
			res = prj1.getName().compareTo(prj2.getName());
		}
		return res;
	};
}
